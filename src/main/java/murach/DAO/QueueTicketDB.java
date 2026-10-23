package murach.DAO;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import murach.business.Doctor;
import murach.business.MedicalEncounter;
import murach.business.QueueTicket;

public class QueueTicketDB {
    public QueueTicket issue(EntityManager em, MedicalEncounter encounter)
    {
        if (!em.getTransaction().isActive() || !em.contains(encounter))
            throw new IllegalStateException("Cấp số phải nằm trong transaction tiếp nhận");

        Long doctorId = encounter.getDoctor().getDoctorId();
        Doctor doctor = em.find(Doctor.class, doctorId, LockModeType.PESSIMISTIC_WRITE);
        if (doctor == null)
            throw new IllegalArgumentException("Bác sĩ không tồn tại");

        LocalDate queueDate = encounter.getCheckedInAt().toLocalDate();
        Integer lastNumber = em.createQuery(
                        "SELECT COALESCE(MAX(q.queueNumber), 0) FROM QueueTicket q " +
                                "WHERE q.doctor = :doctor AND q.queueDate = :queueDate", Integer.class)
                .setParameter("doctor", doctor)
                .setParameter("queueDate", queueDate)
                .getSingleResult();

        QueueTicket ticket = new QueueTicket(encounter, Math.addExact(lastNumber, 1));
        em.persist(ticket);
        return ticket;
    }
}
