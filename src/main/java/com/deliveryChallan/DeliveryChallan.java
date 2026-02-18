package com.deliveryChallan;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
	@Data
	@Table(name = "delivery_challans")
	public class DeliveryChallan {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String jobOrderNumber;
	    private String customerName;
	    private String material;
	    private String quantityKg;

	    private String vehicleNumber;
	    private String hsnCode;
	    private String transport;
	    private String billToAddress;
	    private String shipToAddress;
	    private String challanNumber;
	    private String createdDate;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getJobOrderNumber() {
			return jobOrderNumber;
		}
		public void setJobOrderNumber(String jobOrderNumber) {
			this.jobOrderNumber = jobOrderNumber;
		}
		public String getCustomerName() {
			return customerName;
		}
		public void setCustomerName(String customerName) {
			this.customerName = customerName;
		}
		public String getMaterial() {
			return material;
		}
		public void setMaterial(String material) {
			this.material = material;
		}
		public String getQuantityKg() {
			return quantityKg;
		}
		public void setQuantity(String quantityKg) {
			this.quantityKg = quantityKg;
		}
		public String getVehicleNumber() {
			return vehicleNumber;
		}
		public void setVehicleNumber(String vehicleNumber) {
			this.vehicleNumber = vehicleNumber;
		}
		public String getHsnCode() {
			return hsnCode;
		}
		public void setHsnCode(String hsnCode) {
			this.hsnCode = hsnCode;
		}
		public String getTransport() {
			return transport;
		}
		public void setTransport(String transport) {
			this.transport = transport;
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
		public String getChallanNumber() {
			return challanNumber;
		}
		public void setChallanNumber(String challanNumber) {
			this.challanNumber = challanNumber;
		}
		public String getCreatedDate() {
			return createdDate;
		}
		public void setCreatedDate(String createdDate) {
			this.createdDate = createdDate;
		}
	    
	    
	}
