package com.jobOrderCreation;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "job_orders")
@Data
public class JobOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String jobOrderNumber; 
    
    private String workOrderNumber;
    private String customerName;
    private String hsnCode;
    private String material;
    private String thickness;
    private String process;
    private String quantityNo;
    private String quantityKg;
    private String location;
    private String priority;

    private String status; 
 
 
    private List<String> assignedOperators;
     

    public List<String> getAssignedOperators() {
		return assignedOperators;
	}
	public void setAssignedOperators(List<String> assignedOperators) {
		this.assignedOperators = assignedOperators;
	}

	private String jobCreatedDate;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
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

	public String getWorkOrderNumber() {
		return workOrderNumber;
	}

	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getQuantityNo() {
		return quantityNo;
	}

	public void setQuantityNo(String quantityNo) {
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

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	

	public String getJobCreatedDate() {
		return jobCreatedDate;
	}

	public void setJobCreatedDate(String jobCreatedDate) {
		this.jobCreatedDate = jobCreatedDate;
	}
    
    
}