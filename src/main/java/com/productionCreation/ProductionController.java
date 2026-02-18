package com.productionCreation;

import com.jobOrderCreation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/production")
@CrossOrigin("*")
public class ProductionController {

    @Autowired
    private ProductionRepository productionRepo;

    @Autowired
    private JobOrderRepository jobOrderRepo;

    // STEP 1: Show data in the "Pending" Tab
    @GetMapping("/pending-from-joborders")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getPendingJobs() {
        List<JobOrder> pending = jobOrderRepo.findByStatus("PENDING");
        return ResponseEntity.ok(new BaseResponse<>(200, "Pending Jobs Fetched", pending));
    }

    // STEP 2: Move from Pending to In-Progress
    // Call this when user clicks the "Edit/Update" icon on a Pending card
    @PostMapping("/start-inprogress/{jobNumber}")
    public ResponseEntity<BaseResponse<?>> startProgress(@PathVariable String jobNumber) {
        
        // 1. Job Order-a fetch panrom (Details copy panna idhu dhaan mukkiyam)
        JobOrder job = jobOrderRepo.findByJobOrderNumber(jobNumber);
        
        if (job == null) {
            return ResponseEntity.status(404).body(new BaseResponse<>(404, "Job Order Not Found", null));
        }

        // 2. Job Order status-a update panrom
        job.setStatus("IN PROGRESS");
        jobOrderRepo.save(job);

        // 3. Production table-la pudhu entry create panrom
        // Ingat dhaan neenga munnadi pannadha thiruthura maari values-a set panrom
        Production newProduction = new Production();
        newProduction.setJobOrderNumber(job.getJobOrderNumber());
        newProduction.setCustomerName(job.getCustomerName()); // Copying from JobOrder
        newProduction.setMaterial(job.getMaterial());         // Copying from JobOrder
        newProduction.setThickness(job.getThickness());       // Copying from JobOrder
        newProduction.setStatus("IN PROGRESS");
        
        // Initial save (ID generate aaga)
        newProduction = productionRepo.save(newProduction);

        // 4. Production Number generate panni update panrom
        String generatedNo = "PRD-" + newProduction.getId();
        newProduction.setProductionNumber(generatedNo);
        
        productionRepo.save(newProduction);

        return ResponseEntity.ok(new BaseResponse<>(
            200, 
            "Production Started: " + generatedNo, 
            newProduction
        ));
    }
    @PostMapping("/submit-production-completed")
    public ResponseEntity<BaseResponse<?>> submitProduction(@RequestBody Production req) {
        try {
            // 1. Fetch Existing Record
            // Database-la irukura andha specific "In-Progress" record-a first edukkanum
            Production existingRecord = productionRepo.findByJobOrderNumber(req.getJobOrderNumber());

            if (existingRecord == null) {
                return ResponseEntity.status(404).body(new BaseResponse<>(404, "Error: Production record not found!", null));
            }

            // 2. Status Validation (Only Completed is allowed here)
            if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
                return ResponseEntity.status(400).body(new BaseResponse<>(400, "Error: Status must be 'COMPLETED' to move to history.", null));
            }

            // 3. Manual Validation for Mandatory Fields
            // User form-la enter panna values-a check panrom
            if (req.getFinishedQuantity() == null || req.getScrapQuantity() == null || req.getScrapType() == null) {
                return ResponseEntity.status(400).body(new BaseResponse<>(400, "Error: Please fill all fields (Finished Qty, Scrap Qty, Type) before completing.", null));
            }

            // 4. Update Existing Record with User Input
            existingRecord.setFinishedQuantity(req.getFinishedQuantity());
            existingRecord.setBalanceQuantity(req.getBalanceQuantity());
            existingRecord.setScrapQuantity(req.getScrapQuantity());
            existingRecord.setScrapType(req.getScrapType());
            existingRecord.setCompletedDate(req.getCompletedDate()); // Date from screen
            existingRecord.setRemarks(req.getRemarks());
            existingRecord.setStatus("COMPLETED");

            // 5. Save Updated Record & Update JobOrder Status
            Production saved = productionRepo.save(existingRecord);
            jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "COMPLETED");

            return ResponseEntity.ok(new BaseResponse<>(200, "Production marked as COMPLETED successfully", saved));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse<>(500, "System Error: " + e.getMessage(), null));
        }
    }

    private boolean isAnyFieldEmpty(Production p) {
        return p.getFinishedQuantity() == null || p.getFinishedQuantity().isEmpty() ||
               p.getScrapQuantity() == null || p.getScrapQuantity().isEmpty() ||
               p.getProductionDate() == null || p.getProductionDate().isEmpty() ||
               p.getScrapType() == null || p.getScrapType().equals("Select");
    }
    // STEP 4: Fetch data for "In Progress" or "Completed" Tabs
    @GetMapping("/list-all-status/{status}")
    public ResponseEntity<BaseResponse<List<Production>>> getListByStatus(@PathVariable String status) {
        List<Production> list = productionRepo.findProductionByStatus(status.toUpperCase());
        return ResponseEntity.ok(new BaseResponse<>(200, "Data fetched", list));
    }
}