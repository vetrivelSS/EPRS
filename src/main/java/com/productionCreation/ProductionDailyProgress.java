package com.productionCreation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// import jakarta.persistence.Table; 
import lombok.Data;

@Entity
@Data
public class ProductionDailyProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productionNumber;

    // Add this inside your ProductionDailyProgress class if @Data fails
    public void setProductionNumber(String productionNumber) {
        this.productionNumber = productionNumber;
    }

    public String getProductionNumber() {
        return this.productionNumber;
    }

    private String status;
    private Double finishedQuantity;
    private Double balanceQuantity;
    private Double scrapQuantity;
    private String productionDate;
    private String remarks;

}
