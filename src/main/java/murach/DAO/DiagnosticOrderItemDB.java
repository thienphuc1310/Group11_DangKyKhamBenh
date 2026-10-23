package murach.DAO;

import java.util.List;
import java.util.Collections;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class DiagnosticOrderItemDB
{
    static final String FETCH = "SELECT i FROM DiagnosticOrderItem i JOIN FETCH i.order o " +
            "JOIN FETCH o.encounter e JOIN FETCH e.appointment a JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH e.doctor d JOIN FETCH d.user JOIN FETCH i.performingSpecialty " +
            "LEFT JOIN FETCH i.assignedDoctor ad " +
            "LEFT JOIN FETCH i.assignedTechnician tech LEFT JOIN FETCH i.result ";

    public List<DiagnosticOrderItem> findWorklist(Long userId, Account.Role role)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String scope;
            Long profileId;
            Long specialtyId;
            if (role == Account.Role.TECHNICIAN)
            {
                Technician technician = ClinicalAccess.technician(em, userId);
                if (!ClinicalAvailabilityDB.isTechnicianScheduled(em, technician.getTechnicianId(), ClinicalAvailabilityDB.today()))
                    return Collections.emptyList();
                profileId = technician.getTechnicianId();
                specialtyId = technician.getSpecialty().getSpecialtyId();
                scope = "((i.status = :waiting AND i.serviceTypeSnapshot = :type " +
                        "AND i.performingSpecialty.specialtyId = :specialtyId) " +
                        "OR tech.technicianId = :profileId) ";
            }
            else if (role == Account.Role.DOCTOR)
            {
                Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
                if (doctor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                    throw new SecurityException("Chỉ dành cho bác sĩ cận lâm sàng");
                if (!ClinicalAvailabilityDB.isDoctorScheduled(em, doctor.getDoctorId(), ClinicalAvailabilityDB.today()))
                    return Collections.emptyList();
                profileId = doctor.getDoctorId();
                specialtyId = doctor.getSpecialty().getSpecialtyId();
                scope = "((i.status = :waiting AND i.serviceTypeSnapshot = :type " +
                        "AND i.performingSpecialty.specialtyId = :specialtyId) " +
                        "OR ad.doctorId = :profileId) ";
            }
            else throw new SecurityException("Không có quyền thực hiện cận lâm sàng");
            TypedQuery<DiagnosticOrderItem> query = em.createQuery(FETCH + "WHERE " + scope +
                    "AND o.status = :confirmed AND i.status NOT IN (:cancelled, :draft, :unpaid) " +
                    "AND e.status <> :encounterCancelled " +
                    "AND EXISTS (SELECT inv.invoiceId FROM Invoice inv WHERE inv.diagnosticOrder = o " +
                    "AND inv.invoiceType = :invoiceType AND inv.status = :paid " +
                    "AND EXISTS (SELECT pm.paymentId FROM Payment pm WHERE pm.invoice = inv AND pm.status = :paymentPaid)) " +
                    "ORDER BY o.orderedAt, i.orderItemId", DiagnosticOrderItem.class)
                    .setParameter("waiting", DiagnosticOrderItem.Status.WAITING_ACCEPTANCE)
                    .setParameter("type", role == Account.Role.TECHNICIAN ? DiagnosticService.ServiceType.LABORATORY : DiagnosticService.ServiceType.IMAGING)
                    .setParameter("specialtyId", specialtyId).setParameter("profileId", profileId)
                    .setParameter("confirmed", DiagnosticOrder.Status.CONFIRMED)
                    .setParameter("cancelled", DiagnosticOrderItem.Status.CANCELLED)
                    .setParameter("draft", DiagnosticOrderItem.Status.DRAFT)
                    .setParameter("unpaid", DiagnosticOrderItem.Status.WAITING_PAYMENT)
                    .setParameter("encounterCancelled", MedicalEncounter.Status.CANCELLED)
                    .setParameter("invoiceType", Invoice.InvoiceType.DIAGNOSTIC)
                    .setParameter("paid", Invoice.Status.PAID).setParameter("paymentPaid", Payment.Status.PAID);
            return query.getResultList();
        }
        finally { em.close(); }
    }

    public DiagnosticOrderItem findForPerformer(Long itemId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<DiagnosticOrderItem> items = em.createQuery(FETCH + "WHERE i.orderItemId = :itemId",
                    DiagnosticOrderItem.class).setParameter("itemId", itemId).getResultList();
            if (items.isEmpty()) return null;
            ClinicalAccess.requirePerformer(em, items.get(0), userId);
            return items.get(0);
        }
        finally { em.close(); }
    }

    // Hồ sơ đã thực hiện của bác sĩ hình ảnh; đọc lịch sử không cần có ca làm hôm nay.
    public List<DiagnosticOrderItem> findCompletedForDoctor(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
            if (doctor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                throw new SecurityException("Chỉ dành cho bác sĩ cận lâm sàng");
            return em.createQuery(FETCH + "WHERE ad.doctorId = :profileId " +
                    "AND i.serviceTypeSnapshot = :type AND i.status = :completed " +
                    "AND i.result.status = :released ORDER BY i.completedAt DESC, i.orderItemId DESC", DiagnosticOrderItem.class)
                    .setParameter("profileId", doctor.getDoctorId())
                    .setParameter("type", DiagnosticService.ServiceType.IMAGING)
                    .setParameter("completed", DiagnosticOrderItem.Status.COMPLETED)
                    .setParameter("released", DiagnosticResult.Status.RELEASED).getResultList();
        }
        finally { em.close(); }
    }

    public void accept(Long itemId, Long userId, Account.Role role)
    {
        change(itemId, userId, role, true);
    }

    public void startProcessing(Long itemId, Long userId, Account.Role role)
    {
        change(itemId, userId, role, false);
    }

    private void change(Long itemId, Long userId, Account.Role role, boolean accept)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            if (role == Account.Role.DOCTOR)
                DoctorLeaveDB.lockDoctor(em, ClinicalAccess.doctorForUser(em, userId).getDoctorId());
            DiagnosticOrderItem item = ClinicalAccess.lockItem(em, itemId);
            if (accept)
            {
                if (role == Account.Role.TECHNICIAN)
                {
                    Technician technician = ClinicalAccess.technician(em, userId);
                    ClinicalAvailabilityDB.requireTechnicianWorking(em, technician);
                    item.assignTechnician(technician);
                }
                else if (role == Account.Role.DOCTOR)
                {
                    Doctor doctor = ClinicalAccess.doctorForUser(em, userId);
                    if (doctor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                        throw new SecurityException("Chỉ dành cho bác sĩ cận lâm sàng");
                    ClinicalAvailabilityDB.requireDoctorWorking(em, doctor);
                    item.assignDoctor(doctor);
                }
                else throw new SecurityException("Không có quyền tiếp nhận");
            }
            else
            {
                ClinicalAccess.requireWorkingPerformer(em, item, userId);
                SpecimenDB.requireReceivedSamples(em, item);
                item.startProcessing();
            }
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
