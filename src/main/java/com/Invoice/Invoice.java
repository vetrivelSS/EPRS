package com.Invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getJobOrderNumber() {
		return jobOrderNumber;
	}
	public void setJobOrderNumber(String jobOrderNumber) {
		this.jobOrderNumber = jobOrderNumber;
	}
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public Double getQuantityNumber() {
		return quantityNumber;
	}
	public void setQuantityNumber(Double quantityNumber) {
		this.quantityNumber = quantityNumber;
	}
	public Double getQuantityKg() {
		return quantityKg;
	}
	public void setQuantityKg(Double quantityKg) {
		this.quantityKg = quantityKg;
	}
	public Double getRatePer() {
		return ratePer;
	}
	public void setRatePer(Double ratePer) {
		this.ratePer = ratePer;
	}
	public Double getDiscount() {
		return discount;
	}
	public void setDiscount(Double discount) {
		this.discount = discount;
	}
	public String getGstType() {
		return gstType;
	}
	public void setGstType(String gstType) {
		this.gstType = gstType;
	}
	public String getBillToAddress() {
		return billToAddress;
	}
	public void setBillToAddress(String billToAddress) {
		this.billToAddress = billToAddress;
	}
	public String getShipToAddress() {
		return shipToAddress;
	}
	public void setShipToAddress(String shipToAddress) {
		this.shipToAddress = shipToAddress;
	}
	public String getDcDetailsJson() {
		return dcDetailsJson;
	}
	public void setDcDetailsJson(String dcDetailsJson) {
		this.dcDetailsJson = dcDetailsJson;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String customerName; //
    private String jobOrderNumber; //
    private String material; //
    private Double quantityNumber; //
    private Double quantityKg; //
    private Double ratePer; //
    private Double discount; //
    private String gstType; // CGST, SGST, IGST dropdown
    
    
    
	// Address fields from Step 2
    private String billToAddress;
    private String shipToAddress;

    // Multiple DC details-ah store panna
    @Column(columnDefinition = "LONGTEXT")
    private String dcDetailsJson; 

    private Double totalAmount; // Final calculated amount
    private String status; // Active/Pending
}