package murach.DAO;

import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import murach.business.*;
import murach.util.JPAUtil;

public class StaffDirectoryDB
{
    public List<Nurse> searchNurses(String keyword, Nurse.ProfessionType profession)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT n FROM Nurse n JOIN FETCH n.user u " +
                    "WHERE (:profession IS NULL OR n.professionType = :profession) ORDER BY u.fullName", Nurse.class)
                    .setParameter("profession", profession).getResultList().stream()
                    .filter(n -> DoctorDB.matches(keyword, n.getNurseCode(), n.getNurseId(), n.getFullName()))
                    .collect(Collectors.toList());
        }
        finally { em.close(); }
    }

    public Nurse findNurseById(Long id)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Nurse> nurses = em.createQuery("SELECT n FROM Nurse n JOIN FETCH n.user WHERE n.nurseId = :id", Nurse.class)
                    .setParameter("id", id).getResultList();
            return nurses.isEmpty() ? null : nurses.get(0);
        }
        finally { em.close(); }
    }

    public List<Technician> searchTechnicians(String keyword, Long specialtyId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT t FROM Technician t JOIN FETCH t.user u JOIN FETCH t.specialty sp " +
                    "WHERE (:specialtyId IS NULL OR sp.specialtyId = :specialtyId) ORDER BY u.fullName", Technician.class)
                    .setParameter("specialtyId", specialtyId).getResultList().stream()
                    .filter(t -> DoctorDB.matches(keyword, t.getTechnicianCode(), t.getTechnicianId(), t.getFullName()))
                    .collect(Collectors.toList());
        }
        finally { em.close(); }
    }

    public Technician findTechnicianById(Long id)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Technician> technicians = em.createQuery("SELECT t FROM Technician t JOIN FETCH t.user " +
                    "JOIN FETCH t.specialty WHERE t.technicianId = :id", Technician.class)
                    .setParameter("id", id).getResultList();
            return technicians.isEmpty() ? null : technicians.get(0);
        }
        finally { em.close(); }
    }

    public Map<Long, Staff> getStaffProfiles()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Staff> profiles = em.createQuery("SELECT s FROM Staff s JOIN FETCH s.user", Staff.class).getResultList();
            Map<Long, Staff> result = new HashMap<>();
            for (Staff profile : profiles) result.put(profile.getUserId(), profile);
            return result;
        }
        finally { em.close(); }
    }
}
