package murach.DAO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import murach.business.Account;
import murach.business.Appointment;
import murach.business.Invoice;
import murach.business.MedicalEncounter;
import murach.business.Payment;
import murach.business.Staff;
import murach.business.Specialty;
import murach.util.JPAUtil;


public class MedicalEncounterDB {
    private final QueueTicketDB queueTicketDB = new QueueTicketDB();

    public List<Appointment> findPaidAppointments(LocalDate workDate, String keyword, Long staffId, Long userId)
    {
        if (workDate == null) throw new IllegalArgumentException("Ngày khám không hợp lệ");
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            requireReceptionist(em, staffId, userId);
            if (!ClinicalAvailabilityDB.isReceptionistScheduled(em, staffId, workDate))
                return java.util.Collections.emptyList();
            String value = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
            String jpql =
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.patient p JOIN FETCH p.user u " +
                            "JOIN FETCH a.schedule s JOIN FETCH s.doctor d JOIN FETCH d.user JOIN FETCH d.specialty sp " +
                            "LEFT JOIN FETCH a.slot sl " +
                            "WHERE s.workDate = :workDate AND a.status = :confirmed " +
                            "AND sp.status = true AND sp.specialtyType = :clinicalType " +
                            "AND EXISTS (SELECT pm.paymentId FROM Payment pm LEFT JOIN pm.invoice i " +
                            "WHERE pm.appointment = a AND pm.status = :paid " +
                            "AND (i.invoiceId IS NULL OR (i.invoiceType = :consultation AND i.status = :invoicePaid))) " +
                            "AND NOT EXISTS (SELECT e.encounterId FROM MedicalEncounter e WHERE e.appointment = a) " +
                            "AND (LOWER(a.appointmentCode) LIKE :keyword OR LOWER(p.patientCode) LIKE :keyword " +
                            "OR LOWER(u.fullName) LIKE :keyword OR u.phone LIKE :keyword) " +
                            "ORDER BY sl.startTime ASC, a.appointmentId ASC";

            return em.createQuery(jpql, Appointment.class)
                    .setParameter("workDate", workDate)
                    .setParameter("confirmed", Appointment.Status.CONFIRMED)
                    .setParameter("clinicalType", Specialty.SpecialtyType.CLINICAL)
                    .setParameter("paid", Payment.Status.PAID)
                    .setParameter("consultation", Invoice.InvoiceType.CONSULTATION)
                    .setParameter("invoicePaid", Invoice.Status.PAID)
                    .setParameter("keyword", "%" + value + "%")
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public Long checkIn(Long appointmentId, Long staffId, Long userId)
    {
        if (appointmentId == null || appointmentId <= 0
                || staffId == null || staffId <= 0
                || userId == null || userId <= 0)
        {
            throw new IllegalArgumentException(
                    "Thông tin lịch hẹn hoặc người tiếp nhận không hợp lệ");
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            Staff staff = requireReceptionist(em, staffId, userId);
            if (!ClinicalAvailabilityDB.isReceptionistScheduled(em, staffId, ClinicalAvailabilityDB.today()))
                throw new IllegalStateException("Tiếp tân chưa có lịch làm việc hôm nay");

            Appointment appointment = em.find(Appointment.class, appointmentId);
            if (appointment == null) throw new IllegalArgumentException("Không tìm thấy lịch hẹn");
            Long previousDoctorId = appointment.getSchedule().getDoctor().getDoctorId();
            DoctorLeaveDB.lockDoctor(em, previousDoctorId);
            em.refresh(appointment, LockModeType.PESSIMISTIC_WRITE);
            if (!previousDoctorId.equals(appointment.getSchedule().getDoctor().getDoctorId()))
                throw new IllegalStateException("Lịch vừa chuyển bác sĩ; vui lòng tải lại danh sách");
            if (appointment == null || appointment.getStatus() != Appointment.Status.CONFIRMED)
                throw new IllegalArgumentException("Lịch hẹn không đủ điều kiện tiếp nhận");
            Specialty specialty = appointment.getSchedule().getDoctor().getSpecialty();
            if (specialty == null || !specialty.isStatus()
                    || specialty.getSpecialtyType() != Specialty.SpecialtyType.CLINICAL)
                throw new IllegalArgumentException("Lịch hẹn không thuộc khoa lâm sàng đang hoạt động");
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            if (!today.equals(appointment.getSchedule().getWorkDate()))
                throw new IllegalArgumentException("Chỉ tiếp nhận lịch khám của ngày hôm nay");

            Long paid = em.createQuery(
                            "SELECT COUNT(pm.paymentId) FROM Payment pm LEFT JOIN pm.invoice i " +
                                    "WHERE pm.appointment = :appointment AND pm.status = :paid " +
                                    "AND (i.invoiceId IS NULL OR (i.invoiceType = :consultation AND i.status = :invoicePaid))",
                            Long.class)
                    .setParameter("appointment", appointment)
                    .setParameter("paid", Payment.Status.PAID)
                    .setParameter("consultation", Invoice.InvoiceType.CONSULTATION)
                    .setParameter("invoicePaid", Invoice.Status.PAID)
                    .getSingleResult();
            if (paid == 0)
                throw new IllegalArgumentException("Chưa thanh toán phí khám");

            List<MedicalEncounter> existing = em.createQuery(
                            "SELECT e FROM MedicalEncounter e WHERE e.appointment = :appointment",
                            MedicalEncounter.class)
                    .setParameter("appointment", appointment)
                    .getResultList();
            if (!existing.isEmpty())
            {
                transaction.commit();
                return existing.get(0).getEncounterId();
            }

            String code = "ENC-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
            MedicalEncounter encounter = new MedicalEncounter(code, appointment, staff);
            em.persist(encounter);
            queueTicketDB.issue(em, encounter);
            transaction.commit();
            return encounter.getEncounterId();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    // Kiểm tra cả hồ sơ thực trong DB; STAFF quản lý cũ không mặc nhiên là tiếp tân.
    private Staff requireReceptionist(EntityManager em, Long staffId, Long userId)
    {
        return ClinicalAccess.receptionist(em, staffId, userId);
    }

    // Chỉ đọc tiến trình của bệnh nhân đăng nhập, kể cả lượt chưa có phiếu cấp số.
    public Map<Long, Map<String, Object>> findProgressByPatientId(Long patientId)
    {
        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Mã bệnh nhân không hợp lệ");
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Object[]> rows = em.createQuery(
                    "SELECT a.appointmentId, e.status, q.queueNumber FROM MedicalEncounter e " +
                    "JOIN e.appointment a LEFT JOIN QueueTicket q ON q.encounter = e " +
                    "WHERE a.patient.patientId = :patientId", Object[].class)
                    .setParameter("patientId", patientId).getResultList();
            Map<Long, Map<String, Object>> progress = new LinkedHashMap<Long, Map<String, Object>>();
            for (Object[] row : rows)
            {
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("encounterStatus", row[1]);
                item.put("queueNumber", row[2]);
                progress.put((Long) row[0], item);
            }
            return progress;
        }
        finally
        {
            em.close();
        }
    }
}
