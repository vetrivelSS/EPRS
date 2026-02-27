package com.productionCreation;

import com.jobOrderCreation.BaseResponse;
import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
import com.productionCreation.Production;
import com.productionCreation.ProductionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/production"})
@CrossOrigin(value={"*"})
public class ProductionController {
    
    @Autowired
    private ProductionRepository productionRepo;
    
    @Autowired
    private JobOrderRepository jobOrderRepo;

    @GetMapping(value={"/pending-from-joborders"})
    public ResponseEntity<Object> getPendingJobs() {
        List<JobOrder> pending = this.jobOrderRepo.findByStatus("PENDING");
        return ResponseEntity.ok(new BaseResponse(200, "Pending Jobs Fetched", pending));
    }

    @GetMapping(value={"/all-joborders"})
    public ResponseEntity<Object> getAllJobs() {
        List<JobOrder> allJobs = this.jobOrderRepo.findAll();
        return ResponseEntity.ok(new BaseResponse(200, "All Job Orders Fetched Successfully", allJobs));
    }

    @GetMapping(value={"/all-productions"})
    public ResponseEntity<Object> getAllProductions() {
        try {
            List<Production> productionList = this.productionRepo.findAll();
            if (productionList.isEmpty()) {
                return ResponseEntity.ok(new BaseResponse(200, "No production records found", productionList));
            }
            return ResponseEntity.ok(new BaseResponse(200, "Production data fetched successfully", productionList));
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping(value={"/start-inprogress/{jobNumber}"})
    public ResponseEntity<Object> startProgress(@PathVariable String jobNumber) {
        JobOrder job = this.jobOrderRepo.findByJobOrderNumber(jobNumber);
        if (job == null) {
            return ResponseEntity.status(404).body(new BaseResponse(404, "Job Order Not Found", null));
        }
        
        // Update Job Order Status
        job.setStatus("IN PROGRESS");
        this.jobOrderRepo.save(job); // Removed (Object) cast

        // Create New Production Entry
        Production newProduction = new Production();
        newProduction.setJobOrderNumber(job.getJobOrderNumber());
        newProduction.setCustomerName(job.getCustomerName());
        newProduction.setMaterial(job.getMaterial());
        newProduction.setThickness(job.getThickness());
        newProduction.setStatus("IN PROGRESS");
        
        // Save and get the ID to generate Production Number
        newProduction = this.productionRepo.save(newProduction); // Removed (Object) cast
        
        String generatedNo = "PRD-" + newProduction.getId();
        newProduction.setProductionNumber(generatedNo);
        
        // Update with generated ID
        this.productionRepo.save(newProduction); // Removed (Object) cast
        
        return ResponseEntity.ok(new BaseResponse(200, "Production Started: " + generatedNo, newProduction));
    }

    @PostMapping(value={"/submit-production-completed"})
    public ResponseEntity<Object> submitProduction(@RequestBody Production req) {
        try {
            Production existingRecord = this.productionRepo.findByJobOrderNumber(req.getJobOrderNumber());
            if (existingRecord == null) {
                return ResponseEntity.status(404).body(new BaseResponse(404, "Error: Production record not found!", null));
            }
            if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
                return ResponseEntity.status(400).body(new BaseResponse(400, "Error: Status must be 'COMPLETED' to move to history.", null));
            }
            if (req.getFinishedQuantity() == null || req.getScrapQuantity() == null || req.getScrapType() == null) {
                return ResponseEntity.status(400).body(new BaseResponse(400, "Error: Please fill all fields before completing.", null));
            }
            
            existingRecord.setFinishedQuantity(req.getFinishedQuantity());
            existingRecord.setBalanceQuantity(req.getBalanceQuantity());
            existingRecord.setScrapQuantity(req.getScrapQuantity());
            existingRecord.setScrapType(req.getScrapType());
            existingRecord.setCompletedDate(req.getCompletedDate());
            existingRecord.setProductionDate(req.getProductionDate());
            existingRecord.setRemarks(req.getRemarks());
            existingRecord.setStatus("COMPLETED");
            
            Production saved = this.productionRepo.save(existingRecord); // Removed (Object) cast
            
            this.jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "COMPLETED");
            
            return ResponseEntity.ok(new BaseResponse(200, "Production marked as COMPLETED successfully", saved));
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "System Error: " + e.getMessage(), null));
        }
    }

    @GetMapping(value={"/list-all-status/{status}"})
    public ResponseEntity<Object> getListByStatus(@PathVariable String status) {
        List<Production> list = this.productionRepo.findProductionByStatus(status.toUpperCase());
        return ResponseEntity.ok(new BaseResponse(200, "Data fetched", list));
    }
}