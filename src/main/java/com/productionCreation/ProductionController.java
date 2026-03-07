package com.productionCreation;

import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
import com.jobOrderCreation.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.HashMap;
// import java.util.Map;
// import com.productionCreation.ProductionDailyProgressRepository; 
// import java.util.HashMap; 
import java.util.List;
// import java.util.ArrayList;

@RestController
@RequestMapping("/api/production")
@CrossOrigin("*")
public class ProductionController {

    @Autowired
    private ProductionRepository productionRepo;

    @Autowired
    private JobOrderRepository jobOrderRepo;

    @Autowired
    private ProductionDailyProgressRepository dailyRepo;

    @Autowired
    private MaterialBalanceRepository materialRepo;

    // START PRODUCTION: Prevents duplicate starts

    @PostMapping("/create-production")
    public ResponseEntity<Object> createProduction(@RequestBody Production req) {
        try {
            // 1. STRICT FIELD VALIDATION
            if (req.getJobOrderNumber() == null || req.getJobOrderNumber().trim().isEmpty() ||
                    req.getMaterial() == null || req.getMaterial().trim().isEmpty() ||
                    req.getReceivedQuantityKg() == null) {

                return ResponseEntity.status(400)
                        .body(new BaseResponse(400, "Validation Error: Missing mandatory fields.", null));
            }

            // 2. CHECK IF JOB ORDER EXISTS
            JobOrder job = jobOrderRepo.findByJobOrderNumber(req.getJobOrderNumber());
            if (job == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "Not Found: Job Order Number does not exist.", null));
            }

            // 3. CHECK FOR DUPLICATES
            Production existingProd = productionRepo.findByJobOrderNumber(req.getJobOrderNumber());
            if (existingProd != null) {
                return ResponseEntity.status(409)
                        .body(new BaseResponse(409, "Conflict: Production record already exists for this Job Order.",
                                null));
            }

            // 4. MAPPING
            Production newProd = new Production();
            newProd.setJobOrderNumber(req.getJobOrderNumber());
            newProd.setMaterial(req.getMaterial());
            newProd.setThickness(req.getThickness());
            newProd.setProcess(req.getProcess());
            newProd.setStatus(req.getStatus());
            newProd.setReceivedQuantityKg(req.getReceivedQuantityKg());
            newProd.setRemarks(req.getRemarks());

            // --- UPDATED STATUS ---
            newProd.setStatus("INPROGRESS");

            // 5. SEQUENTIAL PRODUCTION NUMBER GENERATION (PRD-01-2026)
            long currentCount = productionRepo.count();
            String generatedNo = String.format("PRD-%02d-%d",
                    (currentCount + 1),
                    java.time.LocalDate.now().getYear());
            newProd.setProductionNumber(generatedNo);

            // 6. SAVE
            Production saved = productionRepo.save(newProd);

