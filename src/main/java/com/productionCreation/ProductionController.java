package com.productionCreation;

import com.jobOrderCreation.BaseResponse;
import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
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

    // 1. FETCH PENDING (From JobOrder Table)
    @GetMapping("/pending-from-orders")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getPendingFromOrders() {
        List<JobOrder> pending = jobOrderRepo.findByStatus("PENDING");
        return ResponseEntity.ok(new BaseResponse<>(200, "Pending Jobs Fetched", pending));
    }

    // 2. UPDATE PROGRESS (The "Update" Button)
    @PostMapping("/update-progress")
    public ResponseEntity<BaseResponse<?>> updateProduction(@RequestBody Production req) {
        try {
            // Auto-generate Production Number: PRO-0001-2026
            if (req.getProductionNumber() == null || req.getProductionNumber().isEmpty()) {
                long count = productionRepo.count() + 1;
                req.setProductionNumber(String.format("PRO-%04d-2026", count));
            }

            Production saved = productionRepo.save(req);

            // SYNC: Update Job Order table status based on production status
            if (req.getJobOrderNumber() != null) {
                jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), req.getStatus().toUpperCase());
            }

            return ResponseEntity.ok(new BaseResponse<>(200, "Success", saved));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, e.getMessage(), null));
        }
    }

    // 3. FETCH BY TAB (List for Completed/In Progress)
    @GetMapping("/list/{status}")
    public ResponseEntity<BaseResponse<List<Production>>> getProductionByStatus(@PathVariable String status) {
        // This uses the JOIN logic to ensure data integrity between tables
        List<Production> list = productionRepo.findProductionWithJoins(status.toUpperCase());
        return ResponseEntity.ok(new BaseResponse<>(200, "Data fetched via Join Query", list));
    }
}