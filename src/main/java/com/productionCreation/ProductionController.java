package com.productionCreation;

import java.time.Year;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private ProductionResultRepository productionResultRepository;

    @Autowired
    private JobOrderRepository jobOrderRepository; // <--- INJECT JOB ORDER REPOSITORY

    @GetMapping("/lookup-job/{jobNo}")
    public ResponseEntity<Object> lookupJobDetails(@PathVariable String jobNo) {
        try {
            // 1. Fetch the full object from the database
            JobOrder job = jobOrderRepository.findByJobOrderNumber(jobNo);

            // 2. If not found, throw 404
            if (job == null) {
                return ResponseEntity.status(404)
                        .body("Error: Job Number [" + jobNo + "] does not exist.");
            }

            // 3. Create a Map to hold ONLY the 5 fields you want
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("jobOrder", job.getJobOrderNumber());
            response.put("material", job.getMaterial());
            response.put("thickness", job.getThickness());
            response.put("process", job.getProcess());
            response.put("quantityKg", job.getQuantityKg());
            response.put("status", job.getStatus());

            // 4. Return only the filtered map
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server Error: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createProduction(@RequestBody Production pro) {

        Map<String, Object> response = new HashMap<>();

        try {

            // 1. CHECK DUPLICATE JOB ORDER
            if (productionRepository.countByJobOrder(pro.getJobOrder()) > 0) {

                response.put("status", 400);
                response.put("message", "Production already created for JobOrder: " + pro.getJobOrder());
                response.put("data", null);

                return ResponseEntity.status(400).body(response);
            }

            // 2. SAVE FIRST (to generate ID)
            Production saved = productionRepository.save(pro);

            // 3. GENERATE PRODUCTION NUMBER
            int year = Year.now().getValue();
            String formattedId = String.format("%02d", saved.getId());
            String productionNumber = "PRD-" + formattedId + "-" + year;

            saved.setProductionNumber(productionNumber);

            // 4. SAVE AGAIN
            Production finalData = productionRepository.save(saved);

            // 5. SUCCESS RESPONSE
            response.put("status", 200);
            response.put("message", "Production Created Successfully");
            response.put("data", finalData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("status", 500);
            response.put("message", "Server Error: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }

    @RestController
    @RequestMapping("/api/production")
    public class ProductionResultController {

        @Autowired
        private ProductionResultRepository productionResultRepository;

        // POST API
        @PostMapping("/add-result")
        public ResponseEntity<Object> addResult(@RequestBody ProductionResult result) {

            Map<String, Object> response = new HashMap<>();

            try {

                ProductionResult saved = productionResultRepository.save(result);

                response.put("status", 200);
                response.put("message", "Production Result Saved");
                response.put("data", saved);

                return ResponseEntity.ok(response);

            } catch (Exception e) {

                response.put("status", 500);
                response.put("message", "Server Error: " + e.getMessage());
                response.put("data", null);

                return ResponseEntity.status(500).body(response);
            }
        }

        // GET ALL API
        @GetMapping("/results")
        public ResponseEntity<Object> getAllResults() {

            Map<String, Object> response = new HashMap<>();

            try {

                List<ProductionResult> list = productionResultRepository.findAll();

                response.put("status", 200);
                response.put("message", "Production Results List");
                response.put("data", list);

                return ResponseEntity.ok(response);

            } catch (Exception e) {

                response.put("status", 500);
                response.put("message", "Server Error: " + e.getMessage());
                response.put("data", null);

                return ResponseEntity.status(500).body(response);
            }
        }
    }
    @Autowired
private ProductionSummaryRepository productionSummaryRepository;

@PostMapping("/add-summary")
public ResponseEntity<Object> addSummary(@RequestBody ProductionSummary summary) {

    Map<String, Object> response = new HashMap<>();

    try {

        ProductionSummary saved = productionSummaryRepository.save(summary);

        response.put("status", 200);
        response.put("message", "Production Summary Saved Successfully");
        response.put("data", saved);

        return ResponseEntity.ok(response);

    } catch (Exception e) {

        response.put("status", 500);
        response.put("message", "Server Error: " + e.getMessage());
        response.put("data", null);

        return ResponseEntity.status(500).body(response);
    }
}
@GetMapping("/summary")
public ResponseEntity<Object> getAllSummary() {

    Map<String, Object> response = new HashMap<>();

    try {

        List<ProductionSummary> list = productionSummaryRepository.findAll();

        response.put("status", 200);
        response.put("message", "Production Summary List");
        response.put("data", list);

        return ResponseEntity.ok(response);

    } catch (Exception e) {

        response.put("status", 500);
        response.put("message", "Server Error: " + e.getMessage());
        response.put("data", null);

        return ResponseEntity.status(500).body(response);
    }
}
}