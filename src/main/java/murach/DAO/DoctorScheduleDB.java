package murach.DAO;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import murach.business.Doctor;
import murach.business.DoctorSchedule;
import murach.business.Appointment;
import murach.business.AppointmentSlot;
import murach.util.JPAUtil;
import murach.business.Specialty;

public final class DoctorScheduleDB
{
    private static final int DEFAULT_MAX_PATIENTS = 4;

    public static final class SlotInfo
    {
        private final AppointmentSlot slot;
        private final long bookedCount;

        public SlotInfo(AppointmentSlot slot, long bookedCount)
        {
            this.slot = slot;
            this.bookedCount = bookedCount;
        }

        public AppointmentSlot getSlot() { return slot; }
        public long getBookedCount() { return bookedCount; }
        public long getRemainingCount() { return slot.getMaxPatients() - bookedCount; }
        public boolean isWaitingReplacement() { return slot.getSchedule().getStatus() == DoctorSchedule.Status.ON_LEAVE; }
    }
    public static final class RecurringScheduleResult
    {
        private final int createdCount;
        private final int skippedCount;

        public RecurringScheduleResult(int createdCount, int skippedCount)
        {
            this.createdCount = createdCount;
            this.skippedCount = skippedCount;
        }

        public int getCreatedCount() { return createdCount; }
        public int getSkippedCount() { return skippedCount; }
    }

    private DoctorScheduleDB()
    {
    }

    public static DoctorSchedule insert(
            Long doctorId,
            LocalDate workDate,
            LocalTime startTime,
            LocalTime endTime,
            String room
    )
    {
        EntityManager em = JPAUtil.getEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            Doctor doctor = em.find(Doctor.class, doctorId, LockModeType.PESSIMISTIC_WRITE);

            if (doctor == null)
            {
                throw new IllegalArgumentException("Bác sĩ không tồn tại");
            }

            DoctorSchedule schedule = new DoctorSchedule(doctor, workDate, startTime, endTime, room);
            if (DoctorLeaveDB.hasLeave(em, doctor, workDate, startTime, endTime))
                throw new IllegalArgumentException("Bác sĩ đã có đơn nghỉ trong ca này");
            List<DoctorSchedule> existing = em.createQuery("SELECT s FROM DoctorSchedule s " +
                    "WHERE s.doctor = :doctor AND s.workDate = :date AND s.status <> :replaced", DoctorSchedule.class)
                    .setParameter("doctor", doctor).setParameter("date", workDate)
                    .setParameter("replaced", DoctorSchedule.Status.REPLACED).getResultList();
            for (DoctorSchedule other : existing)
                if (other.getStartTime().isBefore(endTime) && other.getEndTime().isAfter(startTime))
                    throw new IllegalArgumentException("Ca trùng lịch làm việc đã có");

            em.persist(schedule);
            createHourlySlots(em, schedule, findSharedSlotCapacity(em, doctorId));

            transaction.commit();

            return schedule;

        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
            {
                transaction.rollback();
            }

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public static List<DoctorSchedule> findByDoctor(
            Long doctorId
    )
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            return em.createQuery(
                            "SELECT s "
                                    + "FROM DoctorSchedule s "
                                    + "WHERE s.doctor.doctorId = :doctorId "
                                    + "ORDER BY s.workDate, s.startTime",
                            DoctorSchedule.class
                    )
                    .setParameter("doctorId", doctorId)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    /** Danh sách lịch của tất cả bác sĩ dành cho trang quản trị. */
    public static List<DoctorSchedule> findAll()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                            "SELECT s FROM DoctorSchedule s "
                                    + "JOIN FETCH s.doctor d "
                                    + "JOIN FETCH d.user "
                                    + "JOIN FETCH d.specialty "
                                    + "ORDER BY s.workDate DESC, s.startTime, d.user.fullName",
                            DoctorSchedule.class)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    /** Tạo lịch lặp theo tuần; ca bị chồng giờ được bỏ qua. */
    public static RecurringScheduleResult createRecurringSchedules(
            Long doctorId,
            LocalDate fromDate,
            LocalDate toDate,
            Set<DayOfWeek> weekdays,
            boolean morning,
            boolean afternoon,
            String room)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();
            Doctor doctor = em.find(Doctor.class, doctorId, LockModeType.PESSIMISTIC_WRITE);
            if (doctor == null)
                throw new IllegalArgumentException("Bác sĩ không tồn tại");

