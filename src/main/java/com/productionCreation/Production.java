package com.productionCreation;

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
    private String createdDate;
    private String productionNumber;

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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getProductionNumber() {
        return productionNumber;
    }

    public void setProductionNumber(String productionNumber) {
        this.productionNumber = productionNumber;
    }

}