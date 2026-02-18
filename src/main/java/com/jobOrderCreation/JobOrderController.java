package com.jobOrderCreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/joborders")
@CrossOrigin("*") 
public class JobOrderController {

    @Autowired
    private JobOrderRepository repository;

    // --- 1. CREATE (Automatically sets status to PENDING) ---
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<?>> createJobOrder(@RequestBody JobOrder job) {
        try {
            // Current count + 1 eduthu format panrom
            long count = repository.count() + 1;
            String generatedNumber = String.format("JOB-%04d", count);
            
            job.setJobOrderNumber(generatedNumber);
            job.setStatus("PENDING");

            // Database-la save pandrom
            JobOrder saved = repository.save(job);

            // Saved object-la ippo ID-yum irukkum, andha JobOrderNumber-um irukkum
            return ResponseEntity.ok(new BaseResponse<>(200, "Job Order Created Successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "Error: " + e.getMessage(), null));
        }
    }

    // --- 2. GET PENDING (For Production Dashboard) ---
    @GetMapping("/production/pending")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getPendingJobs() {
        List<JobOrder> pendingJobs = repository.findByStatus("PENDING");
        return ResponseEntity.ok(new BaseResponse<>(200, "Pending jobs fetched successfully", pendingJobs));
    }

    // --- 3. UPDATE (Edit Screen - Keeps status as PENDING) ---
    @PutMapping("/update/{id}")
    public ResponseEntity<BaseResponse<?>> updateJobOrder(@PathVariable Long id, @RequestBody JobOrder jobDetails) {
        try {
            Optional<JobOrder> existingJob = repository.findById(id);
            
            if (existingJob.isPresent()) {
                JobOrder job = existingJob.get();
                
                job.setCustomerName(jobDetails.getCustomerName());
                job.setHsnCode(jobDetails.getHsnCode());
                job.setMaterial(jobDetails.getMaterial());
                job.setThickness(jobDetails.getThickness());
                job.setProcess(jobDetails.getProcess());
                job.setQuantityNo(jobDetails.getQuantityNo());
                job.setQuantityKg(jobDetails.getQuantityKg());
                job.setLocation(jobDetails.getLocation());
                job.setPriority(jobDetails.getPriority());
                job.setAssignedOperators(jobDetails.getAssignedOperators());

                JobOrder updated = repository.save(job);
                return ResponseEntity.ok(new BaseResponse<>(200, "Job Order Updated Successfully", updated));
            } else {
                return ResponseEntity.status(404).body(new BaseResponse<>(404, "Job Order not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "Update failed: " + e.getMessage(), null));
        }
    }

    // --- 4. GET ALL (For History) ---
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getAllJobs() {
        List<JobOrder> jobs = repository.findAll();
        return ResponseEntity.ok(new BaseResponse<>(200, "Data fetched successfully", jobs));
    }
}