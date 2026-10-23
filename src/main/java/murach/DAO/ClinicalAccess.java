package murach.DAO;

import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import murach.business.*;

/** Các kiểm tra dùng chung; hàm phối hợp không tự mở/commit transaction. */
public final class ClinicalAccess
{
    private ClinicalAccess() {}

    public static String code(String prefix)
    {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static User requireUser(EntityManager em, Long userId, Account.Role role)
    {
        List<Account> accounts = em.createQuery(
                "SELECT a FROM Account a JOIN FETCH a.user WHERE a.user.userId = :userId " +
                "AND a.role = :role AND a.status = :active", Account.class)
                .setParameter("userId", userId).setParameter("role", role)
                .setParameter("active", Account.Status.ACTIVE).getResultList();
        if (accounts.isEmpty()) throw new SecurityException("Tài khoản không có quyền hoặc đã bị khóa");
        return accounts.get(0).getUser();
    }

    public static Doctor doctorForUser(EntityManager em, Long userId)
    {
        requireUser(em, userId, Account.Role.DOCTOR);
        List<Doctor> doctors = em.createQuery(
                "SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.specialty " +
                "WHERE d.user.userId = :userId", Doctor.class)
                .setParameter("userId", userId).getResultList();
        if (doctors.isEmpty()) throw new SecurityException("Chưa có hồ sơ bác sĩ");
        return doctors.get(0);
    }

    public static Staff receptionist(EntityManager em, Long staffId, Long userId)
    {
        if (staffId == null || staffId <= 0 || userId == null || userId <= 0)
            throw new SecurityException("Phiên đăng nhập thiếu hồ sơ tiếp tân");
        requireUser(em, userId, Account.Role.STAFF);
        Staff staff = em.find(Staff.class, staffId);
        if (staff == null || !userId.equals(staff.getUserId()) || staff.getJobType() != Staff.JobType.RECEPTIONIST)
            throw new SecurityException("Hồ sơ đăng nhập không phải tiếp tân");
        return staff;
    }

    public static MedicalEncounter lockForDoctor(EntityManager em, Long encounterId, Long doctorId)
    {
        DoctorLeaveDB.lockDoctor(em, doctorId);
        MedicalEncounter encounter = em.find(MedicalEncounter.class, encounterId, LockModeType.PESSIMISTIC_WRITE);
        if (encounter == null || !doctorId.equals(encounter.getDoctor().getDoctorId()))
            throw new SecurityException("Lượt khám không thuộc bác sĩ đang đăng nhập");
        requireUser(em, encounter.getDoctor().getUser().getUserId(), Account.Role.DOCTOR);
        if (encounter.getAppointment().getSchedule().getStatus() != DoctorSchedule.Status.ACTIVE)
            throw new IllegalStateException("Ca đã được duyệt nghỉ hoặc đã chuyển bác sĩ");
        if (encounter.getAppointment().getStatus() != Appointment.Status.CONFIRMED)
            throw new IllegalStateException("Lịch hẹn không còn đủ điều kiện khám");
        requireAssessment(em, encounter);
        return encounter;
    }

    public static InitialAssessment requireAssessment(EntityManager em, MedicalEncounter encounter)
    {
        List<InitialAssessment> assessments = em.createQuery(
                "SELECT ia FROM InitialAssessment ia WHERE ia.encounter = :encounter AND ia.completed = true",
                InitialAssessment.class).setParameter("encounter", encounter).getResultList();
        if (assessments.isEmpty()) throw new IllegalStateException("Chưa hoàn tất đánh giá ban đầu");
        return assessments.get(0);
    }

    public static void requireConsulting(MedicalEncounter encounter)
    {
        if (encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalStateException("Chỉ thực hiện thao tác này khi đang khám");
    }

    public static Technician technician(EntityManager em, Long userId)
    {
        requireUser(em, userId, Account.Role.TECHNICIAN);
        List<Technician> items = em.createQuery("SELECT t FROM Technician t JOIN FETCH t.user " +
                "JOIN FETCH t.specialty WHERE t.user.userId = :userId",
                Technician.class).setParameter("userId", userId).getResultList();
        if (items.isEmpty()) throw new SecurityException("Chưa có hồ sơ kỹ thuật viên");
        if (items.get(0).getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
            throw new SecurityException("Kỹ thuật viên phải thuộc khoa cận lâm sàng");
        return items.get(0);
    }

    public static void requirePerformer(EntityManager em, DiagnosticOrderItem item, Long userId)
    {
        if (item.getServiceTypeSnapshot() == DiagnosticService.ServiceType.LABORATORY)
        {
            Technician actor = technician(em, userId);
            if (item.getAssignedTechnician() == null ||
                    !actor.getTechnicianId().equals(item.getAssignedTechnician().getTechnicianId()))
                throw new SecurityException("Chỉ định không được giao cho kỹ thuật viên này");
        }
        else
        {
            Doctor actor = doctorForUser(em, userId);
            if (actor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL ||
                    item.getAssignedDoctor() == null ||
                    !actor.getDoctorId().equals(item.getAssignedDoctor().getDoctorId()))
                throw new SecurityException("Chỉ định không được giao cho bác sĩ này");
        }
    }

    // Thao tác ghi còn cần lịch làm việc; xem kết quả cũ vẫn được kiểm tra theo người phụ trách.
    public static void requireWorkingPerformer(EntityManager em, DiagnosticOrderItem item, Long userId)
    {
        requirePerformer(em, item, userId);
        if (item.getServiceTypeSnapshot() == DiagnosticService.ServiceType.LABORATORY)
            ClinicalAvailabilityDB.requireTechnicianWorking(em, item.getAssignedTechnician());
        else
            ClinicalAvailabilityDB.requireDoctorWorking(em, item.getAssignedDoctor());
    }

    // Luôn khóa lượt trước, sau đó mới khóa chỉ định để tránh khóa chéo.
    public static DiagnosticOrderItem lockItem(EntityManager em, Long itemId)
    {
        DiagnosticOrderItem found = em.find(DiagnosticOrderItem.class, itemId);
        if (found == null)
            throw new IllegalArgumentException("Không tìm thấy chỉ định");
        MedicalEncounter encounter = found.getOrder().getEncounter();
        em.refresh(encounter, LockModeType.PESSIMISTIC_WRITE);
        em.refresh(found, LockModeType.PESSIMISTIC_WRITE);
        if (encounter.getStatus() != MedicalEncounter.Status.WAITING_RESULTS ||
                found.getOrder().getStatus() != DiagnosticOrder.Status.CONFIRMED)
            throw new IllegalStateException("Lượt khám không còn ở bước chờ kết quả");
        InvoiceDB.requireDiagnosticPaid(em, found.getOrder());
        return found;
    }
}