            List<DoctorSchedule> existing = em.createQuery(
                            "SELECT s FROM DoctorSchedule s "
                                    + "WHERE s.doctor.doctorId = :doctorId "
                                    + "AND s.workDate BETWEEN :fromDate AND :toDate",
                            DoctorSchedule.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getResultList();

            int created = 0;
            int skipped = 0;
            LocalDate current = fromDate;

            while (!current.isAfter(toDate))
            {
                if (weekdays.contains(current.getDayOfWeek()))
                {
                    if (morning)
                    {
                        if (addIfNotOverlapping(em, existing, doctor, current, LocalTime.of(7, 0), LocalTime.of(11, 30), room))
                            created++;
                        else
                            skipped++;
                    }
                    if (afternoon)
                    {
                        if (addIfNotOverlapping(em, existing, doctor, current, LocalTime.of(13, 0), LocalTime.of(16, 0), room))
                            created++;
                        else
                            skipped++;
                    }
                }
                current = current.plusDays(1);
            }

            transaction.commit();
            return new RecurringScheduleResult(created, skipped);
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

    private static boolean addIfNotOverlapping(
            EntityManager em,
            List<DoctorSchedule> existing,
            Doctor doctor,
            LocalDate workDate,
            LocalTime startTime,
            LocalTime endTime,
            String room)
    {
        if (DoctorLeaveDB.hasLeave(em, doctor, workDate, startTime, endTime)) return false;
        for (DoctorSchedule schedule : existing)
        {
            if (schedule.getStatus() != DoctorSchedule.Status.REPLACED && schedule.getWorkDate().equals(workDate)
                    && schedule.getStartTime().isBefore(endTime)
                    && schedule.getEndTime().isAfter(startTime))
                return false;
        }

        DoctorSchedule schedule = new DoctorSchedule(
                doctor, workDate, startTime, endTime, room);
        em.persist(schedule);
        createHourlySlots(em, schedule, findSharedSlotCapacity(em, doctor.getDoctorId()));
        existing.add(schedule);
        return true;
    }

    /** Chỉ sinh các khung đủ 60 phút; phần thời gian lẻ cuối ca không mở đặt khám. */
    private static void createHourlySlots(EntityManager em, DoctorSchedule schedule, int maxPatients)
    {
        LocalTime start = schedule.getStartTime();
        /**thời gian hiện tại + 1 tiếng mà ko vượt quá giờ kết thúc của lịch làm việc, thì tiếp tục tạo slot*/
        while (!start.plusHours(1).isAfter(schedule.getEndTime()))
        {
            LocalTime end = start.plusHours(1);
            em.persist(new AppointmentSlot(schedule, start, end, maxPatients));
            start = end;
        }
    }

    /** Bác sĩ thuộc chuyên khoa và còn ít nhất một ca trống trong ngày đã chọn. */
    public static List<Doctor> findAvailableDoctors(
            Long specialtyId,
            LocalDate workDate
    )
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            return em.createQuery(
                            "SELECT DISTINCT d FROM AppointmentSlot sl "
                                    + "JOIN sl.schedule s JOIN s.doctor d "
                                    + "JOIN FETCH d.user "
                                    + "JOIN FETCH d.specialty "
                                    + "WHERE s.workDate = :workDate "
                                    + "AND s.status <> :replaced "
                                    + "AND d.specialty.specialtyId = :specialtyId "
                                    + "AND d.specialty.status = true "
                                    + "AND d.specialty.specialtyType = :clinicalType "
                                    + "AND sl.status = :openStatus "
                                    + "AND (SELECT COUNT(a.appointmentId) FROM Appointment a "
                                    + "     WHERE a.slot = sl AND a.status <> :cancelled) < sl.maxPatients "
                                    + "ORDER BY d.user.fullName",
                            Doctor.class
                    )
                    .setParameter("workDate", workDate)
                    .setParameter("specialtyId", specialtyId)
                    .setParameter("replaced", DoctorSchedule.Status.REPLACED)
                    .setParameter("openStatus", AppointmentSlot.Status.OPEN)
                    .setParameter("cancelled", Appointment.Status.CANCELLED)
                    .setParameter("clinicalType", Specialty.SpecialtyType.CLINICAL)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    /** Các khung một giờ của bác sĩ trong ngày; slot đầy vẫn được trả về để giao diện khóa lựa chọn. */
    public static List<SlotInfo> findAvailableSlots(
            Long doctorId,
            LocalDate workDate
    )
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            List<AppointmentSlot> slots = em.createQuery(
                            "SELECT sl FROM AppointmentSlot sl "
                                    + "JOIN FETCH sl.schedule s "
                                    + "WHERE s.doctor.doctorId = :doctorId AND s.workDate = :workDate "
                                    + "AND s.status <> :replaced "
                                    + "AND s.doctor.specialty.status = true "
                                    + "AND s.doctor.specialty.specialtyType = :clinicalType "
                                    + "AND sl.status = :openStatus "
                                    + "ORDER BY sl.startTime",
                            AppointmentSlot.class
                    )
                    .setParameter("doctorId", doctorId)
                    .setParameter("workDate", workDate)
                    .setParameter("replaced", DoctorSchedule.Status.REPLACED)
                    .setParameter("openStatus", AppointmentSlot.Status.OPEN)
                    .setParameter("clinicalType", Specialty.SpecialtyType.CLINICAL)
                    .getResultList();

            List<SlotInfo> result = new java.util.ArrayList<>();
            for (AppointmentSlot slot : slots)
            {
                long booked = countActiveAppointments(em, slot.getSlotId());
                result.add(new SlotInfo(slot, booked));
            }
            return result;
        }
        finally
        {
            em.close();
        }
    }

    /** Danh sách slot để bác sĩ xem và điều chỉnh giới hạn của chính mình. */
    public static List<SlotInfo> findSlotsByDoctor(Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<AppointmentSlot> slots = em.createQuery(
                            "SELECT sl FROM AppointmentSlot sl JOIN FETCH sl.schedule s "
                                    + "WHERE s.doctor.doctorId = :doctorId "
                                    + "ORDER BY s.workDate, sl.startTime",
                            AppointmentSlot.class)
                    .setParameter("doctorId", doctorId)
                    .getResultList();
            List<SlotInfo> result = new java.util.ArrayList<>();
            for (AppointmentSlot slot : slots)
                result.add(new SlotInfo(slot, countActiveAppointments(em, slot.getSlotId())));
            return result;
        }
        finally
        {
            em.close();
        }
    }

    /** Một giới hạn dùng chung cho tất cả khung giờ của bác sĩ. */
    public static int findSharedSlotCapacity(Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return findSharedSlotCapacity(em, doctorId);
        }
        finally
        {
            em.close();
        }
    }

    private static int findSharedSlotCapacity(EntityManager em, Long doctorId)
    {
        List<Integer> capacities = em.createQuery(
                        "SELECT sl.maxPatients FROM AppointmentSlot sl "
                                + "WHERE sl.schedule.doctor.doctorId = :doctorId "
                                + "ORDER BY sl.slotId DESC",
                        Integer.class)
                .setParameter("doctorId", doctorId)
                .setMaxResults(1)
                .getResultList();
        return capacities.isEmpty() ? DEFAULT_MAX_PATIENTS : capacities.get(0);
    }

    /** Cập nhật cùng một giới hạn cho toàn bộ khung giờ hiện có của bác sĩ. */
    public static void updateSharedSlotCapacity(Long doctorId, int maxPatients)
    {
        if (maxPatients < 1)
            throw new IllegalArgumentException("Số bệnh nhân tối đa phải lớn hơn 0");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DoctorLeaveDB.lockDoctor(em, doctorId);
            List<AppointmentSlot> slots = em.createQuery(
                            "SELECT sl FROM AppointmentSlot sl "
                                    + "WHERE sl.schedule.doctor.doctorId = :doctorId",
                            AppointmentSlot.class)
                    .setParameter("doctorId", doctorId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();

            for (AppointmentSlot slot : slots)
            {
                long booked = countActiveAppointments(em, slot.getSlotId());
                if (maxPatients < booked)
                    throw new IllegalArgumentException(
                            "Giới hạn không được nhỏ hơn số bệnh nhân đã đặt trong một khung giờ: " + booked);
            }
            for (AppointmentSlot slot : slots)
                slot.changeMaxPatients(maxPatients);

            transaction.commit();
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

    public static void updateSlotCapacity(Long doctorId, Long slotId, int maxPatients)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DoctorLeaveDB.lockDoctor(em, doctorId);
            AppointmentSlot slot = em.find(AppointmentSlot.class, slotId, LockModeType.PESSIMISTIC_WRITE);
            if (slot == null || !slot.getSchedule().getDoctor().getDoctorId().equals(doctorId))
                throw new IllegalArgumentException("Khung giờ không thuộc lịch của bác sĩ đang đăng nhập");
            long booked = countActiveAppointments(em, slotId);
            if (maxPatients < booked)
                throw new IllegalArgumentException("Giới hạn không được nhỏ hơn số bệnh nhân đã đặt: " + booked);
            slot.changeMaxPatients(maxPatients);
            transaction.commit();
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

    private static long countActiveAppointments(EntityManager em, Long slotId)
    {
        return em.createQuery(
                        "SELECT COUNT(a.appointmentId) FROM Appointment a "
                                + "WHERE a.slot.slotId = :slotId AND a.status <> :cancelled",
                        Long.class)
                .setParameter("slotId", slotId)
                .setParameter("cancelled", Appointment.Status.CANCELLED)
                .getSingleResult();
    }
}
