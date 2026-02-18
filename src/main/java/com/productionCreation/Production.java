package com.productionCreation;

	import jakarta.persistence.*;
	import lombok.Data;

	@Entity
	@Table(name = "production")
	@Data
	public class Production{
		
		
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String jobOrderNumber;      
	    private String productionNumber;    
	    private String customerName;
	    private String material;
	    private String thickness;
	    private String process;
	    
	    private String finishedQuantity;
	    private String balanceQuantity;
	    private String scrapQuantity;
	    private String scrapType;
	    private String productionDate;
	    private String status;     
	    private String completedDate;
	    
	  
		public String getCompletedDate() {
			return completedDate;
		}
		public void setCompletedDate(String completedDate) {
			this.completedDate = completedDate;
		}
		private String remarks;
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
		public String getProductionNumber() {
			return productionNumber;
		}
		public void setProductionNumber(String productionNumber) {
			this.productionNumber = productionNumber;
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
		public String getScrapType() {
			return scrapType;
		}
		public void setScrapType(String scrapType) {
			this.scrapType = scrapType;
		}
		public String getProductionDate() {
			return productionDate;
		}
		public void setProductionDate(String productionDate) {
			this.productionDate = productionDate;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getRemarks() {
			return remarks;
		}
		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}
	    
	    
	}


