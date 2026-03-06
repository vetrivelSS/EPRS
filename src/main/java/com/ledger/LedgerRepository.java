package com.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    Ledger findByInvoiceNumber(String invoiceNumber);

    List<Ledger> findByStatusIgnoreCase(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Ledger l SET l.status = :status, l.settledDate = :settledDate WHERE l.invoiceNumber = :invoiceNumber")
    int updateLedgerStatus(String invoiceNumber, String status, LocalDate settledDate);
}