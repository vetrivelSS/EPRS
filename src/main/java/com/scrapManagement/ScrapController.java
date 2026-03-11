package com.ScrapManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.productionCreation.Production;
import com.productionCreation.ProductionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/scrap")
@CrossOrigin(origins = "*") // Allows your frontend to call this API
public class ScrapController {

    @Autowired
    private ProductionRepository productionRepository;

    @GetMapping("/scrap-report")
    public ResponseEntity<Object> getScrapReport(@RequestParam String scrapType) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Production> allProductions = productionRepository.findAll();
            List<Map<String, Object>> reportList = new ArrayList<>();

            // 1. Initialize Grand Totals
            double totalAvailable = 0.0;
            double totalSold = 0.0;

            for (Production p : allProductions) {
                for (Production.DailyUpdate item : p.getHistory()) {
                    if (scrapType.equalsIgnoreCase(item.getScrapType())) {

                        double originalScrap = (item.getScrapQty() != null) ? item.getScrapQty() : 0.0;
                        double soldQty = (item.getSoldScrapQty() != null) ? item.getSoldScrapQty() : 0.0;
                        double availableNow = originalScrap - soldQty;

                        // 2. Add to Grand Totals
                        totalAvailable += availableNow;
                        totalSold += soldQty;

                        // 3. Row Details (No totals here)
                        Map<String, Object> row = new HashMap<>();
                        row.put("productionNumber", p.getProductionNumber());
                        row.put("customerName", p.getCustomerName());
                        row.put("material", p.getMaterial());
                        row.put("thickness", p.getThickness());
                        row.put("process", p.getProcess());
                        row.put("availableQuantity", availableNow);
                        row.put("soldQuantity", soldQty);
                        row.put("scrapType", item.getScrapType());
                        row.put("productionDate", p.getCreatedDate());
                        row.put("lastUpdate", item.getUpdateTime());

                        reportList.add(row);
                    }
                }
            }

            // 4. Construct the Data Object with ONE set of totals
            Map<String, Object> data = new HashMap<>();
            data.put("grandTotalAvailable", totalAvailable); // Calculated once
            data.put("grandTotalSold", totalSold); // Calculated once
            data.put("scrapDetails", reportList);

            response.put("status", 200);
            response.put("message", scrapType + " Report Generated");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/sell-scrap/{productionNumber}")
    public ResponseEntity<Object> sellScrap(@PathVariable String productionNumber,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Production pro = productionRepository.findByProductionNumber(productionNumber)
                    .orElseThrow(() -> new Exception("Production Number " + productionNumber + " not found"));

            // Get Scrap Type from body (e.g., "SALE")
            String scrapTypeFromBody = request.get("scrapType") != null ? request.get("scrapType").toString() : "";

            boolean updated = false;
            Production.DailyUpdate soldRecord = null;

            for (Production.DailyUpdate history : pro.getHistory()) {
                // Find the record matching the type from Postman and ensure it's not already
                // SOLD
                if (scrapTypeFromBody.equalsIgnoreCase(history.getScrapType()) &&
                        !"SOLD".equalsIgnoreCase(history.getScrapStatus())) {

                    double availableAmount = (history.getScrapQty() != null) ? history.getScrapQty() : 0.0;

                    // 1. Move ALL available quantity to Sold field
                    history.setSoldScrapQty(availableAmount);

                    // 2. Clear the Available field
                    history.setScrapQty(0.0);

                    // 3. Update Status and Time
                    history.setScrapStatus("SOLD");
                    history.setUpdateTime(LocalDateTime.now());

                    soldRecord = history;
                    updated = true;
                    break;
                }
            }

            if (updated) {
                productionRepository.save(pro);

                // 4. Build Response with full Production details as requested
                Map<String, Object> details = new HashMap<>();
                details.put("productionNumber", pro.getProductionNumber());
                details.put("customerName", pro.getCustomerName());
                details.put("material", pro.getMaterial());
                details.put("thickness", pro.getThickness());
                details.put("process", pro.getProcess());
                details.put("soldQuantity", soldRecord.getSoldScrapQty());
                details.put("status", "MOVED TO SOLD");

                response.put("status", 200);
                response.put("message", "Scrap moved successfully");
                response.put("data", details);

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(404).body(Collections.singletonMap("message",
                    "No matching AVAILABLE record found for type: " + scrapTypeFromBody));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/sold-report")
    public ResponseEntity<Object> getSoldReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Production> all = productionRepository.findAll();
            List<Map<String, Object>> details = new ArrayList<>();
            double totalSold = 0.0;

            for (Production p : all) {
                for (Production.DailyUpdate item : p.getHistory()) {
                    // FILTER: Only show items marked as SOLD
                    if ("SOLD".equalsIgnoreCase(item.getScrapStatus())) {

                        totalSold += item.getSoldScrapQty();

                        Map<String, Object> row = new HashMap<>();
                        row.put("productionNumber", p.getProductionNumber());
                        row.put("customerName", p.getCustomerName());
                        row.put("soldQuantity", item.getSoldScrapQty());
                        row.put("soldDate", item.getUpdateTime());
                        details.add(row);
                    }
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("grandTotalSold", totalSold); // One total
            summary.put("details", details);

            response.put("status", 200);
            response.put("data", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