            return ResponseEntity.status(201)
                    .body(new BaseResponse(201, "Production Created Successfully", saved));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400, "Database Error: Data integrity violation.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Internal Server Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/fetch-job-details/{jobOrderNumber}")
    public ResponseEntity<Object> fetchJobDetails(@PathVariable String jobOrderNumber) {
        try {
            // 1. Get Job Order from Database
            JobOrder job = jobOrderRepo.findByJobOrderNumber(jobOrderNumber);

            if (job == null) {
                return ResponseEntity.status(404).body(new BaseResponse(404, "Job Order not found", null));
            }

            // 2. Put only the 4 required fields into a Map
            Map<String, Object> details = new HashMap<>();
            details.put("material", job.getMaterial());
            details.put("thickness", job.getThickness());
            details.put("process", job.getProcess());
            details.put("receivedQuantityKg", job.getQuantityKg());
            details.put("createdDate", java.time.LocalDate.now().toString());

            // 3. Return the response
            return ResponseEntity.ok(new BaseResponse(200, "Fetched successfully", details));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/get-production/{productionNumber}")
    public ResponseEntity<Object> getProductionByNumber(@PathVariable String productionNumber) {
        try {
            // 1. Validation: Ensure the path variable isn't empty
            if (productionNumber == null || productionNumber.trim().isEmpty()) {
                return ResponseEntity.status(400)
                        .body(new BaseResponse(400, "Validation Error: Production Number is required in the URL.",
                                null));
            }

            // 2. Execution: Fetch from Database
            Production prod = productionRepo.findByProductionNumber(productionNumber);

            // 3. Logic Check: If the record doesn't exist
            if (prod == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404,
                                "Not Found: No production record found for '" + productionNumber + "'.", null));
            }

            // 4. Success Response
            return ResponseEntity.status(200)
                    .body(new BaseResponse(200, "Production Details Retrieved Successfully", prod));

        }
        // CATCH BLOCK 1: Database Connectivity or Query Syntax Errors
        catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500,
                            "Database Error: Could not connect to the database or execute the query.", null));
        }
        // CATCH BLOCK 2: Handles unexpected null values in the logic
        catch (NullPointerException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Logic Error: An unexpected null value was encountered.", null));
        }
        // CATCH BLOCK 3: General Catch-All for any other Exception
        catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Internal Server Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/add-daily-progress")
    public ResponseEntity<Object> addDaily(@RequestBody ProductionDailyProgress req) {
        try {
            // 1. VALIDATION
            if (req.getProductionNumber() == null || req.getFinishedQuantity() == null) {
                return ResponseEntity.status(400)
                        .body(new BaseResponse(400,
                                "Validation Error: Production Number and Finished Qty are required.", null));
            }

            // 2. FIND MAIN PRODUCTION RECORD
            Production mainProd = productionRepo.findByProductionNumber(req.getProductionNumber());
            if (mainProd == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "Not Found: Main Production record not found.", null));
            }

            // 3. AUTO-STATUS LOGIC (Determine status before saving)
            String newStatus = (req.getRemarks() != null && req.getRemarks().equalsIgnoreCase("COMPLETE"))
                    ? "COMPLETED"
                    : "IN_PROGRESS";

            // 4. SAVE DAILY PROGRESS
            // Set the status on the daily object so it's NOT null in the database/response
            req.setStatus(newStatus);
            ProductionDailyProgress savedDaily = dailyRepo.save(req);

            // 5. UPDATE MAIN PRODUCTION TABLE
            mainProd.setStatus(newStatus);
            productionRepo.save(mainProd);

            return ResponseEntity.status(201)
                    .body(new BaseResponse(201, "Daily Progress Saved. Status is now: " + newStatus, savedDaily));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400, "Data Error: Constraint violation.", null));
        } catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Database Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Unknown Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/add-material-balance")
    public ResponseEntity<Object> addMaterialBalance(@RequestBody MaterialBalance req) {
        try {
            // 1. MANDATORY FIELD VALIDATION
            if (req.getProductionNumber() == null || req.getReceivedQuantityKg() == null) {
                return ResponseEntity.status(400)
                        .body(new BaseResponse(400,
                                "Validation Error: Production Number and Received Qty are mandatory.", null));
            }

            // 2. FIND AND LOCK CHECK
            Production mainProd = productionRepo.findByProductionNumber(req.getProductionNumber());
            if (mainProd == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404,
                                "Not Found: Record for " + req.getProductionNumber() + " doesn't exist.", null));
            }

            // --- LOCK LOGIC ---
            if ("COMPLETED".equalsIgnoreCase(mainProd.getStatus())) {
                return ResponseEntity.status(403)
                        .body(new BaseResponse(403,
                                "Access Denied: This production is already COMPLETED and cannot be modified.", null));
            }

            // 3. CALCULATIONS
            double finished = (req.getFinishedQuantity() != null) ? req.getFinishedQuantity() : 0.0;
            double scrap = (req.getScrapQuantity() != null) ? req.getScrapQuantity() : 0.0;
            double calcBalance = req.getReceivedQuantityKg() - (finished + scrap);

            req.setBalanceQuantity(calcBalance);
            req.setStatus("COMPLETED");

            // 4. SAVE (Main Table first, then Balance Table)
            mainProd.setStatus("COMPLETED");
            productionRepo.save(mainProd);
            MaterialBalance saved = materialRepo.save(req);

            return ResponseEntity.status(201)
                    .body(new BaseResponse(201, "Material Balance Saved. Status: COMPLETED.", saved));

        }
        // ERROR 1: Constraint Violation (e.g. number too large for database column)
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400, "Data Error: Invalid format or database constraint violation.", null));
        }
        // ERROR 2: Database Connectivity (e.g. MySQL is offline)
        catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Database Error: Could not connect to the database server.", null));
        }
        // ERROR 3: Null pointer safety (if a variable is unexpectedly null)
        catch (NullPointerException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Logic Error: A null value was encountered during processing.", null));
        }
        // ERROR 4: Catch-all for everything else
        catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Unknown Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/get-material-balance/{productionNumber}")
    public ResponseEntity<Object> getMaterialBalance(@PathVariable String productionNumber) {
        try {
            // 1. Fetch data from repository
            List<MaterialBalance> balanceList = materialRepo.findByProductionNumber(productionNumber);

            // 2. Check if data exists
            if (balanceList.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "No material balance records found for: " + productionNumber,
                                null));
            }

            // 3. Return success
            return ResponseEntity.status(200)
                    .body(new BaseResponse(200, "Material Balance details fetched successfully.", balanceList));

        } catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Database Error: Could not reach the server.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/get-all-productions")
    public ResponseEntity<Object> getAllProductions() {
        try {
            // 1. Fetch all records from the database
            // Sorting by ID DESC ensures the newest PRD numbers appear first
            List<Production> allProductions = productionRepo.findAll(
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));

            // 2. Check if the list is empty
            if (allProductions.isEmpty()) {
                return ResponseEntity.status(200)
                        .body(new BaseResponse(200, "The production list is currently empty.",
                                new java.util.ArrayList<>()));
            }

            // 3. Success Response
            return ResponseEntity.status(200)
                    .body(new BaseResponse(200,
                            "Successfully fetched " + allProductions.size() + " production records.", allProductions));

        }
        // Catch Database Connectivity Issues
        catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Database Error: Could not connect to the production table.", null));
        }
        // Catch-all for unexpected system failures
        catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Internal Server Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete-daily-progress/{id}")
    public ResponseEntity<Object> deleteDaily(@PathVariable Long id) {
        try {
            // ERROR 1: Check if the ID exists before trying to delete
            if (!dailyRepo.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404,
                                "Error Thrown: Cannot delete. Daily Progress ID " + id + " not found.", null));
            }

            // 1. Perform the deletion
            dailyRepo.deleteById(id);

            // 2. Success Response
            return ResponseEntity
                    .ok(new BaseResponse(200, "Success: Daily Progress ID " + id + " has been deleted.", null));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // ERROR 2: Foreign key constraints (if this ID is linked elsewhere)
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400,
                            "Error Thrown: Cannot delete this record because it is being used by another part of the system.",
                            null));

        } catch (Exception e) {
            // ERROR 3: General System Failure (Datagitbase down, etc.)
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Error Thrown: System failure during deletion. " + e.getMessage(),
                            null));
        }
    }

    @GetMapping("/get-daily-history/{prodNumber}")
    public ResponseEntity<Object> getDailyHistory(@PathVariable String prodNumber) {
        try {
            // 1. Fetch all records from the daily table using the method we added to the
            // Repo
            List<ProductionDailyProgress> history = dailyRepo.findByProductionNumber(prodNumber);

            if (history.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "Error: No daily updates found for " + prodNumber, null));
            }

            // 2. Return the full list (ID 1, 2, 3...)
            return ResponseEntity.ok(new BaseResponse(200, "History Fetched Successfully", history));

        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Error Thrown: System failure. " + e.getMessage(), null));
        }
    }

}