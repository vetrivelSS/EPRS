package com.productionCreation;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "production_result")
public class ProductionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int finishedQuantity;

    private int scrapQuantity;
    private String jobOrder;
    private int balanceQuantity;

    private LocalDate productionDate;

    private String remark;
    private String scrapType;
    private String productionNumber;
    private LocalDate updateDate;
    private String status;

    @PrePersist
    @PreUpdate
    public void updateDate() {
        this.updateDate = LocalDate.now();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public int getFinishedQuantity() {
        return finishedQuantity;
    }

    public void setFinishedQuantity(int finishedQuantity) {
        this.finishedQuantity = finishedQuantity;
    }

    public int getScrapQuantity() {
        return scrapQuantity;
    }

    public void setScrapQuantity(int scrapQuantity) {
        this.scrapQuantity = scrapQuantity;
    }

    public int getBalanceQuantity() {
        return balanceQuantity;
    }

    public void setBalanceQuantity(int balanceQuantity) {
        this.balanceQuantity = balanceQuantity;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getProductionNumber() {
        return productionNumber;
    }

    public void setProductionNumber(String productionNumber) {
        this.productionNumber = productionNumber;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScrapType() {
        return scrapType;
    }

    public void setScrapType(String scrapType) {
        this.scrapType = scrapType;
    }

    public String getJobOrder() {
        return jobOrder;
    }

    public void setJobOrder(String jobOrder) {
        this.jobOrder = jobOrder;
    }

}