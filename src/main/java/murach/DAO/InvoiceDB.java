package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;

/** Cầu nối thanh toán cho chỉ định/đơn thuốc, dùng chung transaction với DAO gọi. */
public final class InvoiceDB
{
    private InvoiceDB() {}

    public static void issue(EntityManager em, Invoice invoice)
    {
        if (!em.getTransaction().isActive()) throw new IllegalStateException("Cần transaction đang mở");
        em.persist(invoice);
        em.persist(new Payment(ClinicalAccess.code("PAY"), invoice, Payment.Method.BANK_TRANSFER,
                "Chờ thanh toán " + invoice.getInvoiceCode()));
    }

    public static boolean isPaid(EntityManager em, String source, Object value, Invoice.InvoiceType type)
    {
        // source chỉ do code nội bộ truyền, không lấy từ request.
        if (!"diagnosticOrder".equals(source) && !"prescription".equals(source))
            throw new IllegalArgumentException("Nguồn hóa đơn không hợp lệ");
        Long count = em.createQuery("SELECT COUNT(i.invoiceId) FROM Invoice i WHERE i." + source +
                " = :source AND i.invoiceType = :type AND i.status = :paid " +
                "AND EXISTS (SELECT p.paymentId FROM Payment p WHERE p.invoice = i AND p.status = :paymentPaid)",
                Long.class).setParameter("source", value).setParameter("type", type)
                .setParameter("paid", Invoice.Status.PAID).setParameter("paymentPaid", Payment.Status.PAID)
                .getSingleResult();
        Long debt = em.createQuery("SELECT COUNT(i.invoiceId) FROM Invoice i WHERE i." + source +
                " = :source AND i.status IN (:draft, :unpaid)", Long.class)
                .setParameter("source", value).setParameter("draft", Invoice.Status.DRAFT)
                .setParameter("unpaid", Invoice.Status.UNPAID).getSingleResult();
        return count > 0 && debt == 0;
    }

    public static void requireDiagnosticPaid(EntityManager em, DiagnosticOrder order)
    {
        if (!isPaid(em, "diagnosticOrder", order, Invoice.InvoiceType.DIAGNOSTIC))
            throw new IllegalStateException("Chỉ định chưa được thanh toán đầy đủ");
    }

    // Chỉ mở công việc sau khi giao dịch và hóa đơn thực sự đã PAID.
    public static void afterPayment(EntityManager em, Payment payment)
    {
        Invoice invoice = payment.getInvoice();
        if (invoice == null) return;
        if (payment.getStatus() != Payment.Status.PAID || invoice.getStatus() != Invoice.Status.PAID)
            throw new IllegalStateException("Giao dịch chưa hoàn tất");
        if (invoice.getInvoiceType() == Invoice.InvoiceType.DIAGNOSTIC)
        {
            DiagnosticOrder order = invoice.getDiagnosticOrder();
            requireDiagnosticPaid(em, order);
            for (DiagnosticOrderItem item : order.getItems())
                if (item.getStatus() == DiagnosticOrderItem.Status.WAITING_PAYMENT) item.markPaid();
        }
        if (invoice.getEncounter() != null)
            EncounterWorkflow.tryCompleteEncounter(em, invoice.getEncounter().getEncounterId());
    }
}
