package com.productionCreation;

import com.jobOrderCreation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/production")
@CrossOrigin("*")
public class ProductionController {

    @Autowired
    private ProductionRepository productionRepo;

    @Autowired
    private JobOrderRepository jobOrderRepo;

    // 1. Fetch pending jobs from JobOrder table
    @GetMapping("/pending-from-joborders")
    public ResponseEntity<BaseResponse<List<JobOrder>>> getPendingJobs() {
        List<JobOrder> pending = jobOrderRepo.findPendingJobsWithJoin("PENDING");
        return ResponseEntity.ok(new BaseResponse<>(200, "Pending Jobs Fetched", pending));
    }

    // 2. Start Job: Moves data from JobOrder to Production initially
    @PostMapping("/start-inprogress/{jobNumber}")
    @Transactional  
    public ResponseEntity<BaseResponse<?>> startProgress(@PathVariable String jobNumber) {
        try {
            JobOrder job = jobOrderRepo.findByJobOrderNumber(jobNumber);
            if (job == null) return ResponseEntity.status(404).body(new BaseResponse<>(404, "Job Not Found", null));

            jobOrderRepo.updateStatusByJobNumber(jobNumber, "IN PROGRESS");

            // This creates the record with CustomerName, Material, etc.
            Production newProd = ProductionMapper.copyJobToProduction(job, productionRepo.count());
            Production saved = productionRepo.saveAndFlush(newProd); 

            return ResponseEntity.ok(new BaseResponse<>(200, "Job Started", saved));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResponse<>(400, "Error: " + e.getMessage(), null));
        }
    }

  
//     --- INNER CLASS ---
  private static class ProductionMapper {
      public static Production copyJobToProduction(JobOrder job, long count) {
          Production p = new Production();
          p.setJobOrderNumber(job.getJobOrderNumber());
          p.setCustomerName(job.getCustomerName());
          p.setMaterial(job.getMaterial());
          p.setThickness(job.getThickness());
          p.setProcess(job.getProcess());
          p.setStatus("IN PROGRESS");
          p.setProductionNumber(String.format("PRO-%04d-2026", count + 1));
          // Default quantities
          p.setFinishedQuantity("0");
          p.setScrapQuantity("0");
          return p;
      }
  }
  @PostMapping("/submit-production-completed")
  @Transactional
  public ResponseEntity<BaseResponse<?>> submitProduction(@RequestBody Production req) {
      try {
          // ... (Keep your validation logic here) ...

          // 1. Fetch the EXISTING record from the database first
          // This 'existing' object currently has the correct Customer, Material, etc.
          Production existing = productionRepo.findByJobOrderNumber(req.getJobOrderNumber());
          
          if (existing == null) {
              return ResponseEntity.status(404).body(new BaseResponse<>(404, "Production record not found", null));
          }

          // 2. UPDATE ONLY the specific fields you received from the form
          // By NOT calling existing.setCustomerName(req.getCustomerName()), 
          // the original value stays safe in the 'existing' object.
          existing.setFinishedQuantity(req.getFinishedQuantity());
          existing.setBalanceQuantity(req.getBalanceQuantity());
          existing.setScrapQuantity(req.getScrapQuantity());
          existing.setScrapType(req.getScrapType());
          existing.setProductionDate(req.getProductionDate());
          existing.setRemarks(req.getRemarks());
          existing.setStatus("COMPLETED");

          // 3. Save the 'existing' object (NOT the 'req' object)
          Production saved = productionRepo.saveAndFlush(existing);
          
          // 4. Update the JobOrder table status
          jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "COMPLETED");

          return ResponseEntity.ok(new BaseResponse<>(200, "Production Updated Successfully", saved));

      } catch (Exception e) {
          return ResponseEntity.status(400).body(new BaseResponse<>(400, "System Error: " + e.getMessage(), null));
      }
  }

    private boolean isAnyFieldEmpty(Production p) {
        return p.getFinishedQuantity() == null || p.getFinishedQuantity().isEmpty() ||
               p.getScrapQuantity() == null || p.getScrapQuantity().isEmpty() ||
               p.getProductionDate() == null || p.getProductionDate().isEmpty() ||
               p.getScrapType() == null || p.getScrapType().equals("Select");
    }

    @GetMapping("/list-all-status/{status}")
    public ResponseEntity<BaseResponse<List<Production>>> getListByStatus(@PathVariable String status) {
        List<Production> list = productionRepo.findByStatus(status.toUpperCase());
        return ResponseEntity.ok(new BaseResponse<>(200, "Fetched", list));
    }
}