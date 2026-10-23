package murach.DAO;

import java.time.*;
import java.util.*;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public final class DoctorLeaveDB
{
    private static final String FETCH = "SELECT l FROM DoctorLeaveRequest l " +
            "JOIN FETCH l.schedule s JOIN FETCH s.doctor d JOIN FETCH d.user JOIN FETCH d.specialty " +
            "LEFT JOIN FETCH l.reviewedBy LEFT JOIN FETCH l.replacementSchedule rs " +
            "LEFT JOIN FETCH rs.doctor rd LEFT JOIN FETCH rd.user ";

    public static LocalDateTime now() { return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")); }

    static void requireFuture(DoctorSchedule schedule)
    {
        if (!schedule.getWorkDate().atTime(schedule.getStartTime()).isAfter(now()))
            throw new IllegalStateException("Chỉ xử lý nghỉ và nhận thay ca chưa bắt đầu");
    }

    // Các luồng đặt khám, cấp số, bắt đầu khám và bàn giao đều khóa bác sĩ trước.
    static Doctor lockDoctor(EntityManager em, Long doctorId)
    {
        Doctor doctor = em.find(Doctor.class, doctorId, LockModeType.PESSIMISTIC_WRITE);
        if (doctor == null) throw new IllegalArgumentException("Không tìm thấy bác sĩ");
        em.refresh(doctor, LockModeType.PESSIMISTIC_WRITE);
        return doctor;
    }

    private static List<Long> checkedIds(List<Long> ids)
    {
        if (ids == null || ids.isEmpty() || ids.size() > 2 || new HashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("Chọn một hoặc hai ca trong cùng ngày");
        for (Long id : ids) if (id == null || id <= 0) throw new IllegalArgumentException("Mã ca không hợp lệ");
        List<Long> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        return sorted;
    }

    private static void requireSameDay(DoctorSchedule first, DoctorSchedule next)
    {
        if (!first.getWorkDate().equals(next.getWorkDate()) ||
                !first.getDoctor().getDoctorId().equals(next.getDoctor().getDoctorId()))
            throw new IllegalArgumentException("Hai ca phải cùng ngày và cùng bác sĩ xin nghỉ");
        if (!first.getScheduleId().equals(next.getScheduleId()) &&
                first.getStartTime().isBefore(next.getEndTime()) && first.getEndTime().isAfter(next.getStartTime()))
            throw new IllegalArgumentException("Hai ca bị trùng giờ");
    }

    public List<DoctorSchedule> findRequestableSchedules(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
            List<DoctorSchedule> schedules = em.createQuery("SELECT s FROM DoctorSchedule s " +
                    "WHERE s.doctor = :doctor AND s.status = :active AND s.workDate >= :today " +
                    "AND NOT EXISTS (SELECT l.leaveRequestId FROM DoctorLeaveRequest l " +
                    "WHERE l.schedule = s AND l.status <> :rejected) ORDER BY s.workDate, s.startTime", DoctorSchedule.class)
                    .setParameter("doctor", doctor).setParameter("active", DoctorSchedule.Status.ACTIVE)
                    .setParameter("today", ClinicalAvailabilityDB.today()).setParameter("rejected", DoctorLeaveRequest.Status.REJECTED)
                    .getResultList();
            schedules.removeIf(s -> !s.getWorkDate().atTime(s.getStartTime()).isAfter(now()));
            return schedules;
        }
        finally { em.close(); }
    }

    public List<DoctorLeaveRequest> findForDoctor(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            ClinicalAccess.doctorForUser(em, userId);
            return em.createQuery(FETCH + "WHERE d.user.userId = :userId OR rd.user.userId = :userId " +
                    "ORDER BY l.createdAt DESC", DoctorLeaveRequest.class).setParameter("userId", userId).getResultList();
        }
        finally { em.close(); }
    }

    public List<DoctorLeaveRequest> findForAdmin(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            ClinicalAccess.requireUser(em, userId, Account.Role.ADMIN);
            return em.createQuery(FETCH + "ORDER BY s.workDate DESC, s.startTime, l.leaveRequestId DESC",
                    DoctorLeaveRequest.class).getResultList();
        }
        finally { em.close(); }
    }

    public List<DoctorLeaveRequest> findOpenReplacements(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
            List<DoctorLeaveRequest> leaves = em.createQuery(FETCH +
                    "WHERE l.status = :waiting AND d.specialty = :specialty AND s.workDate >= :today " +
                    "ORDER BY s.workDate, s.startTime", DoctorLeaveRequest.class)
                    .setParameter("waiting", DoctorLeaveRequest.Status.WAITING_REPLACEMENT)
                    .setParameter("specialty", doctor.getSpecialty()).setParameter("today", ClinicalAvailabilityDB.today()).getResultList();
            leaves.removeIf(l -> !eligible(em, doctor, l.getSchedule()));
            return leaves;
        }
        finally { em.close(); }
    }

    public void submit(Long userId, List<Long> scheduleIds, String reason)
    {
        List<Long> ids = checkedIds(scheduleIds);
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();
            Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
            lockDoctor(em, doctor.getDoctorId());
            DoctorSchedule first = null;
            for (Long id : ids)
            {
                DoctorSchedule schedule = em.find(DoctorSchedule.class, id, LockModeType.PESSIMISTIC_WRITE);
                if (schedule == null || !doctor.getDoctorId().equals(schedule.getDoctor().getDoctorId()))
                    throw new SecurityException("Ca không thuộc bác sĩ đang đăng nhập");
                requireFuture(schedule);
                if (first == null) first = schedule;
                else requireSameDay(first, schedule);
                Long count = em.createQuery("SELECT COUNT(l.leaveRequestId) FROM DoctorLeaveRequest l " +
                        "WHERE l.schedule = :schedule AND l.status <> :rejected", Long.class)
                        .setParameter("schedule", schedule).setParameter("rejected", DoctorLeaveRequest.Status.REJECTED).getSingleResult();
                if (count > 0) throw new IllegalStateException("Ca đã có đơn nghỉ");
                requireUnstarted(em, schedule);
                DoctorLeaveRequest leave = new DoctorLeaveRequest(schedule, reason);
                em.persist(leave);
                notifyAdmins(em, leave, doctor.getFullName() + " gửi đơn nghỉ ca " + label(schedule));
            }
            tx.commit();
        }
        catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        finally { em.close(); }
    }

    public void review(Long userId, Long leaveId, boolean approved)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();
            User admin = ClinicalAccess.requireUser(em, userId, Account.Role.ADMIN);
            DoctorLeaveRequest leave = em.find(DoctorLeaveRequest.class, leaveId);
            if (leave == null) throw new IllegalArgumentException("Không tìm thấy đơn nghỉ");
            lockDoctor(em, leave.getSchedule().getDoctor().getDoctorId());
            em.refresh(leave.getSchedule(), LockModeType.PESSIMISTIC_WRITE);
            em.refresh(leave, LockModeType.PESSIMISTIC_WRITE);
            if (approved) { requireFuture(leave.getSchedule()); requireUnstarted(em, leave.getSchedule()); }
            leave.review(admin, approved);
            NotificationDB.send(em, leave.getSchedule().getDoctor().getUser(), leave,
                    (approved ? "Đã duyệt nghỉ ca " : "Đã từ chối đơn nghỉ ca ") + label(leave.getSchedule()));
            if (approved)
            {
                List<Doctor> doctors = em.createQuery("SELECT d FROM Doctor d JOIN FETCH d.user " +
                        "WHERE d.specialty = :specialty AND EXISTS (SELECT a.accountId FROM Account a " +
                        "WHERE a.user = d.user AND a.role = :role AND a.status = :active)", Doctor.class)
                        .setParameter("specialty", leave.getSchedule().getDoctor().getSpecialty())
                        .setParameter("role", Account.Role.DOCTOR).setParameter("active", Account.Status.ACTIVE).getResultList();
                for (Doctor doctor : doctors)
                    if (eligible(em, doctor, leave.getSchedule()))
                        NotificationDB.send(em, doctor.getUser(), leave, "Mời nhận ca thay: " + label(leave.getSchedule()) +
                                ", bác sĩ " + leave.getSchedule().getDoctor().getFullName());
            }
            tx.commit();
        }
        catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        finally { em.close(); }
    }

    // Nhận cả hai ca trong một transaction: một ca không hợp lệ thì không bàn giao ca nào.
    public void accept(Long userId, List<Long> leaveIds)
    {
        List<Long> ids = checkedIds(leaveIds);
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();
            Doctor replacement = ClinicalAccess.doctorForUser(em, userId);
            List<DoctorLeaveRequest> leaves = new ArrayList<>();
            SortedSet<Long> doctors = new TreeSet<>();
            doctors.add(replacement.getDoctorId());
            for (Long id : ids)
            {
                DoctorLeaveRequest leave = em.find(DoctorLeaveRequest.class, id);
                if (leave == null) throw new IllegalArgumentException("Không tìm thấy ca thay");
                leaves.add(leave); doctors.add(leave.getSchedule().getDoctor().getDoctorId());
            }
            for (Long id : doctors) lockDoctor(em, id);
            ClinicalAccess.requireUser(em, userId, Account.Role.DOCTOR);
            DoctorSchedule first = null;
            for (DoctorLeaveRequest leave : leaves)
            {
                DoctorSchedule old = leave.getSchedule();
                em.refresh(old, LockModeType.PESSIMISTIC_WRITE);
                em.refresh(leave, LockModeType.PESSIMISTIC_WRITE);
                if (leave.getStatus() != DoctorLeaveRequest.Status.WAITING_REPLACEMENT ||
                        old.getStatus() != DoctorSchedule.Status.ON_LEAVE)
                    throw new IllegalStateException("Ca đã có người nhận hoặc chưa được duyệt");
                requireFuture(old);
                if (first == null) first = old;
                else requireSameDay(first, old);
                if (!eligible(em, replacement, old)) throw new IllegalStateException("Khác chuyên khoa hoặc trùng lịch làm việc");
                requireUnstarted(em, old);
                transfer(em, leave, replacement);
            }
            tx.commit();
        }
        catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        finally { em.close(); }
    }

    private static void transfer(EntityManager em, DoctorLeaveRequest leave, Doctor replacement)
    {
        DoctorSchedule old = leave.getSchedule();
        DoctorSchedule target = new DoctorSchedule(replacement, old.getWorkDate(), old.getStartTime(), old.getEndTime(), old.getRoom());
        em.persist(target);
        List<AppointmentSlot> slots = em.createQuery("SELECT sl FROM AppointmentSlot sl WHERE sl.schedule = :schedule " +
                "ORDER BY sl.slotId", AppointmentSlot.class).setParameter("schedule", old)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        Map<Long, AppointmentSlot> copies = new HashMap<>();
        for (AppointmentSlot slot : slots)
        {
            AppointmentSlot copy = new AppointmentSlot(target, slot.getStartTime(), slot.getEndTime(), slot.getMaxPatients());
            copy.changeStatus(slot.getStatus());
            em.persist(copy); copies.put(slot.getSlotId(), copy);
            slot.changeStatus(AppointmentSlot.Status.CLOSED);
        }
        List<Appointment> appointments = em.createQuery("SELECT a FROM Appointment a WHERE a.schedule = :schedule " +
                "AND a.status IN (:pending, :confirmed) ORDER BY a.appointmentId", Appointment.class)
                .setParameter("schedule", old).setParameter("pending", Appointment.Status.PENDING)
                .setParameter("confirmed", Appointment.Status.CONFIRMED).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        int nextNumber = em.createQuery("SELECT COALESCE(MAX(q.queueNumber), 0) FROM QueueTicket q " +
                "WHERE q.doctor = :doctor AND q.queueDate = :date", Integer.class)
                .setParameter("doctor", replacement).setParameter("date", old.getWorkDate()).getSingleResult();
        for (Appointment appointment : appointments)
        {
            String queueMessage = "";
            List<MedicalEncounter> encounters = em.createQuery("SELECT e FROM MedicalEncounter e WHERE e.appointment = :appointment",
                    MedicalEncounter.class).setParameter("appointment", appointment).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
            for (MedicalEncounter encounter : encounters)
            {
                em.refresh(encounter, LockModeType.PESSIMISTIC_WRITE);
                encounter.transferTo(replacement);
                List<QueueTicket> tickets = em.createQuery("SELECT q FROM QueueTicket q WHERE q.encounter = :encounter", QueueTicket.class)
                        .setParameter("encounter", encounter).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
                for (QueueTicket ticket : tickets)
                {
                    ticket.transferTo(replacement, ++nextNumber);
                    queueMessage = ". Số thứ tự mới: " + nextNumber;
                }
            }
            appointment.transferTo(target, appointment.getSlot() == null ? null : copies.get(appointment.getSlot().getSlotId()));
            NotificationDB.send(em, appointment.getPatient().getUser(), leave, "Lịch " + appointment.getAppointmentCode() +
                    " ngày " + old.getWorkDate() + " chuyển sang bác sĩ " + replacement.getFullName() +
                    ", giờ và phòng khám không đổi" + queueMessage);
        }
        leave.accept(target);
        String message = replacement.getFullName() + " đã nhận ca " + label(old);
        NotificationDB.send(em, old.getDoctor().getUser(), leave, message);
        NotificationDB.send(em, replacement.getUser(), leave, "Đã nhận ca thay " + label(target));
        notifyAdmins(em, leave, message);
    }

    private static boolean eligible(EntityManager em, Doctor doctor, DoctorSchedule shift)
    {
        if (!doctor.getSpecialty().isStatus() || doctor.getDoctorId().equals(shift.getDoctor().getDoctorId()) ||
                !doctor.getSpecialty().getSpecialtyId().equals(shift.getDoctor().getSpecialty().getSpecialtyId()) ||
                !shift.getWorkDate().atTime(shift.getStartTime()).isAfter(now())) return false;
        List<DoctorSchedule> schedules = em.createQuery("SELECT s FROM DoctorSchedule s " +
                "WHERE s.doctor = :doctor AND s.workDate = :date AND s.status <> :replaced", DoctorSchedule.class)
                .setParameter("doctor", doctor).setParameter("date", shift.getWorkDate())
                .setParameter("replaced", DoctorSchedule.Status.REPLACED).getResultList();
        // So sánh LocalTime trong Java, tránh JDBC SQL Server gửi tham số TIME dưới dạng DATETIME.
        for (DoctorSchedule schedule : schedules)
            if (schedule.getStartTime().isBefore(shift.getEndTime()) && schedule.getEndTime().isAfter(shift.getStartTime())) return false;
        return !hasLeave(em, doctor, shift.getWorkDate(), shift.getStartTime(), shift.getEndTime());
    }

    // Ca đã bàn giao vẫn là khoảng nghỉ của bác sĩ cũ, không được nhận ca khác đè lên.
    static boolean hasLeave(EntityManager em, Doctor doctor, LocalDate date, LocalTime start, LocalTime end)
    {
        List<DoctorLeaveRequest> leaves = em.createQuery("SELECT l FROM DoctorLeaveRequest l JOIN FETCH l.schedule s " +
                "WHERE s.doctor = :doctor AND s.workDate = :date AND l.status <> :rejected", DoctorLeaveRequest.class)
                .setParameter("doctor", doctor).setParameter("date", date)
                .setParameter("rejected", DoctorLeaveRequest.Status.REJECTED).getResultList();
        for (DoctorLeaveRequest leave : leaves)
            if (leave.getSchedule().getStartTime().isBefore(end) && leave.getSchedule().getEndTime().isAfter(start)) return true;
        return false;
    }

    private static void requireUnstarted(EntityManager em, DoctorSchedule schedule)
    {
        Long records = em.createQuery("SELECT COUNT(r.recordId) FROM MedicalRecord r " +
                "WHERE r.appointment.schedule = :schedule", Long.class).setParameter("schedule", schedule).getSingleResult();
        Long encounters = em.createQuery("SELECT COUNT(e.encounterId) FROM MedicalEncounter e " +
                "WHERE e.appointment.schedule = :schedule AND (e.startedAt IS NOT NULL OR e.status NOT IN (:statuses))", Long.class)
                .setParameter("schedule", schedule).setParameter("statuses", Arrays.asList(MedicalEncounter.Status.WAITING_ASSESSMENT,
                        MedicalEncounter.Status.ASSESSING, MedicalEncounter.Status.WAITING_DOCTOR)).getSingleResult();
        Long diagnostics = em.createQuery("SELECT COUNT(i.orderItemId) FROM DiagnosticOrderItem i " +
                "WHERE i.assignedDoctor = :doctor AND i.order.encounter.appointment.schedule.workDate = :date " +
                "AND i.status NOT IN (:completed, :cancelled)", Long.class)
                .setParameter("doctor", schedule.getDoctor()).setParameter("date", schedule.getWorkDate())
                .setParameter("completed", DiagnosticOrderItem.Status.COMPLETED).setParameter("cancelled", DiagnosticOrderItem.Status.CANCELLED).getSingleResult();
        if (records > 0 || encounters > 0 || diagnostics > 0)
            throw new IllegalStateException("Ca có lượt đã bắt đầu khám hoặc chỉ định đang thực hiện; chưa thể bàn giao tự động");
    }

    private static String label(DoctorSchedule schedule)
    {
        return schedule.getWorkDate() + " " + schedule.getStartTime() + "–" + schedule.getEndTime();
    }

    private static void notifyAdmins(EntityManager em, DoctorLeaveRequest leave, String message)
    {
        List<User> admins = em.createQuery("SELECT a.user FROM Account a WHERE a.role = :role AND a.status = :active", User.class)
                .setParameter("role", Account.Role.ADMIN).setParameter("active", Account.Status.ACTIVE).getResultList();
        for (User user : admins) NotificationDB.send(em, user, leave, message);
    }
}
