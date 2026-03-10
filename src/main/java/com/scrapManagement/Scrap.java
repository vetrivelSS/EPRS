// package com.scrapManagement;

// import jakarta.persistence.*;
// import java.time.LocalDate;

// @Entity
// @Table(name = "scrap")
// public class Scrap {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Integer id;

//     private String jobNumber;
//     private String customerName;
//     private String material;
//     private Double thickness;
//     private String process;

//     private Double scrapQuantity;

//     private String scrapType; // RETURN or SALE

//     private LocalDate productionDate;
//     private LocalDate returnedDate;
//     private LocalDate soldDate;

//     private Double soldScrap;
//     private Double availableScrap;

//     // getters setters

//     public Integer getId() {
//         return id;
//     }

//     public void setId(Integer id) {
//         this.id = id;
//     }

//     public String getJobNumber() {
//         return jobNumber;
//     }

//     public void setJobNumber(String jobNumber) {
//         this.jobNumber = jobNumber;
//     }

//     public String getCustomerName() {
//         return customerName;
//     }

//     public void setCustomerName(String customerName) {
//         this.customerName = customerName;
//     }

//     public String getMaterial() {
//         return material;
//     }

//     public void setMaterial(String material) {
//         this.material = material;
//     }

//     public Double getThickness() {
//         return thickness;
//     }

//     public void setThickness(Double thickness) {
//         this.thickness = thickness;
//     }

//     public String getProcess() {
//         return process;
//     }

//     public void setProcess(String process) {
//         this.process = process;
//     }

//     public Double getScrapQuantity() {
//         return scrapQuantity;
//     }

//     public void setScrapQuantity(Double scrapQuantity) {
//         this.scrapQuantity = scrapQuantity;
//     }

//     public String getScrapType() {
//         return scrapType;
//     }

//     public void setScrapType(String scrapType) {
//         this.scrapType = scrapType;
//     }

//     public LocalDate getProductionDate() {
//         return productionDate;
//     }

//     public void setProductionDate(LocalDate productionDate) {
//         this.productionDate = productionDate;
//     }

//     public LocalDate getReturnedDate() {
//         return returnedDate;
//     }

//     public void setReturnedDate(LocalDate returnedDate) {
//         this.returnedDate = returnedDate;
//     }

//     public LocalDate getSoldDate() {
//         return soldDate;
//     }

//     public void setSoldDate(LocalDate soldDate) {
//         this.soldDate = soldDate;
//     }

//     public Double getSoldScrap() {
//         return soldScrap;
//     }

//     public void setSoldScrap(Double soldScrap) {
//         this.soldScrap = soldScrap;
//     }

//     public Double getAvailableScrap() {
//         return availableScrap;
//     }

//     public void setAvailableScrap(Double availableScrap) {
//         this.availableScrap = availableScrap;
//     }
// }
