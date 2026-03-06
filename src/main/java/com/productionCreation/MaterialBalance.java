package com.productionCreation;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "material_balance")
@Data
public class MaterialBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    private String productionNumber;
    private Double receivedQuantityKg;
    private Double finishedQuantity;
    private Double balanceQuantity;
    private Double scrapQuantity;
    private String scrapType;
    private String remarks;
}
