package murach.DAO;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

/** Admin phân lịch; tiếp tân, điều dưỡng và chuyên viên chỉ xem lịch của mình. */
public class StaffScheduleDB
{
    public List<Staff> findReceptionists()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT s FROM Staff s JOIN FETCH s.user u WHERE s.jobType = :jobType AND EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = u AND a.role = :role AND a.status = :active) " +
                    "ORDER BY u.fullName", Staff.class)
                    .setParameter("jobType", Staff.JobType.RECEPTIONIST)
                    .setParameter("role", Account.Role.STAFF).setParameter("active", Account.Status.ACTIVE).getResultList();
        }
        finally { em.close(); }
    }

    public List<StaffSchedule> findReceptionistSchedules(Long profileId, Long ownerUserId, LocalDate fromDate, LocalDate toDate)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            // Trang cá nhân phải khớp hồ sơ thật, không chỉ dựa vào role STAFF trong session.
            if (ownerUserId != null) ClinicalAccess.receptionist(em, profileId, ownerUserId);
            return em.createQuery("SELECT s FROM StaffSchedule s JOIN FETCH s.staff st JOIN FETCH st.user u " +
                    "WHERE st.jobType = :jobType AND s.workDate BETWEEN :fromDate AND :toDate " +
                    "AND (:profileId IS NULL OR st.staffId = :profileId) " +
                    "AND (:ownerUserId IS NULL OR u.userId = :ownerUserId) ORDER BY s.workDate, s.startTime, u.fullName", StaffSchedule.class)
                    .setParameter("jobType", Staff.JobType.RECEPTIONIST)
                    .setParameter("fromDate", fromDate).setParameter("toDate", toDate)
                    .setParameter("profileId", profileId).setParameter("ownerUserId", ownerUserId).getResultList();
        }
        finally { em.close(); }
    }

    public List<Nurse> findNurses()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT n FROM Nurse n JOIN FETCH n.user u WHERE EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = u AND a.role = :role AND a.status = :active) " +
                    "ORDER BY u.fullName", Nurse.class)
                    .setParameter("role", Account.Role.NURSE).setParameter("active", Account.Status.ACTIVE).getResultList();
        }
        finally { em.close(); }
    }

    public List<Technician> findTechnicians()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT t FROM Technician t JOIN FETCH t.user u JOIN FETCH t.specialty sp " +
                    "WHERE sp.specialtyType = :type AND EXISTS (SELECT a.accountId FROM Account a " +
                    "WHERE a.user = u AND a.role = :role AND a.status = :active) ORDER BY u.fullName", Technician.class)
                    .setParameter("type", Specialty.SpecialtyType.PARACLINICAL)
                    .setParameter("role", Account.Role.TECHNICIAN).setParameter("active", Account.Status.ACTIVE).getResultList();
        }
        finally { em.close(); }
    }

    public List<NurseSchedule> findNurseSchedules(Long profileId, Long ownerUserId, LocalDate fromDate, LocalDate toDate)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT s FROM NurseSchedule s JOIN FETCH s.nurse n JOIN FETCH n.user u " +
                    "WHERE s.workDate BETWEEN :fromDate AND :toDate " +
                    "AND (:profileId IS NULL OR n.nurseId = :profileId) " +
                    "AND (:ownerUserId IS NULL OR u.userId = :ownerUserId) ORDER BY s.workDate, s.startTime, u.fullName", NurseSchedule.class)
                    .setParameter("fromDate", fromDate).setParameter("toDate", toDate)
                    .setParameter("profileId", profileId).setParameter("ownerUserId", ownerUserId).getResultList();
        }
        finally { em.close(); }
    }

    public List<TechnicianSchedule> findTechnicianSchedules(Long profileId, Long ownerUserId, LocalDate fromDate, LocalDate toDate)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT s FROM TechnicianSchedule s JOIN FETCH s.technician t JOIN FETCH t.user u " +
                    "JOIN FETCH t.specialty WHERE s.workDate BETWEEN :fromDate AND :toDate " +
                    "AND (:profileId IS NULL OR t.technicianId = :profileId) " +
                    "AND (:ownerUserId IS NULL OR u.userId = :ownerUserId) ORDER BY s.workDate, s.startTime, u.fullName", TechnicianSchedule.class)
                    .setParameter("fromDate", fromDate).setParameter("toDate", toDate)
                    .setParameter("profileId", profileId).setParameter("ownerUserId", ownerUserId).getResultList();
        }
        finally { em.close(); }
    }

    public Map<String, Integer> createRecurring(Account.Role role, Long profileId, LocalDate fromDate, LocalDate toDate,
            Set<DayOfWeek> weekdays, boolean morning, boolean afternoon, String room, Long actorUserId)
    {
        if (role != Account.Role.NURSE && role != Account.Role.TECHNICIAN && role != Account.Role.STAFF)
            throw new IllegalArgumentException("Loại nhân sự không hợp lệ");
        if (profileId == null || profileId <= 0 || fromDate == null || toDate == null || toDate.isBefore(fromDate)
                || ChronoUnit.DAYS.between(fromDate, toDate) > 366)
            throw new IllegalArgumentException("Chọn nhân sự và khoảng ngày hợp lệ, tối đa 12 tháng");
        if (weekdays == null || weekdays.isEmpty() || (!morning && !afternoon))
            throw new IllegalArgumentException("Chọn ít nhất một thứ và một ca làm việc");
        String checkedRoom = room == null ? "" : room.trim();
        if (checkedRoom.isEmpty() || checkedRoom.length() > 100)
            throw new IllegalArgumentException("Phòng làm việc từ 1 đến 100 ký tự");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            ClinicalAccess.requireUser(em, actorUserId, Account.Role.ADMIN);
            Nurse nurse = null;
            Technician technician = null;
            Staff receptionist = null;
            List<LocalDateTime[]> occupied = new ArrayList<>();
            // Khóa hồ sơ nhân sự để hai yêu cầu phân lịch không tạo ca trùng nhau.
            if (role == Account.Role.NURSE)
            {
                nurse = em.find(Nurse.class, profileId, LockModeType.PESSIMISTIC_WRITE);
                if (nurse == null) throw new IllegalArgumentException("Điều dưỡng không tồn tại");
                ClinicalAccess.requireUser(em, nurse.getUserId(), role);
                for (NurseSchedule s : em.createQuery("SELECT s FROM NurseSchedule s WHERE s.nurse = :nurse " +
                        "AND s.workDate BETWEEN :fromDate AND :toDate", NurseSchedule.class)
                        .setParameter("nurse", nurse).setParameter("fromDate", fromDate).setParameter("toDate", toDate).getResultList())
                    occupied.add(new LocalDateTime[]{s.getWorkDate().atTime(s.getStartTime()), s.getWorkDate().atTime(s.getEndTime())});
            }
            else if (role == Account.Role.TECHNICIAN)
            {
                technician = em.find(Technician.class, profileId, LockModeType.PESSIMISTIC_WRITE);
                if (technician == null || technician.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                    throw new IllegalArgumentException("Chuyên viên không tồn tại hoặc không thuộc khoa cận lâm sàng");
                ClinicalAccess.requireUser(em, technician.getUserId(), role);
                for (TechnicianSchedule s : em.createQuery("SELECT s FROM TechnicianSchedule s WHERE s.technician = :technician " +
                        "AND s.workDate BETWEEN :fromDate AND :toDate", TechnicianSchedule.class)
                        .setParameter("technician", technician).setParameter("fromDate", fromDate).setParameter("toDate", toDate).getResultList())
                    occupied.add(new LocalDateTime[]{s.getWorkDate().atTime(s.getStartTime()), s.getWorkDate().atTime(s.getEndTime())});
            }
            else
            {
                receptionist = em.find(Staff.class, profileId, LockModeType.PESSIMISTIC_WRITE);
                if (receptionist == null || receptionist.getJobType() != Staff.JobType.RECEPTIONIST)
                    throw new IllegalArgumentException("Hồ sơ được chọn không phải tiếp tân");
                ClinicalAccess.requireUser(em, receptionist.getUserId(), role);
                for (StaffSchedule s : em.createQuery("SELECT s FROM StaffSchedule s WHERE s.staff = :staff " +
                        "AND s.workDate BETWEEN :fromDate AND :toDate", StaffSchedule.class)
                        .setParameter("staff", receptionist).setParameter("fromDate", fromDate).setParameter("toDate", toDate).getResultList())
                    occupied.add(new LocalDateTime[]{s.getWorkDate().atTime(s.getStartTime()), s.getWorkDate().atTime(s.getEndTime())});
            }

            int created = 0, skipped = 0;
            for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1))
            {
                if (!weekdays.contains(day.getDayOfWeek())) continue;
                for (int shift = 0; shift < 2; shift++)
                {
                    if ((shift == 0 && !morning) || (shift == 1 && !afternoon)) continue;
                    LocalTime start = shift == 0 ? LocalTime.of(7, 0) : LocalTime.of(13, 0);
                    LocalTime end = shift == 0 ? LocalTime.of(11, 30) : LocalTime.of(16, 0);
                    LocalDateTime startAt = day.atTime(start), endAt = day.atTime(end);
                    boolean overlapping = false;
                    for (LocalDateTime[] range : occupied)
                        if (range[0].isBefore(endAt) && range[1].isAfter(startAt)) { overlapping = true; break; }
                    if (overlapping) { skipped++; continue; }
                    if (role == Account.Role.NURSE) em.persist(new NurseSchedule(nurse, day, start, end, checkedRoom));
                    else if (role == Account.Role.TECHNICIAN) em.persist(new TechnicianSchedule(technician, day, start, end, checkedRoom));
                    else em.persist(new StaffSchedule(receptionist, day, start, end, checkedRoom));
                    occupied.add(new LocalDateTime[]{startAt, endAt});
                    created++;
                }
            }
            transaction.commit();
            Map<String, Integer> result = new LinkedHashMap<>();
            result.put("created", created); result.put("skipped", skipped);
            return result;
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }
}
