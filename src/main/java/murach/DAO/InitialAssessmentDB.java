package murach.DAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import murach.business.Appointment;
import murach.business.Account;
import murach.business.InitialAssessment;
import murach.business.MedicalEncounter;
import murach.business.Nurse;
import murach.util.JPAUtil;

public class InitialAssessmentDB
{
    // Tải trước các quan hệ cần hiển thị sau khi EntityManager đóng.
    private static final String FETCH_ENCOUNTER =
            "SELECT e FROM MedicalEncounter e "
                    + "JOIN FETCH e.appointment a "
                    + "JOIN FETCH a.patient p "
                    + "JOIN FETCH p.user "
                    + "JOIN FETCH a.schedule "
                    + "LEFT JOIN FETCH a.slot "
                    + "JOIN FETCH e.doctor d "
                    + "JOIN FETCH d.user "
                    + "JOIN FETCH e.specialty ";

    public List<MedicalEncounter> findWaiting(
            Long nurseId, Long userId, LocalDate workDate)
    {
        if (workDate == null)
            throw new IllegalArgumentException("Ngày khám không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            requireActiveNurse(em, nurseId, userId);
            if (!ClinicalAvailabilityDB.isNurseScheduled(em, nurseId, workDate)) return Collections.emptyList();

            // Mọi điều dưỡng thấy lượt chưa có người nhận, không phân chuyên khoa.
            // Lượt đang đánh giá chỉ hiển thị cho điều dưỡng đã nhận phiếu.
            String jpql = FETCH_ENCOUNTER
                    + "WHERE a.status = :confirmed "
                    + "AND e.checkedInAt >= :fromDateTime "
                    + "AND e.checkedInAt < :toDateTime "
                    + "AND ("
                    + "  (e.status = :waiting AND NOT EXISTS ("
                    + "      SELECT ia.assessmentId FROM InitialAssessment ia "
                    + "      WHERE ia.encounter = e"
                    + "  )) "
                    + "  OR "
                    + "  (e.status = :assessing AND EXISTS ("
                    + "      SELECT ia.assessmentId FROM InitialAssessment ia "
                    + "      WHERE ia.encounter = e "
                    + "      AND ia.nurse.nurseId = :nurseId "
                    + "      AND ia.completed = false"
                    + "  ))"
                    + ") "
                    + "ORDER BY e.checkedInAt ASC, e.encounterId ASC";

            return em.createQuery(jpql, MedicalEncounter.class)
                    .setParameter("confirmed", Appointment.Status.CONFIRMED)
                    .setParameter(
                            "fromDateTime", workDate.atStartOfDay())
                    .setParameter(
                            "toDateTime",
                            workDate.plusDays(1).atStartOfDay())
                    .setParameter(
                            "waiting",
                            MedicalEncounter.Status.WAITING_ASSESSMENT)
                    .setParameter(
                            "assessing",
                            MedicalEncounter.Status.ASSESSING)
                    .setParameter("nurseId", nurseId)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public MedicalEncounter findEncounterForNurse(
            Long encounterId, Long nurseId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            Nurse nurse = requireActiveNurse(em, nurseId, userId);

            return requireAccessibleEncounter(em, encounterId, nurse);
        }
        finally
        {
            em.close();
        }
    }

    public InitialAssessment findAssessmentForNurse(
            Long encounterId, Long nurseId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            Nurse nurse = requireActiveNurse(em, nurseId, userId);

            MedicalEncounter encounter =
                    requireAccessibleEncounter(em, encounterId, nurse);

            return findAssessment(em, encounter);
        }
        finally
        {
            em.close();
        }
    }

