package com.productionCreation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "production_records")
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "job_order", unique = true)
    private String jobOrder;
    private String material;
    private String thickness;
    private String process;
    private String receivedQuantityKg;
    private String status;
    private String remark;
    private LocalDateTime createdDate;
    private String productionNumber;

    private String quantityNo;
    @Column(name = "customer_name")
    private String customerName;
    @ElementCollection
    @CollectionTable(name = "production_daily_history", joinColumns = @JoinColumn(name = "production_id"))
    private List<DailyUpdate> history = new ArrayList<>();

    // Embeddable Inner Class
    @Embeddable
    public static class DailyUpdate {
        private Double finishedQty;
        private Double balanceQty;
        private Double scrapQty;
        private String scrapType;
        private String remark;
        private LocalDateTime updateTime;

        // ************
        private String scrapStatus; // "AVAILABLE" or "SOLD"
        private Double soldScrapQty;

        // Constructors, Getters & Setters
        public DailyUpdate() {
        }

        public DailyUpdate(Double f, Double b, Double s, String t, String r) {
            this.finishedQty = f;
            this.balanceQty = b;
            this.scrapQty = s;
            this.scrapType = t;
            this.remark = r;
            this.updateTime = LocalDateTime.now();

            this.scrapStatus = "AVAILABLE";
            this.soldScrapQty = 0.0;
        }

        public String getScrapStatus() {
            return scrapStatus;
        }

        public void setScrapStatus(String scrapStatus) {
            this.scrapStatus = scrapStatus;
        }

        public Double getSoldScrapQty() {
            return soldScrapQty;
        }

        public void setSoldScrapQty(Double soldScrapQty) {
            this.soldScrapQty = soldScrapQty;
        }
        // *************
        // ... (Add Getters/Setters here) ...
        public Double getFinishedQty() {
            return finishedQty;
        }

        public void setFinishedQty(Double finishedQty) {
            this.finishedQty = finishedQty;
        }

        public Double getBalanceQty() {
            return balanceQty;
        }

        public void setBalanceQty(Double balanceQty) {
            this.balanceQty = balanceQty;
        }

        public Double getScrapQty() {
            return scrapQty;
        }

        public void setScrapQty(Double scrapQty) {
            this.scrapQty = scrapQty;
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

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }

    public String getQuantityNo() {
        return quantityNo;
    }

    public void setQuantityNo(String quantityNo) {
        this.quantityNo = quantityNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Default No-Argument Constructor (Required by JPA)
    public Production() {
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobOrder() {
        return jobOrder;
    }

    public void setJobOrder(String jobOrder) {
        this.jobOrder = jobOrder;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getThickness() {
        return thickness;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getReceivedQuantityKg() {
        return receivedQuantityKg;
    }

    public void setReceivedQuantityKg(String receivedQuantityKg) {
        this.receivedQuantityKg = receivedQuantityKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getProductionNumber() {
        return productionNumber;
    }

    public void setProductionNumber(String productionNumber) {
        this.productionNumber = productionNumber;
    }

    public List<DailyUpdate> getHistory() {
        return history;
    }

    public void setHistory(List<DailyUpdate> history) {
        this.history = history;
    }

}