package murach.DAO;

import murach.business.MedicalRecord;
import murach.business.Appointment;
import murach.util.JPAUtil;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

import java.util.Arrays;
import murach.business.InitialAssessment;
import murach.business.MedicalEncounter;
import murach.business.QueueTicket;

public final class MedicalRecordDB
{
    /** Hồ sơ đã hoàn thành của những bệnh nhân từng khám với bác sĩ. */
    public List<MedicalRecord> findCompletedByDoctorId(Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                            "SELECT r FROM MedicalRecord r " +
                                    "LEFT JOIN FETCH r.encounter " +
                                    "JOIN FETCH r.appointment a " +
                                    "JOIN FETCH a.patient p " +
                                    "JOIN FETCH p.user " +
                                    "JOIN FETCH a.schedule s " +
                                    "JOIN s.doctor d " +
                                    "LEFT JOIN FETCH a.slot " +
                                    "WHERE d.doctorId = :doctorId " +
                                    "AND (r.status = :finalized OR (r.encounter IS NULL AND a.status = :completed)) " +
                                    "ORDER BY s.workDate DESC, s.startTime DESC",
                            MedicalRecord.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("completed", Appointment.Status.COMPLETED)
                    .setParameter("finalized", MedicalRecord.Status.FINALIZED)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public void completeExamination(Long appointmentId, Long doctorId,
                                    String diagnosis, String clinicalNote,
                                    String treatment, String prescription)
    {
        if (appointmentId == null || doctorId == null)
            throw new IllegalArgumentException("Thông tin buổi khám không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();

            DoctorLeaveDB.lockDoctor(em, doctorId);
            Appointment appointment;
            try
            {
                appointment = em.createQuery(
                                "SELECT a FROM Appointment a " +
                                        "JOIN a.schedule s JOIN s.doctor d " +
                                        "WHERE a.appointmentId = :appointmentId " +
                                        "AND d.doctorId = :doctorId",
                                Appointment.class)
                        .setParameter("appointmentId", appointmentId)
                        .setParameter("doctorId", doctorId)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .getSingleResult();
            }
            catch (NoResultException e)
            {
                throw new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc bác sĩ này");
            }

            if (appointment.getSchedule().getStatus() != murach.business.DoctorSchedule.Status.ACTIVE)
                throw new IllegalStateException("Ca đã được duyệt nghỉ hoặc đã chuyển bác sĩ");
            if (appointment.getStatus() == Appointment.Status.CANCELLED)
                throw new IllegalArgumentException("Không thể khám một lịch hẹn đã hủy");
            if (appointment.getStatus() == Appointment.Status.COMPLETED)
                throw new IllegalArgumentException("Lịch hẹn này đã hoàn thành");

            long existingRecord = em.createQuery(
                            "SELECT COUNT(r.recordId) FROM MedicalRecord r " +
                                    "WHERE r.appointment = :appointment", Long.class)
                    .setParameter("appointment", appointment)
                    .getSingleResult();
            if (existingRecord > 0)
                throw new IllegalArgumentException("Lịch hẹn đã có hồ sơ bệnh án");

            MedicalRecord record = new MedicalRecord(
                    appointment, diagnosis, clinicalNote, treatment,
                    prescription, "COMPLETED");
            em.persist(record);
            appointment.complete();
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

    public List<MedicalRecord> findByPatientId(Long patientId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM MedicalRecord r LEFT JOIN FETCH r.encounter JOIN FETCH r.appointment a JOIN FETCH a.patient p JOIN FETCH p.user" +
                    " JOIN FETCH a.schedule s JOIN FETCH s.doctor d" +
                    " JOIN FETCH d.user JOIN FETCH d.specialty " +
                    "WHERE p.patientId = :patientId AND r.status IN (:finalized, :legacy) " +
                    "ORDER BY s.workDate DESC, s.startTime DESC",
                    MedicalRecord.class)
                    .setParameter("patientId", patientId)
                    .setParameter("finalized", MedicalRecord.Status.FINALIZED)
                    .setParameter("legacy", MedicalRecord.Status.COMPLETED)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //Dương, tìm bệnh nhân theo ngày, truy vấn động
    public List<MedicalRecord> findByPatientIdAndDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT r FROM MedicalRecord r LEFT JOIN FETCH r.encounter JOIN FETCH r.appointment a JOIN FETCH a.patient p JOIN FETCH p.user" +
                            " JOIN FETCH a.schedule s JOIN FETCH s.doctor d" +
                            " JOIN FETCH d.user JOIN FETCH d.specialty " +
                            "WHERE p.patientId = :patientId AND r.status IN (:finalized, :legacy) "
            );

            // Bổ sung điều kiện lọc
            if (startDate != null) {
                jpql.append("AND s.workDate >= :startDate ");
            }
            if (endDate != null) {
                jpql.append("AND s.workDate <= :endDate ");
            }

            jpql.append("ORDER BY s.workDate DESC, s.startTime DESC");

            TypedQuery<MedicalRecord> query = em.createQuery(jpql.toString(), MedicalRecord.class);
            query.setParameter("patientId", patientId);
            query.setParameter("finalized", MedicalRecord.Status.FINALIZED);
            query.setParameter("legacy", MedicalRecord.Status.COMPLETED);

            if (startDate != null) {
                query.setParameter("startDate", startDate);
            }
            if (endDate != null) {
                query.setParameter("endDate", endDate);
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // là những  người đã được điều dưỡng xác nhận
    public List<QueueTicket> findDoctorQueue(Long doctorId, LocalDate workDate)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String jpql =
                    "SELECT q FROM QueueTicket q " +
                            "JOIN FETCH q.encounter e " +
                            "JOIN FETCH e.appointment a " +
                            "JOIN FETCH a.patient p " +
                            "JOIN FETCH p.user " +
                            "JOIN FETCH a.schedule s " +
                            "LEFT JOIN FETCH a.slot " +
                            "WHERE e.doctor.doctorId = :doctorId " +
                            "AND q.queueDate = :workDate AND s.status = :scheduleActive " +
                            "AND a.status = :confirmed " +
                            "AND e.status IN (:encounterStatuses) " +
                            "AND q.status IN (:ticketStatuses) " +
                            "AND EXISTS (SELECT ia.assessmentId FROM InitialAssessment ia " +
                            "WHERE ia.encounter = e AND ia.completed = true) " +
                            "ORDER BY q.queueNumber ASC, q.ticketId ASC";

            return em.createQuery(jpql, QueueTicket.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("workDate", workDate)
                    .setParameter("scheduleActive", murach.business.DoctorSchedule.Status.ACTIVE)
                    .setParameter("confirmed", Appointment.Status.CONFIRMED)
                    .setParameter("encounterStatuses", Arrays.asList(
                            MedicalEncounter.Status.WAITING_DOCTOR,
                            MedicalEncounter.Status.READY_FOR_REVIEW,
                            MedicalEncounter.Status.IN_CONSULTATION))
                    .setParameter("ticketStatuses", Arrays.asList(
                            QueueTicket.Status.WAITING,
                            QueueTicket.Status.CALLED,
                            QueueTicket.Status.ON_HOLD))
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    //lấy chi tiết lượt khám của bác sĩ
    public MedicalEncounter findEncounterForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<MedicalEncounter> encounters = em.createQuery(
                            "SELECT e FROM MedicalEncounter e " +
                                    "JOIN FETCH e.appointment a " +
                                    "JOIN FETCH a.patient p JOIN FETCH p.user " +
                                    "JOIN FETCH a.schedule s LEFT JOIN FETCH a.slot " +
                                    "JOIN FETCH e.doctor d JOIN FETCH d.user " +
                                    "JOIN FETCH e.specialty " +
                                    "WHERE e.encounterId = :encounterId AND d.doctorId = :doctorId " +
                                    "AND a.status IN (:confirmed, :completed)", MedicalEncounter.class)
                    .setParameter("encounterId", encounterId)
                    .setParameter("doctorId", doctorId)
                    .setParameter("confirmed", Appointment.Status.CONFIRMED)
                    .setParameter("completed", Appointment.Status.COMPLETED)
                    .getResultList();

            return encounters.isEmpty() ? null : encounters.get(0);
        }
        finally
        {
            em.close();
        }
    }

    //bác sĩ xem thông tin đánh giá ban đầu của bệnh nhân của họ
    public InitialAssessment findAssessmentForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<InitialAssessment> assessments = em.createQuery(
                            "SELECT ia FROM InitialAssessment ia " +
                                    "JOIN FETCH ia.nurse n JOIN FETCH n.user " +
                                    "JOIN ia.encounter e " +
                                    "WHERE e.encounterId = :encounterId " +
                                    "AND e.doctor.doctorId = :doctorId", InitialAssessment.class)
                    .setParameter("encounterId", encounterId)
                    .setParameter("doctorId", doctorId)
                    .getResultList();

            return assessments.isEmpty() ? null : assessments.get(0);
        }
        finally
        {
            em.close();
        }
    }

