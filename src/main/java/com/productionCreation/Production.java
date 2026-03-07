package com.productionCreation;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "productions")
@Data
public class Production {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productionNumber;
    private String jobOrderNumber;
    private String material;
    private Double thickness;
    private String process;
    private Double receivedQuantityKg;
    private String status;
    private String remarks;
}
