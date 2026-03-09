package com.productionCreation;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "production_summary")
public class ProductionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String productionNumber;

    private int receivedQuantity;

    private int finishedQuantity;

    private int scrapQuantity;

    private int balanceQuantity;

    private String scrapType;

    private String remark;

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

    public String getProductionNumber() {
        return productionNumber;
    }

    public void setProductionNumber(String productionNumber) {
        this.productionNumber = productionNumber;
    }

    public int getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(int receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
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

    public String getScrapType() {
        return scrapType;
    }

    public void setScrapType(String scrapType) {
        this.scrapType = scrapType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setId(int id) {
        this.id = id;
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

}