    public void startAssessment(
            Long encounterId, Long nurseId, Long userId)
    {
        if (encounterId == null || encounterId <= 0)
            throw new IllegalArgumentException("Mã lượt khám không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            // Xác minh điều dưỡng thuộc tài khoản NURSE đang hoạt động.
            Nurse nurse = requireActiveNurse(em, nurseId, userId);

            // Khóa lượt trước khi kiểm tra trạng thái và phiếu.
            // Hai điều dưỡng cùng nhận một lượt phải chờ nhau tại đây.
            MedicalEncounter lockedEncounter = em.find(
                    MedicalEncounter.class,
                    encounterId,
                    LockModeType.PESSIMISTIC_WRITE);

            if (lockedEncounter == null)
            {
                throw new IllegalArgumentException(
                        "Lượt khám không tồn tại");
            }

            // Kiểm tra lại trạng thái và người phụ trách sau khi khóa.
            MedicalEncounter encounter =
                    requireAccessibleEncounter(em, encounterId, nurse);

            if (encounter.getAppointment().getStatus()
                    != Appointment.Status.CONFIRMED)
            {
                throw new IllegalStateException(
                        "Lịch hẹn không còn đủ điều kiện đánh giá");
            }

            InitialAssessment assessment = findAssessment(em, encounter);

            // Chính điều dưỡng gửi lại yêu cầu: giữ nguyên phiếu hiện có.
            // requireAccessibleEncounter đã kiểm tra chủ phiếu và completed.
            if (assessment != null)
            {
                transaction.commit();
                return;
            }

            if (encounter.getStatus()
                    != MedicalEncounter.Status.WAITING_ASSESSMENT)
            {
                throw new IllegalStateException(
                        "Lượt khám không ở trạng thái chờ đánh giá");
            }

            assessment = new InitialAssessment(encounter, nurse);

            encounter.beginAssessment();
            em.persist(assessment);

            // encounter đang được quản lý nên không cần gọi merge().
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public void saveAssessment(
            Long encounterId, Long nurseId, Long userId,
            String chiefComplaint, Double height, Double weight,
            Double temperature, Integer systolicPressure,
            Integer diastolicPressure, Integer heartRate,
            Integer respiratoryRate, Double spo2,
            String note, boolean complete)
    {
        if (encounterId == null || encounterId <= 0)
            throw new IllegalArgumentException("Mã lượt khám không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            // Xác minh hồ sơ và tài khoản điều dưỡng trong database.
            Nurse nurse = requireActiveNurse(em, nurseId, userId);

            // Luôn khóa lượt trước, rồi mới khóa phiếu.
            MedicalEncounter lockedEncounter = em.find(
                    MedicalEncounter.class,
                    encounterId,
                    LockModeType.PESSIMISTIC_WRITE);

            if (lockedEncounter == null)
            {
                throw new IllegalArgumentException(
                        "Lượt khám không tồn tại");
            }

            // Kiểm tra trạng thái và người phụ trách sau khi khóa.
            MedicalEncounter encounter =
                    requireAccessibleEncounter(em, encounterId, nurse);

            if (encounter.getAppointment().getStatus()
                    != Appointment.Status.CONFIRMED)
            {
                throw new IllegalStateException(
                        "Lịch hẹn không còn đủ điều kiện đánh giá");
            }

            if (encounter.getStatus()
                    != MedicalEncounter.Status.ASSESSING)
            {
                throw new IllegalStateException(
                        "Bạn phải nhận bệnh nhân trước khi lưu đánh giá");
            }

            List<InitialAssessment> assessments = em.createQuery(
                            "SELECT ia FROM InitialAssessment ia "
                                    + "WHERE ia.encounter = :encounter",
                            InitialAssessment.class)
                    .setParameter("encounter", encounter)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();

            if (assessments.isEmpty())
            {
                throw new IllegalStateException(
                        "Chưa có phiếu đánh giá cho lượt khám");
            }

            InitialAssessment assessment = assessments.get(0);

            if (!nurse.getNurseId().equals(
                    assessment.getNurse().getNurseId()))
            {
                throw new IllegalStateException(
                        "Phiếu đánh giá thuộc điều dưỡng khác");
            }

            if (assessment.isCompleted())
            {
                throw new IllegalStateException(
                        "Phiếu đánh giá đã hoàn tất, không thể sửa");
            }

            // Entity chịu trách nhiệm kiểm tra dữ liệu sinh hiệu.
            assessment.update(
                    chiefComplaint,
                    height,
                    weight,
                    temperature,
                    systolicPressure,
                    diastolicPressure,
                    heartRate,
                    respiratoryRate,
                    spo2,
                    note);

            if (complete)
            {
                assessment.complete();
                encounter.finishAssessment(assessment);
            }

            // Hai entity đang được quản lý, không cần gọi merge().
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    private Nurse requireActiveNurse(
            EntityManager em, Long nurseId, Long userId)
    {
        if (nurseId == null || nurseId <= 0
                || userId == null || userId <= 0)
        {
            throw new IllegalArgumentException(
                    "Thông tin điều dưỡng không hợp lệ");
        }

        List<Nurse> nurses = em.createQuery(
                        "SELECT n FROM Nurse n "
                                + "JOIN FETCH n.user "
                                + "WHERE n.nurseId = :nurseId "
                                + "AND n.user.userId = :userId",
                        Nurse.class)
                .setParameter("nurseId", nurseId)
                .setParameter("userId", userId)
                .getResultList();

        if (nurses.isEmpty())
        {
            throw new SecurityException(
                    "Hồ sơ điều dưỡng không thuộc tài khoản đăng nhập");
        }

        Long active = em.createQuery(
                        "SELECT COUNT(a.accountId) FROM Account a "
                                + "WHERE a.user.userId = :userId "
                                + "AND a.role = :role "
                                + "AND a.status = :status",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("role", Account.Role.NURSE)
                .setParameter("status", Account.Status.ACTIVE)
                .getSingleResult();

        if (active == 0)
        {
            throw new SecurityException(
                    "Tài khoản điều dưỡng không còn hoạt động");
        }

        return nurses.get(0);
    }

    private MedicalEncounter requireAccessibleEncounter(
            EntityManager em, Long encounterId, Nurse nurse)
    {
        if (!ClinicalAvailabilityDB.isNurseScheduled(em, nurse.getNurseId(), ClinicalAvailabilityDB.today()))
            throw new IllegalStateException("Điều dưỡng chưa có lịch làm việc hôm nay");
        if (encounterId == null || encounterId <= 0)
            throw new IllegalArgumentException("Mã lượt khám không hợp lệ");

        List<MedicalEncounter> encounters = em.createQuery(
                        FETCH_ENCOUNTER
                                + "WHERE e.encounterId = :encounterId "
                                + "AND a.status = :confirmed",
                        MedicalEncounter.class)
                .setParameter("encounterId", encounterId)
                .setParameter("confirmed", Appointment.Status.CONFIRMED)
                .getResultList();

        if (encounters.isEmpty())
        {
            throw new SecurityException(
                    "Lượt khám không tồn tại hoặc không còn đủ điều kiện đánh giá");
        }

        MedicalEncounter encounter = encounters.get(0);

        if (encounter.getStatus()
                != MedicalEncounter.Status.WAITING_ASSESSMENT
                && encounter.getStatus()
                != MedicalEncounter.Status.ASSESSING)
        {
            throw new IllegalStateException(
                    "Lượt khám không còn ở bước đánh giá ban đầu");
        }

        InitialAssessment assessment = findAssessment(em, encounter);

        if (assessment != null)
        {
            if (!nurse.getNurseId().equals(
                    assessment.getNurse().getNurseId()))
            {
                throw new IllegalStateException(
                        "Bệnh nhân đã được điều dưỡng khác tiếp nhận");
            }

            if (assessment.isCompleted())
            {
                throw new IllegalStateException(
                        "Phiếu đánh giá đã hoàn tất");
            }
        }

        if (encounter.getStatus()
                == MedicalEncounter.Status.ASSESSING
                && assessment == null)
        {
            throw new IllegalStateException(
                    "Lượt đang đánh giá nhưng chưa có phiếu đánh giá");
        }

        if (encounter.getStatus()
                == MedicalEncounter.Status.WAITING_ASSESSMENT
                && assessment != null)
        {
            throw new IllegalStateException(
                    "Lượt đang chờ nhưng đã có phiếu; cần kiểm tra dữ liệu");
        }

        return encounter;
    }

    private InitialAssessment findAssessment(
            EntityManager em, MedicalEncounter encounter)
    {
        List<InitialAssessment> assessments = em.createQuery(
                        "SELECT ia FROM InitialAssessment ia "
                                + "JOIN FETCH ia.nurse n "
                                + "JOIN FETCH n.user "
                                + "WHERE ia.encounter = :encounter",
                        InitialAssessment.class)
                .setParameter("encounter", encounter)
                .getResultList();

        return assessments.isEmpty() ? null : assessments.get(0);
    }
}
