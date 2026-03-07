// package com.scrap;

// import java.time.LocalDate;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import lombok.Data;

// @Entity
// @Data
// public class ScrapEntry {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(unique = true)
//     private String jobNumber; // The "Production Number" used for lookup

//     private String material; // e.g., HRPO
//     private Integer thickness; // e.g., 2
//     private String process; // e.g., Laser Cutting
//     private String scrapQuantity; // e.g., 1kg
//     private String scrapType; // e.g., Return
//     private LocalDate productionDate;
//     private LocalDate returnedDate;
//     private LocalDate soldDate; // For the "Sold Scrap" state

//     private String status; // "Available" or "Sold"
// }
