package murach.DAO;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import murach.business.Doctor;
import murach.business.Specialty;
import murach.util.JPAUtil;

public class DoctorDB {

    // Toan: Tìm bác sĩ theo tên, mã, ID và lọc theo chuyên khoa, giới tính.
    public List<Doctor> search(String keyword, Long specialtyId, String gender)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT d FROM Doctor d JOIN FETCH d.user u LEFT JOIN FETCH d.specialty s WHERE 1 = 1");
            if (specialtyId != null) jpql.append(" AND s.specialtyId = :specialtyId");
            if (gender != null) jpql.append(" AND u.gender = :gender");
            jpql.append(" ORDER BY u.fullName");

            TypedQuery<Doctor> query = em.createQuery(jpql.toString(), Doctor.class);
            if (specialtyId != null) query.setParameter("specialtyId", specialtyId);
            if (gender != null) query.setParameter("gender", gender);
            return query.getResultList().stream()
                    .filter(d -> matches(keyword, d.getFullName(), d.getDoctorCode(), d.getDoctorId()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // Toan: So khớp từ khóa không phân biệt dấu và chữ hoa/thường.
    public static boolean matches(String keyword, Object... fields)
    {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        StringBuilder all = new StringBuilder();
        for (Object field : fields) if (field != null) all.append(field).append(' ');
        String text = unaccent(all.toString());
        for (String word : unaccent(keyword).split("\\s+"))
            if (!text.contains(word)) return false;
        return true;
    }

    // Toan: Chuẩn hóa chuỗi tiếng Việt phục vụ tìm kiếm.
    private static String unaccent(String text)
    {
        if (text == null) return "";
        String value = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return value.replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).trim();
    }

    // Toan: Lấy một bác sĩ kèm thông tin người dùng và chuyên khoa.
    public Doctor findById(Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Doctor> doctors = em.createQuery(
                            "SELECT d FROM Doctor d JOIN FETCH d.user " +
                                    "LEFT JOIN FETCH d.specialty WHERE d.doctorId = :id",
                            Doctor.class)
                    .setParameter("id", doctorId)
                    .getResultList();
            return doctors.isEmpty() ? null : doctors.get(0);
        } finally {
            em.close();
        }
    }

    // Lấy toàn bộ danh sách Bác sĩ
    public List<Doctor> getAllDoctors()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT d FROM Doctor d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.specialty";
            TypedQuery<Doctor> query = em.createQuery(jpql, Doctor.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Tìm bác sĩ theo ID
    public Doctor getDoctorById(int doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Doctor.class, doctorId);
        } finally {
            em.close();
        }
    }

    // Thêm Bác sĩ mới
    public void insert(Doctor doctor)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(doctor);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Cập nhật thông tin Bác sĩ
    public void update(Doctor doctor)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(doctor);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Bác sĩ cập nhật thông tin cá nhân và hồ sơ chuyên môn của chính mình.
    public Doctor updateProfile(Long doctorId, String fullName, String phone, String email,
                                LocalDate dateOfBirth, String gender, String address,
                                Long specialtyId, String licenseNumber, String qualification,
                                Integer experienceYears, String description)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Doctor doctor = em.find(Doctor.class, doctorId);
            if (doctor == null)
                throw new IllegalArgumentException("Không tìm thấy hồ sơ bác sĩ");

            Specialty specialty = specialtyId == null ? null : em.find(Specialty.class, specialtyId);
            if (specialty == null)
                throw new IllegalArgumentException("Chuyên khoa không hợp lệ");

            doctor.updateProfileDoctor(fullName, phone, email, dateOfBirth, gender, address,
                    specialty, licenseNumber, qualification, experienceYears, description);
            trans.commit();
            return doctor;
        } catch (RuntimeException e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

//    //tìm bác sĩ theo id của chuyên khoa, phục vụ cho list bác sĩ theo chuyên khoa đã chọn ở main
//    public List<Doctor> getDoctorBySpecialtyId(long specialtyId)
//    {
//        EntityManager em = JPAUtil.getEntityManager();
//
//        try
//        {
//            String jpql =
//                    "SELECT d FROM Doctor d " +
//                    "JOIN FETCH d.user" +
//                    "JOIN FETCH d.specialty" +
//                    "WHERE d.specialty.specialId = :specialtyId" +
//                    "ORDER BY d.user.fullName";
//            return em.createQuery(jpql, Doctor.class).setParameter("specialtyId", specialtyId).getResultList();
//        }
//        finally
//        {
//            em.close();
//        }
//    }
}
