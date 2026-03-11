package com.productionCreation;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/production")
public class ProductionController {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private DailyUpdateRepository dailyUpdateRepository;

    @Autowired
    private JobOrderRepository jobOrderRepository; // <--- INJECT JOB ORDER REPOSITORY

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
            Optional<JobOrder> jobData = jobOrderRepository.findByJobOrderNumber(pro.getJobOrder());

            if (jobData.isPresent()) {
                // Table-ilirundhu customer name-ai eduthu production-la set pannuvom
                pro.setCustomerName(jobData.get().getCustomerName());
            } else {
                // Job order-ae illaiyendraal error tharuvom
                response.put("status", 400);
                response.put("message", "Invalid Job Order Number");
                return ResponseEntity.status(400).body(response);
            }

            pro.setCreatedDate(LocalDateTime.now());

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

    @GetMapping("/all")
    public ResponseEntity<Object> getAllProductions() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Fetch all records from repository
            List<Production> list = productionRepository.findAll();

            // 2. Check if list is empty
            if (list.isEmpty()) {
                response.put("status", 200);
                response.put("message", "No Production records found");
                response.put("data", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            // 3. Success Response
            response.put("status", 200);
            response.put("message", "All Productions Fetched Successfully");
            response.put("data", list);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 4. Error Handling
            response.put("status", 500);
            response.put("message", "Server Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/daily-report/{productionId}")
    public ResponseEntity<?> getDailyReport(@PathVariable String productionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            return productionRepository.findByProductionNumber(productionId).map(pro -> {

                // 1. Table-la irukkira normal updates (PRD-13-2026, 150.5, etc.)
                List<DailyProductionUpdate> tableReport = dailyUpdateRepository
                        .findByProductionIdOrderByEntryTimestampDesc(productionId);

                // 2. Response-ai sariyaana structure-la set pannuvom
                response.put("status", 200);
                response.put("message", "Daily Report Fetched");

                // Table-la ulla updates 'data' array-la varum (Repeat aagaadhu)
                response.put("data", tableReport);

                // Complete API vazhiya vandha logic 'history' array-la oru vaatti mattum varum
                response.put("history", pro.getHistory());

                return ResponseEntity.ok(response);

            }).orElseGet(() -> {
                response.put("status", 404);
                response.put("message", "Production Number " + productionId + " not found");
                return ResponseEntity.status(404).body(response);
            });

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Server Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/daily-update/delete/{id}")
    public ResponseEntity<Object> deleteDailyUpdate(@PathVariable Long id) {
        try {
            dailyUpdateRepository.deleteById(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "History record deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/complete/{productionId}")
    public ResponseEntity<?> completeProduction(@PathVariable String productionId,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        return productionRepository.findByProductionNumber(productionId).map(pro -> {
            // --- NEW DUPLICATE CHECK ---
            if ("COMPLETED".equalsIgnoreCase(pro.getStatus())) {
                response.put("status", 400);
                response.put("message",
                        "Duplicate Entry: This Production ( " + productionId + " ) is already COMPLETED.");
                return ResponseEntity.status(400).body(response);
            }

            try {
                // 1. Status Change
                pro.setStatus("COMPLETED");

                // 2. Safe Parsing from String JSON (since you send strings)
                Double finished = data.get("finishedQuantity") != null
                        ? Double.parseDouble(data.get("finishedQuantity").toString())
                        : 0.0;
                Double balance = data.get("balanceQuantity") != null
                        ? Double.parseDouble(data.get("balanceQuantity").toString())
                        : 0.0;
                Double scrap = data.get("scrapQuantity") != null
                        ? Double.parseDouble(data.get("scrapQuantity").toString())
                        : 0.0;

                String sType = data.get("scrapType") != null ? data.get("scrapType").toString() : "";
                String rem = data.get("remark") != null ? data.get("remark").toString() : "";

                // 3. Inner Class Entry for History Card
                Production.DailyUpdate finalLog = new Production.DailyUpdate(finished, balance, scrap, sType, rem);
                pro.getHistory().add(finalLog);

                // 4. Save the Final State
                productionRepository.save(pro);

                response.put("status", 200);
                response.put("message", "Production " + productionId + " Completed Successfully");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("status", 400);
                response.put("message", "Check your input values: " + e.getMessage());
                return ResponseEntity.status(400).body(response);
            }
        }).orElseGet(() -> {
            response.put("status", 404);
            response.put("message", "Production Number " + productionId + " not found");
            return ResponseEntity.status(404).body(response);
        });
    }

    @PostMapping("/daily-update")
    public ResponseEntity<?> createDailyUpdate(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            DailyProductionUpdate update = new DailyProductionUpdate();

            // JSON-ilirundhu values-ai parse pannuvom
            update.setProductionId(data.get("productionId").toString());
            update.setFinishedQuantity(data.get("finishedQuantity").toString());
            update.setBalanceQuantity(data.get("balanceQuantity").toString());
            update.setScrapQuantity(data.get("scrapQuantity").toString());
            update.setProductionDate(data.get("productionDate").toString());
            update.setRemark(data.get("remark") != null ? data.get("remark").toString() : "");

            // Timestamp auto-aa set pannuvom
            update.setEntryTimestamp(LocalDateTime.now());

            // Save to daily_production_updates table
            DailyProductionUpdate savedUpdate = dailyUpdateRepository.save(update);

            response.put("status", 200);
            response.put("message", "Daily update saved successfully");
            response.put("data", savedUpdate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}