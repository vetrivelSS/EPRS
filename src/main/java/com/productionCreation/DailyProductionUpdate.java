package com.productionCreation;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity

@Table(name = "DailyProductionUpdate")

@Data
public class DailyProductionUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;

    private String productionId; // Now String type (e.g., PRD-12-2026)
    private String finishedQuantity;
    private String balanceQuantity;
    private String scrapQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductionId() {
        return productionId;
    }

    public void setProductionId(String productionId) {
        this.productionId = productionId;
    }

    public String getFinishedQuantity() {
        return finishedQuantity;
    }

    public void setFinishedQuantity(String finishedQuantity) {
        this.finishedQuantity = finishedQuantity;
    }

    public String getBalanceQuantity() {
        return balanceQuantity;
    }

    public void setBalanceQuantity(String balanceQuantity) {
        this.balanceQuantity = balanceQuantity;
    }

    public String getScrapQuantity() {
        return scrapQuantity;
    }

    public void setScrapQuantity(String scrapQuantity) {
        this.scrapQuantity = scrapQuantity;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getEntryTimestamp() {
        return entryTimestamp;
    }

    public void setEntryTimestamp(LocalDateTime entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    private String productionDate; // DD/MM/YYYY
    private String remark;
    private LocalDateTime entryTimestamp;

    @PrePersist
    protected void onCreate() {
        this.entryTimestamp = LocalDateTime.now();
    }
}
