package com.ledger;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "ledgers")
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;
    private String ledgerNumber;

    private String partnerName;
    private Integer paymentTerms;
    private Integer dueDays;
    private Double totalAmount;

    private LocalDate invoiceCreatedDate;
    private LocalDate settledDate;

    private String status;
}
