package murach.DAO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import murach.business.Patient;
import murach.util.JPAUtil;

public class PatientDB {

    // Toan: Tìm bệnh nhân theo mã, tên, điện thoại, email và giới tính.
    public List<Patient> search(String keyword, Long specialtyId, String gender) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Patient p JOIN FETCH p.user u WHERE 1 = 1");
            if (gender != null) jpql.append(" AND u.gender = :gender");
            if (specialtyId != null)
                jpql.append(" AND EXISTS (SELECT a FROM Appointment a " +
                        "WHERE a.patient = p AND a.schedule.doctor.specialty.specialtyId = :specialtyId)");
            jpql.append(" ORDER BY p.patientId");

            TypedQuery<Patient> query = em.createQuery(jpql.toString(), Patient.class);
            if (gender != null) query.setParameter("gender", gender);
            if (specialtyId != null) query.setParameter("specialtyId", specialtyId);
            return query.getResultList().stream()
                    .filter(p -> DoctorDB.matches(keyword, p.getPatientId(), p.getPatientCode(),
                            p.getFullName(), p.getPhone(), p.getEmail()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    public Patient findById(Long patientId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Patient> patients = em.createQuery(
                    "SELECT p FROM Patient p JOIN FETCH p.user WHERE p.patientId = :patientId",
                    Patient.class)
                    .setParameter("patientId", patientId)
                    .getResultList();
            return patients.isEmpty() ? null : patients.get(0);
        } finally {
            em.close();
        }
    }

    public Long findFirstPatientId() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Long> ids = em.createQuery(
                    "SELECT p.patientId FROM Patient p ORDER BY p.patientId", Long.class)
                    .setMaxResults(1)
                    .getResultList();
            return ids.isEmpty() ? null : ids.get(0);
        } finally {
            em.close();
        }
    }

    public void updateProfile(Long patientId, String fullName, String phone, String email,
                              LocalDate dateOfBirth, String gender, String address) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Patient patient = em.find(Patient.class, patientId);
            if (patient == null) {
                throw new IllegalArgumentException("Bệnh nhân không tồn tại");
            }
            patient.updatePatientProfile(fullName, phone, email, dateOfBirth, gender, address);
            trans.commit();
        } catch (RuntimeException e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Lấy toàn bộ danh sách Bệnh nhân
    public List<Patient> getAllPatients() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT p FROM Patient p LEFT JOIN FETCH p.user";
            TypedQuery<Patient> query = em.createQuery(jpql, Patient.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Tìm Bệnh nhân theo ID
    public Patient getPatientById(int patientId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Patient.class, patientId);
        } finally {
            em.close();
        }
    }

    // Thêm Bệnh nhân mới
    public void insert(Patient patient) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(patient);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
