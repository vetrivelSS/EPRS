package com.DeliveryChallan;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "delivery_challans")
@Data // Getter, Setter, and No-Args Constructor-ku (illana manual-ah eludhungga)
public class DeliveryChallan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String challanNumber;
    private String customerName;
    private String billToAddress;
    private String shipToAddress;
    private String status;
    
    public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChallanNumber() {
		return challanNumber;
	}
	public void setChallanNumber(String challanNumber) {
		this.challanNumber = challanNumber;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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
	public String getTransport() {
		return transport;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	public String getVehicleNumber() {
		return vehicleNumber;
	}
	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}
	public String getProductionNumber() {
		return productionNumber;
	}
	public void setProductionNumber(String productionNumber) {
		this.productionNumber = productionNumber;
	}
	public Double getQuantityKg() {
		return quantityKg;
	}
	public void setQuantityKg(Double quantityKg) {
		this.quantityKg = quantityKg;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	private String hsnCode;
    private String material;
    private String transport;
    private String vehicleNumber;
    private String productionNumber; // Merged PRO numbers (PRO-001, PRO-002)
    
    private Double quantityKg;
    private Double totalAmount;

    // --- MUKKIYAM: JSON Array-ah String-ah store panna indha field ---
    @Column(name = "productions", columnDefinition = "LONGTEXT")
    private String productionsJson;

    // Manual-ah Getter/Setter venum-na:
    public String getProductionsJson() { return productionsJson; }
    public void setProductionsJson(String productionsJson) { this.productionsJson = productionsJson; }
    
    // Matra getters/setters...
}