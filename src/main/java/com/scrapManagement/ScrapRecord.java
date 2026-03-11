package com.ScrapManagement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "production_daily_history")
@Data // Lombok annotation to auto-generate Getters/Setters
public class ScrapRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String material; // e.g., HRPO
    private Double thickness; // e.g., 2
    private String process; // e.g., Laser Cutting
    private String scrapQuantity; // e.g., 1kg
    private String scrapType; // e.g., Return

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;
}
