package com.WorkOrderCreate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String workOrderNumber; // Auto-generated WON-XXXX

    private String purchaseOrderNumber;
    private String customer;
    private String gstin;
    private String hsnCode;
    private String material;
    private String thickness;
    private String width;
    private String length;
    private String process;
    private Integer quantityNo;
    private String quantityKg;
    private String location;
    private String materialReceivedDate;
    private String workOrderCreatedDate;

    private String cadDrawingPath; // Server path (e.g., /data/storage/cad_123.pdf)
    private String bomExcelPath;    // Server path (e.g., /data/storage/bom_123.xlsx)

    // WON generate panna logic
    @PrePersist
    public void generateWON() {
        this.workOrderNumber = "WON-" + (long)(Math.random() * 9000000000L + 1000000000L);
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWorkOrderNumber() {
		return workOrderNumber;
	}

	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public String getHsnCode() {
		return hsnCode;
	}

	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
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

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public Integer getQuantityNo() {
		return quantityNo;
	}

	public void setQuantityNo(Integer quantityNo) {
		this.quantityNo = quantityNo;
	}

	public String getQuantityKg() {
		return quantityKg;
	}

	public void setQuantityKg(String quantityKg) {
		this.quantityKg = quantityKg;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMaterialReceivedDate() {
		return materialReceivedDate;
	}

	public void setMaterialReceivedDate(String materialReceivedDate) {
		this.materialReceivedDate = materialReceivedDate;
	}

	public String getWorkOrderCreatedDate() {
		return workOrderCreatedDate;
	}

	public void setWorkOrderCreatedDate(String workOrderCreatedDate) {
		this.workOrderCreatedDate = workOrderCreatedDate;
	}

	public String getCadDrawingPath() {
		return cadDrawingPath;
	}

	public void setCadDrawingPath(String cadDrawingPath) {
		this.cadDrawingPath = cadDrawingPath;
	}

	public String getBomExcelPath() {
		return bomExcelPath;
	}

	public void setBomExcelPath(String bomExcelPath) {
		this.bomExcelPath = bomExcelPath;
	}
}