    //bắt  đầu khám
    public void beginConsultation(Long encounterId, Long doctorId)
    {
        if (encounterId == null || doctorId == null)
            throw new IllegalArgumentException("Thiếu thông tin lượt khám hoặc bác sĩ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DoctorLeaveDB.lockDoctor(em, doctorId);
            MedicalEncounter encounter = em.find(
                    MedicalEncounter.class, encounterId, LockModeType.PESSIMISTIC_WRITE);

            if (encounter == null || !doctorId.equals(encounter.getDoctor().getDoctorId()))
                throw new IllegalArgumentException("Không tìm thấy lượt khám thuộc bác sĩ này");
            if (encounter.getAppointment().getStatus() != Appointment.Status.CONFIRMED)
                throw new IllegalStateException("Lịch hẹn không còn đủ điều kiện khám");
            if (encounter.getAppointment().getSchedule().getStatus() != murach.business.DoctorSchedule.Status.ACTIVE)
                throw new IllegalStateException("Ca đã được duyệt nghỉ hoặc đã chuyển bác sĩ");

            List<InitialAssessment> assessments = em.createQuery(
                            "SELECT ia FROM InitialAssessment ia WHERE ia.encounter = :encounter",
                            InitialAssessment.class)
                    .setParameter("encounter", encounter)
                    .getResultList();
            if (assessments.isEmpty() || !assessments.get(0).isCompleted())
                throw new IllegalStateException("Chưa hoàn tất đánh giá ban đầu");

            ClinicalAccess.requireUser(em, encounter.getDoctor().getUser().getUserId(), murach.business.Account.Role.DOCTOR);

            // Nhấn lại nút bắt đầu không tạo thêm hồ sơ hay gọi số thêm lần nữa
            if (encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            {
                if (encounter.getStatus() != MedicalEncounter.Status.WAITING_DOCTOR
                        && encounter.getStatus() != MedicalEncounter.Status.READY_FOR_REVIEW)
                    throw new IllegalStateException("Lượt khám chưa sẵn sàng cho bác sĩ");

                List<QueueTicket> tickets = em.createQuery(
                                "SELECT q FROM QueueTicket q WHERE q.encounter = :encounter",
                                QueueTicket.class)
                        .setParameter("encounter", encounter)
                        .getResultList();
                if (tickets.isEmpty())
                    throw new IllegalStateException("Lượt khám chưa có số thứ tự");

                QueueTicket ticket = tickets.get(0);
                if (ticket.getStatus() != QueueTicket.Status.CALLED)
                    ticket.call();
                // call() phải chạy trước startConsultation() theo điều kiện Entity
                encounter.startConsultation(assessments.get(0));
            }

            List<MedicalRecord> records = em.createQuery(
                            "SELECT r FROM MedicalRecord r WHERE r.encounter = :encounter",
                            MedicalRecord.class)
                    .setParameter("encounter", encounter)
                    .getResultList();
            if (records.isEmpty())
                em.persist(new MedicalRecord(encounter));

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

    // Lấy hồ sơ nháp của lượt khám thuộc bác sĩ đang đăng nhập
    public MedicalRecord findDraftForDoctor(Long encounterId, Long doctorId)
    {
        if (encounterId == null || doctorId == null)
            throw new IllegalArgumentException("Thiếu thông tin lượt khám hoặc bác sĩ");

        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<MedicalRecord> records = em.createQuery(
                            "SELECT r FROM MedicalRecord r " +
                                    "JOIN r.encounter e " +
                                    "WHERE e.encounterId = :encounterId " +
                                    "AND e.doctor.doctorId = :doctorId " +
                                    "AND r.status = :draft", MedicalRecord.class)
                    .setParameter("encounterId", encounterId)
                    .setParameter("doctorId", doctorId)
                    .setParameter("draft", MedicalRecord.Status.DRAFT)
                    .getResultList();

            return records.isEmpty() ? null : records.get(0);
        }
        finally
        {
            em.close();
        }
    }

    // Lưu nháp nội dung khám, chưa xác nhận kết luận và chưa kết thúc lượt khám
    public void saveDraft(Long encounterId, Long doctorId, String diagnosis,
                          String clinicalNote, String treatment, String advice)
    {
        if (encounterId == null || doctorId == null)
            throw new IllegalArgumentException("Thiếu thông tin lượt khám hoặc bác sĩ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            //Khóa lượt khám trong lúc kiểm tra và lưu dữ liệu
            MedicalEncounter encounter = em.find(
                    MedicalEncounter.class, encounterId,
                    LockModeType.PESSIMISTIC_WRITE);

            //Chỉ cho phép bác sĩ phụ trách cập nhật lượt khám này
            if (encounter == null || !doctorId.equals(encounter.getDoctor().getDoctorId()))
                throw new IllegalArgumentException("Không tìm thấy lượt khám thuộc bác sĩ này");

            if (encounter.getAppointment().getStatus() != Appointment.Status.CONFIRMED)
                throw new IllegalStateException("Lịch hẹn không còn đủ điều kiện khám");

            if (encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
                throw new IllegalStateException("Chỉ được lưu nội dung khi đang khám");

            //Kiểm tra phiếu đánh giá ban đầu của đúng lượt khám
            List<InitialAssessment> assessments = em.createQuery(
                            "SELECT ia FROM InitialAssessment ia " +
                                    "WHERE ia.encounter = :encounter", InitialAssessment.class)
                    .setParameter("encounter", encounter)
                    .getResultList();

            if (assessments.isEmpty() || !assessments.get(0).isCompleted())
                throw new IllegalStateException("Chưa hoàn tất đánh giá ban đầu");

            //Lấy và khóa hồ sơ đã được tạo khi bác sĩ bắt đầu khám
            List<MedicalRecord> records = em.createQuery(
                            "SELECT r FROM MedicalRecord r " +
                                    "WHERE r.encounter = :encounter", MedicalRecord.class)
                    .setParameter("encounter", encounter)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();

            if (records.isEmpty())
                throw new IllegalStateException("Chưa có hồ sơ nháp, cần bắt đầu khám trước");

            MedicalRecord record = records.get(0);

            if (record.getStatus() != MedicalRecord.Status.DRAFT)
                throw new IllegalStateException("Hồ sơ đã xác nhận kết luận, không thể lưu nháp");

            ClinicalAccess.requireUser(em, encounter.getDoctor().getUser().getUserId(), murach.business.Account.Role.DOCTOR);

            //Cập nhật nội dung - trạng thái hồ sơ vẫn là DRAFT
            record.update(diagnosis, clinicalNote, treatment, advice);

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
    public InitialAssessment findAssessmentForPatient(Long encounterId, Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<InitialAssessment> items = em.createQuery("SELECT ia FROM InitialAssessment ia " +
                    "JOIN FETCH ia.nurse n JOIN FETCH n.user JOIN ia.encounter e " +
                    "WHERE e.encounterId = :id AND e.appointment.patient.patientId = :patientId AND ia.completed = true",
                    InitialAssessment.class).setParameter("id", encounterId).setParameter("patientId", patientId).getResultList();
            return items.isEmpty() ? null : items.get(0);
        }
        finally { em.close(); }
    }

    // Đọc cả kết luận đã ký để xem lại, không mở quyền sửa
    public MedicalRecord findRecordForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<MedicalRecord> records = em.createQuery("SELECT r FROM MedicalRecord r JOIN r.encounter e " +
                    "WHERE e.encounterId = :encounterId AND e.doctor.doctorId = :doctorId", MedicalRecord.class)
                    .setParameter("encounterId", encounterId).setParameter("doctorId", doctorId).getResultList();
            return records.isEmpty() ? null : records.get(0);
        }
        finally { em.close(); }
    }

    // Lưu nội dung form hiện tại và ký kết luận cùng transaction
    public void finalizeConsultation(Long encounterId, Long doctorId, String diagnosis,
                                     String clinicalNote, String treatment, String advice)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            MedicalEncounter encounter = ClinicalAccess.lockForDoctor(em, encounterId, doctorId);
            ClinicalAccess.requireConsulting(encounter);
            long drafts = em.createQuery("SELECT COUNT(p.prescriptionId) FROM Prescription p WHERE " +
                    "p.encounter = :encounter AND p.status = :draft", Long.class)
                    .setParameter("encounter", encounter).setParameter("draft", murach.business.Prescription.Status.DRAFT)
                    .getSingleResult();
            if (drafts > 0) throw new IllegalStateException("Cần xác nhận hoặc xử lý đơn thuốc nháp trước");
            List<MedicalRecord> records = em.createQuery("SELECT r FROM MedicalRecord r WHERE r.encounter = :encounter",
                    MedicalRecord.class).setParameter("encounter", encounter)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
            if (records.isEmpty()) throw new IllegalStateException("Cần bắt đầu khám trước");
            MedicalRecord record = records.get(0);
            record.update(diagnosis, clinicalNote, treatment, advice);
            record.finalizeRecord(EncounterWorkflow.countPendingResults(em, encounter));
            encounter.finishConsultation();
            List<QueueTicket> tickets = em.createQuery("SELECT q FROM QueueTicket q WHERE q.encounter = :encounter",
                    QueueTicket.class).setParameter("encounter", encounter).getResultList();
            if (tickets.isEmpty() || tickets.get(0).getStatus() != QueueTicket.Status.CALLED)
                throw new IllegalStateException("Phiếu gọi khám không phù hợp");
            tickets.get(0).finish();
            EncounterWorkflow.tryCompleteEncounter(em, encounterId);
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }
}
