package murach.DAO;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.persistence.EntityManager;
import murach.business.*;

/** Kiểm tra lịch theo ngày, không coi lịch phân công là chấm công có mặt thực tế. */
public final class ClinicalAvailabilityDB
{
    private ClinicalAvailabilityDB() {}

    public static LocalDate today() { return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")); }

    public static boolean isReceptionistScheduled(EntityManager em, Long staffId, LocalDate date)
    {
        return em.createQuery("SELECT COUNT(s.scheduleId) FROM StaffSchedule s " +
                "WHERE s.staff.staffId = :profileId AND s.staff.jobType = :jobType AND s.workDate = :date", Long.class)
                .setParameter("profileId", staffId).setParameter("jobType", Staff.JobType.RECEPTIONIST)
                .setParameter("date", date).getSingleResult() > 0;
    }

    public static boolean isNurseScheduled(EntityManager em, Long nurseId, LocalDate date)
    {
        return em.createQuery("SELECT COUNT(s.scheduleId) FROM NurseSchedule s " +
                "WHERE s.nurse.nurseId = :profileId AND s.workDate = :date", Long.class)
                .setParameter("profileId", nurseId).setParameter("date", date).getSingleResult() > 0;
    }

    public static boolean isDoctorScheduled(EntityManager em, Long doctorId, LocalDate date)
    {
        return em.createQuery("SELECT COUNT(s.scheduleId) FROM DoctorSchedule s " +
                "WHERE s.doctor.doctorId = :profileId AND s.workDate = :date AND s.status = :working", Long.class)
                .setParameter("working", DoctorSchedule.Status.ACTIVE)
                .setParameter("profileId", doctorId).setParameter("date", date).getSingleResult() > 0;
    }

    public static boolean isTechnicianScheduled(EntityManager em, Long technicianId, LocalDate date)
    {
        return em.createQuery("SELECT COUNT(s.scheduleId) FROM TechnicianSchedule s " +
                "WHERE s.technician.technicianId = :profileId AND s.workDate = :date", Long.class)
                .setParameter("profileId", technicianId).setParameter("date", date).getSingleResult() > 0;
    }

    public static void requireServiceAvailable(EntityManager em, DiagnosticService service, LocalDate date)
    {
        Long count;
        if (service.getServiceType() == DiagnosticService.ServiceType.IMAGING)
            count = em.createQuery("SELECT COUNT(s.scheduleId) FROM DoctorSchedule s " +
                    "WHERE s.workDate = :date AND s.status = :working AND s.doctor.specialty = :specialty AND EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = s.doctor.user AND a.role = :role AND a.status = :active)", Long.class)
                    .setParameter("date", date).setParameter("specialty", service.getSpecialty())
                    .setParameter("working", DoctorSchedule.Status.ACTIVE)
                    .setParameter("role", Account.Role.DOCTOR).setParameter("active", Account.Status.ACTIVE).getSingleResult();
        else
            count = em.createQuery("SELECT COUNT(s.scheduleId) FROM TechnicianSchedule s " +
                    "WHERE s.workDate = :date AND s.technician.specialty = :specialty AND EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = s.technician.user AND a.role = :role AND a.status = :active)", Long.class)
                    .setParameter("date", date).setParameter("specialty", service.getSpecialty())
                    .setParameter("role", Account.Role.TECHNICIAN).setParameter("active", Account.Status.ACTIVE).getSingleResult();
        if (count == 0) throw new IllegalStateException("Dịch vụ " + service.getServiceName() + " chưa có nhân sự làm việc trong ngày " + date);
    }

    public static void requireDoctorWorking(EntityManager em, Doctor doctor)
    {
        if (!isDoctorScheduled(em, doctor.getDoctorId(), today()))
            throw new IllegalStateException("Bác sĩ chưa có lịch làm việc hôm nay");
    }

    public static void requireTechnicianWorking(EntityManager em, Technician technician)
    {
        if (!isTechnicianScheduled(em, technician.getTechnicianId(), today()))
            throw new IllegalStateException("Chuyên viên chưa có lịch làm việc hôm nay");
    }
}
