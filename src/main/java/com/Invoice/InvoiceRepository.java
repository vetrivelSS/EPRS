package com.Invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // ADD THIS
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // ADD THIS
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query(value = "SELECT i.invoice_number, i.total_amount, i.ship_to_address, i.dc_details_json," +
            "bp.city, i.reference_no, dc.created_at AS created_at, dc.challan_number, " +
            "wo.purchase_order_number, wo.hsn_code,wo.length,wo.width,wo.thickness, bp.payment_terms, bp.customer_name, bp.state " +
            "FROM invoices i " +
            "LEFT JOIN delivery_challans dc ON i.customer_name = dc.customer_name " +
            "LEFT JOIN work_order wo ON dc.customer_name = wo.customer " +
            "LEFT JOIN business_partner bp ON i.customer_name = bp.customer_name " + // Added space here
            "WHERE i.invoice_number = :invoiceNumber", nativeQuery = true)
    List<Map<String, Object>> findInvoiceWithFullDetails(@Param("invoiceNumber") String invoiceNumber);

    @Query("SELECT MAX(i.referenceNo) FROM Invoice i")
    Integer findMaxReferenceNo();

    // In InvoiceRepository.java
    @Modifying
    @Transactional
    @Query("UPDATE Invoice i SET i.downloadCount = COALESCE(i.downloadCount, 0) + 1 WHERE i.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
}
