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
        jobOrderRepo.updateStatusByJobNumber(jobNumber, "IN PROGRESS");
        return ResponseEntity.ok(new BaseResponse<>(200, "Status updated to In Progress", null));
    }

    @PostMapping("/submit-production-completed")
    public ResponseEntity<BaseResponse<?>> submitProduction(@RequestBody Production req) {
        try {
            // 1. Mandatory Field Validation
            // This ensures the user cannot skip fields in your "Production Update" screen
            if (isAnyFieldEmpty(req)) {
                return ResponseEntity.status(400).body(new BaseResponse<>(400, 
                    "Error: Please fill all production fields before updating.", null));
            }

            // 2. Status Validation
            // If the user hasn't selected 'COMPLETED', throw an error (as per your requirement)
            if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
                return ResponseEntity.status(400).body(new BaseResponse<>(400, 
                    "Error: Status must be 'COMPLETED' to finalize this record.", null));
            }

            // 3. Auto-generate PRO number only if it's a new entry
            if (req.getProductionNumber() == null || req.getProductionNumber().isEmpty()) {
                long count = productionRepo.count() + 1;
                req.setProductionNumber(String.format("PRO-%04d-2026", count));
            }

            // 4. Save and Sync with JobOrder Table
            Production saved = productionRepo.save(req);
            jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "COMPLETED");

            return ResponseEntity.ok(new BaseResponse<>(200, "Production details have been updated successfully", saved));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "System Error: " + e.getMessage(), null));
        }
    }

    // Helper method to keep code clean
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