package murach.DAO;

import murach.business.Appointment;
import murach.business.AppointmentSlot;
import murach.business.Patient;
import murach.util.JPAUtil;
import murach.business.Specialty;
import murach.business.DoctorSchedule;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public final class AppointmentDB
{
    private static final String FETCH_FOR_ADMIN =
            "SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user pu " +
                    "JOIN FETCH a.schedule s JOIN FETCH s.doctor d JOIN FETCH d.user du " +
                    "LEFT JOIN FETCH d.specialty sp LEFT JOIN FETCH a.slot ";

    public Appointment createBooking(Long patientId, Long slotId, String reason, BigDecimal fee)
    {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá đăng ký khám không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            AppointmentSlot slot = em.find(AppointmentSlot.class, slotId);
            if (slot == null) throw new IllegalArgumentException("Khung giờ không tồn tại");
            DoctorLeaveDB.lockDoctor(em, slot.getSchedule().getDoctor().getDoctorId());
            em.refresh(slot.getSchedule(), LockModeType.PESSIMISTIC_WRITE);
            em.refresh(slot, LockModeType.PESSIMISTIC_WRITE);
            if (slot.getSchedule().getStatus() == DoctorSchedule.Status.REPLACED)
                throw new IllegalStateException("Ca đã chuyển bác sĩ; vui lòng chọn lại khung giờ");
            Patient patient = em.find(Patient.class, patientId);

            if (slot == null) throw new IllegalArgumentException("Khung giờ không tồn tại");
            if (patient == null) throw new IllegalArgumentException("Bệnh nhân không tồn tại");
            Specialty specialty = slot.getSchedule().getDoctor().getSpecialty();

            if (specialty == null || !specialty.isStatus() || specialty.getSpecialtyType() != Specialty.SpecialtyType.CLINICAL)
            {
                throw new IllegalArgumentException(
                        "Chuyên khoa không nhận đăng ký khám trực tiếp");
            }
            if (slot.getStatus() != AppointmentSlot.Status.OPEN)
                throw new IllegalArgumentException("Khung giờ đã đóng");
            if (slot.getSchedule().getWorkDate().isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Không thể đặt lịch trong quá khứ");
            if (slot.getSchedule().getWorkDate().equals(LocalDate.now())
                    && !slot.getStartTime().isAfter(LocalTime.now()))
                throw new IllegalArgumentException("Khung giờ này đã bắt đầu hoặc đã kết thúc");

            long patientBooking = em.createQuery(
                            "SELECT COUNT(a.appointmentId) FROM Appointment a "
                                    + "WHERE a.slot = :slot AND a.patient = :patient "
                                    + "AND a.status <> :cancelled", Long.class)
                    .setParameter("slot", slot)
                    .setParameter("patient", patient)
                    .setParameter("cancelled", Appointment.Status.CANCELLED)
                    .getSingleResult();
            if (patientBooking > 0)
                throw new IllegalArgumentException("Bạn đã đăng ký trong khung giờ này");

            long booked = em.createQuery(
                            "SELECT COUNT(a.appointmentId) FROM Appointment a "
                                    + "WHERE a.slot = :slot AND a.status <> :cancelled",
                            Long.class)
                    .setParameter("slot", slot)
                    .setParameter("cancelled", Appointment.Status.CANCELLED)
                    .getSingleResult();
            if (booked >= slot.getMaxPatients())
                throw new IllegalArgumentException("Khung giờ vừa hết chỗ, vui lòng chọn giờ khác");

            String code = "APP-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 12).toUpperCase();
            Appointment appointment = new Appointment(
                    code, patient, slot, reason, fee,
                    Appointment.Status.PENDING);
            em.persist(appointment);
            transaction.commit();
            return appointment;
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

    public List<Appointment> getAllAppointments() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user" +
                    " JOIN FETCH a.schedule s JOIN FETCH s.doctor d" +
                    " JOIN FETCH d.user JOIN FETCH d.specialty " +
                    " LEFT JOIN FETCH a.slot " +
                    "ORDER BY s.workDate DESC, s.startTime DESC",
                    Appointment.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //tìm bệnh nhân theo Id
    public List<Appointment> findByPatientId(Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH p.user" +
                    " JOIN FETCH a.schedule s JOIN FETCH s.doctor d" +
                    " JOIN FETCH d.user JOIN FETCH d.specialty " +
                    " LEFT JOIN FETCH a.slot " +
                    "WHERE p.patientId = :patientId " +
                    "ORDER BY s.workDate DESC, s.startTime DESC", Appointment.class)
                    .setParameter("patientId", patientId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Toan: Tìm lịch hẹn theo từ khóa, trạng thái, chuyên khoa và ngày khám.
    public List<Appointment> search(String keyword, Appointment.Status status,
                                    Long specialtyId, LocalDate date)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(FETCH_FOR_ADMIN + "WHERE 1 = 1");
            if (status != null) jpql.append(" AND a.status = :status");
            if (specialtyId != null) jpql.append(" AND sp.specialtyId = :specialtyId");
            if (date != null) jpql.append(" AND s.workDate = :date");
            jpql.append(" ORDER BY s.workDate DESC, s.startTime DESC");
            TypedQuery<Appointment> query = em.createQuery(jpql.toString(), Appointment.class);
            if (status != null) query.setParameter("status", status);
            if (specialtyId != null) query.setParameter("specialtyId", specialtyId);
            if (date != null) query.setParameter("date", date);
            return query.getResultList().stream()
                    .filter(a -> DoctorDB.matches(keyword, a.getAppointmentCode(),
                            a.getPatient().getFullName(), a.getPatient().getPhone(),
                            a.getSchedule().getDoctor().getFullName()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // Toan: Lấy đầy đủ một lịch hẹn để admin/staff xem chi tiết.
    public Appointment findById(Long appointmentId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Appointment> appointments = em.createQuery(
                            FETCH_FOR_ADMIN + "WHERE a.appointmentId = :id", Appointment.class)
                    .setParameter("id", appointmentId)
                    .getResultList();
            return appointments.isEmpty() ? null : appointments.get(0);
        } finally {
            em.close();
        }
    }

    // Toan: Admin/staff chỉ được xác nhận hoặc hủy lịch, không được hoàn tất buổi khám.
    public void updateAdministrativeStatus(Long appointmentId, Appointment.Status newStatus)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Appointment appointment = em.find(Appointment.class, appointmentId, LockModeType.PESSIMISTIC_WRITE);
            if (appointment == null) throw new IllegalArgumentException("Lịch hẹn không tồn tại");
            Appointment.Status current = appointment.getStatus();
            boolean valid = newStatus == Appointment.Status.CONFIRMED
                    && current == Appointment.Status.PENDING;
            valid = valid || newStatus == Appointment.Status.CANCELLED
                    && (current == Appointment.Status.PENDING
                    || current == Appointment.Status.CONFIRMED);
            if (!valid) throw new IllegalArgumentException("Không thể chuyển trạng thái lịch hẹn này");
            appointment.changeStatus(newStatus);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Lấy các lịch đang chờ khám của bác sĩ trong một ngày cụ thể.
    public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate workDate)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String jpql =
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.patient p " +
                            "JOIN FETCH p.user pu " +
                            "JOIN FETCH a.schedule s " +
                            "JOIN s.doctor d " +
                            "LEFT JOIN FETCH a.slot sl " +
                            "WHERE d.doctorId = :doctorId " +
                            "AND s.workDate = :workDate AND s.status = :working " +
                            "AND a.status IN (:pending, :confirmed) " +
                            "ORDER BY sl.startTime ASC";
            return em.createQuery(jpql, Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("workDate", workDate)
                    .setParameter("working", DoctorSchedule.Status.ACTIVE)
                    .setParameter("pending", Appointment.Status.PENDING)
                    .setParameter("confirmed", Appointment.Status.CONFIRMED)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Lấy một lịch hẹn và thông tin bệnh nhân, đồng thời xác nhận lịch thuộc bác sĩ đang đăng nhập.
    public Appointment findByIdAndDoctorId(Long appointmentId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String jpql =
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.patient p " +
                            "JOIN FETCH p.user pu " +
                            "JOIN FETCH a.schedule s " +
                            "JOIN s.doctor d " +
                            "LEFT JOIN FETCH a.slot sl " +
                            "WHERE a.appointmentId = :appointmentId " +
                            "AND d.doctorId = :doctorId";

            return em.createQuery(jpql, Appointment.class)
                    .setParameter("appointmentId", appointmentId)
                    .setParameter("doctorId", doctorId)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
        finally
        {
            em.close();
        }
    }

    //chuyển status pending sang thành công cho thanh toán
    public void cancelAppointment(Long appointmentId, Long patientId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Appointment appointment = em.find(Appointment.class, appointmentId, LockModeType.PESSIMISTIC_WRITE);

            if (appointment == null) {
                throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
            }
            if (!appointment.getPatient().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException("Bạn không có quyền huỷ lịch này.");
            }
            // Chỉ cho phép huỷ nếu ngày hiện tại TRƯỚC ngày khám (trước 0h)
            if (!LocalDate.now().isBefore(appointment.getSchedule().getWorkDate())) {
                throw new IllegalArgumentException("Chỉ có thể huỷ lịch khám trước 0h ngày khám.");
            }
            if (appointment.getStatus() == Appointment.Status.CANCELLED || appointment.getStatus() == Appointment.Status.COMPLETED) {
                throw new IllegalArgumentException("Lịch hẹn này không thể huỷ được nữa.");
            }

            appointment.changeStatus(Appointment.Status.CANCELLED);
            em.merge(appointment);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
