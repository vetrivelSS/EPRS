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

    // --- CREATE ---
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<?>> createJobOrder(
            @RequestParam("workOrderNumber") String workOrderNumber,
            @RequestParam("customerName") String customerName,
            @RequestParam("hsnCode") String hsnCode,
            @RequestParam("material") String material,
            @RequestParam("thickness") String thickness,
            @RequestParam("process") String process,
            @RequestParam("quantityNo") String quantityNo,
            @RequestParam("quantityKg") String quantityKg,
            @RequestParam("location") String location,
            @RequestParam("priority") String priority,
            @RequestParam("assignedOperators") List<String> assignedOperators,
            @RequestParam("jobCreatedDate") String jobCreatedDate) {

        try {
            JobOrder job = new JobOrder();
            // ... (your existing mapping logic) ...
            job.setWorkOrderNumber(workOrderNumber);
            job.setCustomerName(customerName);
            job.setHsnCode(hsnCode);
            job.setMaterial(material);
            job.setThickness(thickness);
            job.setProcess(process);
            job.setQuantityNo(quantityNo);
            job.setQuantityKg(quantityKg);
            job.setLocation(location);
            job.setPriority(priority);
            job.setAssignedOperators(assignedOperators);
            job.setJobCreatedDate(jobCreatedDate);

            long count = repository.count() + 1;
            job.setJobOrderNumber(String.format("JOB-%04d", count));

            JobOrder saved = repository.save(job);
            return ResponseEntity.ok(new BaseResponse<>(200, "Job Order Created Successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "Error: " + e.getMessage(), null));
        }
    }

    // --- GET ALL (For Job History List) ---
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getAllJobs() {
        List<JobOrder> jobs = repository.findAll();
        return ResponseEntity.ok(new BaseResponse<>(200, "Data fetched successfully", jobs));
    }

    // --- GET BY ID (For opening the Edit Screen) ---
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<JobOrder>> getJobById(@PathVariable Long id) {
        return repository.findById(id)
                .map(job -> ResponseEntity.ok(new BaseResponse<>(200, "Job found", job)))
                .orElse(ResponseEntity.status(404).body(new BaseResponse<>(404, "Job not found", null)));
    }

    // --- UPDATE (For the "Edit Job Order" screen) ---
    @PutMapping("/update/{id}")
    public ResponseEntity<BaseResponse<?>> updateJobOrder(
            @PathVariable Long id,
            @RequestParam("customerName") String customerName,
            @RequestParam("hsnCode") String hsnCode,
            @RequestParam("material") String material,
            @RequestParam("thickness") String thickness,
            @RequestParam("process") String process,
            @RequestParam("quantityNo") String quantityNo,
            @RequestParam("quantityKg") String quantityKg,
            @RequestParam("location") String location,
            @RequestParam("priority") String priority,
            @RequestParam("assignedOperators") List<String> assignedOperators) {

        try {
            Optional<JobOrder> existingJob = repository.findById(id);
            if (existingJob.isPresent()) {
                JobOrder job = existingJob.get();
                // Update fields
                job.setCustomerName(customerName);
                job.setHsnCode(hsnCode);
                job.setMaterial(material);
                job.setThickness(thickness);
                job.setProcess(process);
                job.setQuantityNo(quantityNo);
                job.setQuantityKg(quantityKg);
                job.setLocation(location);
                job.setPriority(priority);
                job.setAssignedOperators(assignedOperators);

                JobOrder updated = repository.save(job);
                return ResponseEntity.ok(new BaseResponse<>(200, "Job Order Updated Successfully", updated));
            } else {
                return ResponseEntity.status(404).body(new BaseResponse<>(404, "Job Order not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "Update failed: " + e.getMessage(), null));
        }
    }
}