// package com.productionCreation;

// import com.jobOrderCreation.BaseResponse;
// import com.jobOrderCreation.JobOrder;
// import com.jobOrderCreation.JobOrderRepository;
// import com.productionCreation.Production;
// import com.productionCreation.ProductionRepository;
// import java.util.List;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping(value = { "/api/production" })
// @CrossOrigin(value = { "*" })
// public class ProductionController {

//     @Autowired
//     private ProductionRepository productionRepo;

//     @Autowired
//     private JobOrderRepository jobOrderRepo;

//     @GetMapping(value = { "/pending-from-joborders" })
//     public ResponseEntity<Object> getPendingJobs() {
//         List<JobOrder> pending = this.jobOrderRepo.findByStatus("PENDING");
//         return ResponseEntity.ok(new BaseResponse(200, "Pending Jobs Fetched", pending));
//     }

//     @GetMapping(value = { "/all-joborders" })
//     public ResponseEntity<Object> getAllJobs() {
//         List<JobOrder> allJobs = this.jobOrderRepo.findAll();
//         return ResponseEntity.ok(new BaseResponse(200, "All Job Orders Fetched Successfully", allJobs));
//     }

//     @GetMapping(value = { "/all-productions" })
//     public ResponseEntity<Object> getAllProductions() {
//         try {
//             List<Production> productionList = this.productionRepo.findAll();
//             if (productionList.isEmpty()) {
//                 return ResponseEntity.ok(new BaseResponse(200, "No production records found", productionList));
//             }
//             return ResponseEntity.ok(new BaseResponse(200, "Production data fetched successfully", productionList));
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(new BaseResponse(500, "Error: " + e.getMessage(), null));
//         }
//     }

//     @PostMapping(value = { "/start-inprogress/{jobNumber}" })
//     public ResponseEntity<Object> startProgress(@PathVariable String jobNumber) {
//         JobOrder job = this.jobOrderRepo.findByJobOrderNumber(jobNumber);
//         if (job == null) {
//             return ResponseEntity.status(404).body(new BaseResponse(404, "Job Order Not Found", null));
//         }

//         // Update Job Order Status
//         job.setStatus("IN PROGRESS");
//         this.jobOrderRepo.save(job); // Removed (Object) cast

//         // Create New Production Entry
//         Production newProduction = new Production();
//         newProduction.setJobOrderNumber(job.getJobOrderNumber());
//         newProduction.setCustomerName(job.getCustomerName());
//         newProduction.setMaterial(job.getMaterial());
//         newProduction.setThickness(job.getThickness());
//         newProduction.setProcess(job.getProcess());
//         newProduction.setQuantityNo(job.getQuantityNo());
//         newProduction.setHsnCode(job.getHsnCode()); // If JobOrder has HSN

//         newProduction.setStatus("IN PROGRESS");
//         // **************

//         // ***************
//         // Save and get the ID to generate Production Number
//         newProduction = this.productionRepo.save(newProduction); // Removed (Object) cast

//         String generatedNo = "PRD-" + newProduction.getId();
//         newProduction.setProductionNumber(generatedNo);

//         // Update with generated ID
//         this.productionRepo.save(newProduction); // Removed (Object) cast

//         return ResponseEntity.ok(new BaseResponse(200, "Production Started: " + generatedNo, newProduction));
//     }

//     @PostMapping(value = { "/submit-production-completed" })
//     public ResponseEntity<Object> submitProduction(@RequestBody Production req) {
//         try {
//             Production existingRecord = this.productionRepo.findByJobOrderNumber(req.getJobOrderNumber());
//             if (existingRecord == null) {
//                 return ResponseEntity.status(404)
//                         .body(new BaseResponse(404, "Error: Production record not found!", null));
//             }
//             if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
//                 return ResponseEntity.status(400)
//                         .body(new BaseResponse(400, "Error: Status must be 'COMPLETED' to move to history.", null));
//             }
//             if (req.getFinishedQuantity() == null || req.getScrapQuantity() == null || req.getScrapType() == null) {
//                 return ResponseEntity.status(400)
//                         .body(new BaseResponse(400, "Error: Please fill all fields before completing.", null));
//             }

//             existingRecord.setFinishedQuantity(req.getFinishedQuantity());
//             existingRecord.setBalanceQuantity(req.getBalanceQuantity());
//             existingRecord.setScrapQuantity(req.getScrapQuantity());
//             existingRecord.setScrapType(req.getScrapType());
//             existingRecord.setCompletedDate(req.getCompletedDate());
//             existingRecord.setProductionDate(req.getProductionDate());
//             existingRecord.setQuantityNo(req.getQuantityNo());
//             existingRecord.setQuantityNo(req.getQuantityKg());

//             existingRecord.setRemarks(req.getRemarks());
//             existingRecord.setStatus("COMPLETED");

//             Production saved = this.productionRepo.save(existingRecord);

//             // (Object) cast

//             this.jobOrderRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "COMPLETED");

//             return ResponseEntity.ok(new BaseResponse(200, "Production marked as COMPLETED successfully", saved));
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(new BaseResponse(500, "System Error: " + e.getMessage(), null));
//         }
//     }

//     @GetMapping(value = { "/list-all-status/{status}" })
//     public ResponseEntity<Object> getListByStatus(@PathVariable String status) {
//         List<Production> list = this.productionRepo.findProductionByStatus(status.toUpperCase());
//         return ResponseEntity.ok(new BaseResponse(200, "Data fetched", list));
//     }
// }

// ***********OLD CODE*************

// **********New CODE***********
package com.productionCreation;

import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;
import com.jobOrderCreation.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.productionCreation.ProductionDailyProgressRepository; // Ensure this path is correct
import java.util.HashMap; // Usually needed alongside Map
import java.util.List;
import java.util.ArrayList;

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

    // START PRODUCTION: Prevents duplicate starts

    @PostMapping("/create-production")
    public ResponseEntity<Object> createProduction(@RequestBody Production req) {
        try {
            // 1. STRICT FIELD VALIDATION
            if (req.getJobOrderNumber() == null || req.getJobOrderNumber().trim().isEmpty() ||
                    req.getMaterial() == null || req.getMaterial().trim().isEmpty() ||
                    req.getThickness() == null ||
                    req.getProcess() == null || req.getProcess().trim().isEmpty() ||
                    req.getFinishedQuantity() == null) {

                return ResponseEntity.status(400)
                        .body(new BaseResponse(400,
                                "Validation Error: Missing mandatory fields. Please provide Job Order Number, Material, Thickness, Process, and Received QuantityKg.",
                                null));
            }

            // 2. CHECK IF JOB ORDER EXISTS
            JobOrder job = jobOrderRepo.findByJobOrderNumber(req.getJobOrderNumber());
            if (job == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404,
                                "Not Found: Job Order Number '" + req.getJobOrderNumber() + "' does not exist.", null));
            }

            // 3. CHECK FOR DUPLICATES
            Production existingProd = productionRepo.findByJobOrderNumber(req.getJobOrderNumber());
            if (existingProd != null) {
                return ResponseEntity.status(409)
                        .body(new BaseResponse(409,
                                "Conflict: A production record already exists for this Job Order Number.", null));
            }

            // 4. MAPPING & SAVING
            Production newProd = new Production();
            newProd.setJobOrderNumber(req.getJobOrderNumber());
            newProd.setMaterial(req.getMaterial());
            newProd.setThickness(req.getThickness());
            newProd.setProcess(req.getProcess());
            newProd.setFinishedQuantity(req.getFinishedQuantity());
            newProd.setRemarks(req.getRemarks());
            newProd.setStatus("PENDING"); // Default status

            newProd = productionRepo.save(newProd);

            // 5. PRODUCTION NUMBER GENERATION
            String generatedNo = "PRD-" + newProd.getId() + "-" + java.time.LocalDate.now().getYear();
            newProd.setProductionNumber(generatedNo);

            Production saved = productionRepo.save(newProd);
            return ResponseEntity.status(201).body(new BaseResponse(201, "Production Created Successfully", saved));

        }
        // CATCH BLOCK 1: Handles Database Constraints (e.g., column length, unique
        // constraints)
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400,
                            "Database Error: Data integrity violation. Check for duplicate entries or invalid data types.",
                            null));
        }
        // CATCH BLOCK 2: Handles Null Pointer Issues (if something unexpected is
        // missing)
        catch (NullPointerException e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Logic Error: A null value was encountered during processing.", null));
        }
        // CATCH BLOCK 3: Handles any other unexpected Java exceptions
        catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Internal Server Error: " + e.getMessage(), null));
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

    // @GetMapping("/fetch-by-number/{prodNumber}")
    // public ResponseEntity<Object> getByProdNumber(@PathVariable String
    // prodNumber) {
    // // Note: You must add 'findByProductionNumber' to your ProductionRepository
    // // first
    // Production prod = productionRepo.findByProductionNumber(prodNumber);

    // if (prod != null) {
    // // This will return the 4 fields + others you mapped:
    // // 1. Material, 2. Thickness, 3. Process, 4. FinishedQuantity (Received Qty)
    // return ResponseEntity.ok(new BaseResponse(200, "Data Fetched", prod));
    // } else {
    // return ResponseEntity.status(404)
    // .body(new BaseResponse(404, "Error: Production Number not found.", null));
    // }
    // }

    // @PostMapping("/add-daily-progress")
    // public ResponseEntity<Object> addDaily(@RequestBody ProductionDailyProgress
    // req) {
    // try {
    // // 1. STRICT FIELD VALIDATION
    // if (req.getProductionNumber() == null ||
    // req.getProductionNumber().trim().isEmpty() ||
    // req.getFinishedQuantity() == null ||
    // req.getProductionDate() == null || req.getProductionDate().trim().isEmpty())
    // {

    // return ResponseEntity.status(400)
    // .body(new BaseResponse(400,
    // "Validation Error: Production Number, Finished Quantity, and Production Date
    // are mandatory.",
    // null));
    // }

    // // 2. EXISTENCE CHECK: Verify the Production Number exists in the main table
    // Production mainProd =
    // productionRepo.findByProductionNumber(req.getProductionNumber());
    // if (mainProd == null) {
    // return ResponseEntity.status(404)
    // .body(new BaseResponse(404, "Not Found: The Production Number '" +
    // req.getProductionNumber()
    // + "' does not exist in the main records.", null));
    // }

    // // 3. SUCCESS PATH: Save to Daily Progress Table
    // ProductionDailyProgress saved = dailyRepo.save(req);
    // return ResponseEntity.status(201).body(new BaseResponse(201, "Daily Progress
    // Added Successfully", saved));

    // }
    // // 1. Specific Child First: Handles Data Integrity (e.g., column overflow,
    // // constraint violations)
    // catch (org.springframework.dao.DataIntegrityViolationException e) {
    // return ResponseEntity.status(400)
    // .body(new BaseResponse(400,
    // "Data Error: The values provided violate database constraints (e.g. data too
    // long or invalid format).",
    // null));
    // }
    // // 2. General Parent Second: Handles general database access issues (e.g., DB
    // is
    // // offline)
    // catch (org.springframework.dao.DataAccessException e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Database Error: Communication failure. " +
    // e.getMessage(), null));
    // }
    // // 3. Logic Error: Catch unexpected Null pointers
    // catch (NullPointerException e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Logic Error: A null value was encountered during
    // processing.", null));
    // }
    // // 4. Catch All: Any other unexpected Java exceptions
    // catch (Exception e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Unknown Server Error: " + e.getMessage(),
    // null));
    // }
    // }

    // @PostMapping("/add-daily-progress")
    // public ResponseEntity<Object> addDaily(@RequestBody ProductionDailyProgress
    // req) {
    // try {
    // // 1. VALIDATION
    // if (req.getProductionNumber() == null || req.getFinishedQuantity() == null) {
    // return ResponseEntity.status(400)
    // .body(new BaseResponse(400, "Validation Error: Production Number and Finished
    // Qty are required.", null));
    // }

    // // 2. FIND MAIN PRODUCTION RECORD
    // Production mainProd =
    // productionRepo.findByProductionNumber(req.getProductionNumber());
    // if (mainProd == null) {
    // return ResponseEntity.status(404)
    // .body(new BaseResponse(404, "Not Found: Main Production record not found.",
    // null));
    // }

    // // 3. SAVE DAILY PROGRESS
    // ProductionDailyProgress savedDaily = dailyRepo.save(req);

    // // 4. AUTO-STATUS CHANGE (The "Complete" Button Logic)
    // if (req.getRemarks() != null &&
    // req.getRemarks().equalsIgnoreCase("COMPLETE")) {
    // mainProd.setStatus("COMPLETED");
    // productionRepo.save(mainProd); // This updates the 'production' table
    // automatically
    // }

    // return ResponseEntity.status(201)
    // .body(new BaseResponse(201, "Daily Progress Saved and Status Updated",
    // savedDaily));

    // }
    // catch (org.springframework.dao.DataIntegrityViolationException e) {
    // return ResponseEntity.status(400)
    // .body(new BaseResponse(400, "Data Error: Constraint violation.", null));
    // }
    // catch (org.springframework.dao.DataAccessException e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Database Error: " + e.getMessage(), null));
    // }
    // catch (Exception e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Unknown Error: " + e.getMessage(), null));
    // }
    // }
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

    @PutMapping("/update-daily-progress/{prodNumber}")
    public ResponseEntity<Object> updateDaily(@PathVariable String prodNumber,
            @RequestBody ProductionDailyProgress progressReq) {
        try {
            // ERROR 1: Validate Path Variable (Is the URL empty?)
            if (prodNumber == null || prodNumber.trim().isEmpty()) {
                return ResponseEntity.status(400)
                        .body(new BaseResponse(400, "Error: Production Number is missing from URL.", null));
            }

            // ERROR 2: Verify Main Record (Does this PRD Number exist in the system?)
            Production mainProd = productionRepo.findByProductionNumber(prodNumber);
            if (mainProd == null) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "Error: Production Number '" + prodNumber + "' not found.", null));
            }

            // ERROR 3: Validate Request Body (Is the JSON empty or missing fields?)
            if (progressReq == null || progressReq.getFinishedQuantity() == null) {
                return ResponseEntity.status(400)
                        .body(new BaseResponse(400, "Error: Daily progress data (finishedQuantity) is required.",
                                null));
            }

            // 1. Map the URL PRD Number to the new daily entry
            progressReq.setProductionNumber(prodNumber);

            // 2. Set Status (This ensures the daily row shows "IN PROGRESS")
            progressReq.setStatus("IN PROGRESS");

            // 3. Update Main Table Status (Only if it was PENDING)
            if ("PENDING".equalsIgnoreCase(mainProd.getStatus())) {
                mainProd.setStatus("IN PROGRESS");
                productionRepo.save(mainProd);
            }

            // 4. Save to Daily Table (Creates ID 1, then ID 2, then ID 3...)
            dailyRepo.save(progressReq);

            // 5. Fetch History (Gets the full list for Postman response)
            List<ProductionDailyProgress> history = dailyRepo.findByProductionNumber(prodNumber);

            return ResponseEntity.ok(new BaseResponse(200, "Update Success for " + prodNumber, history));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // ERROR 4: Database Error (e.g., column too short, null constraint)
            return ResponseEntity.status(400)
                    .body(new BaseResponse(400, "Database Error: Data format mismatch or constraint violation.", null));

        } catch (Exception e) {
            // ERROR 5: General System Failure (The "Catch All")
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "System Error: " + e.getMessage(), null));
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
            // ERROR 3: General System Failure (Database down, etc.)
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Error Thrown: System failure during deletion. " + e.getMessage(),
                            null));
        }
    }
    //needed ******************

    // @PutMapping("/submit-final-production/{prodNumber}")
    // public ResponseEntity<Object> submitFinal(@PathVariable String prodNumber,
    // @RequestBody Production finalReq) {
    // try {
    // // 1. Fetch the main record to be closed
    // Production existing = productionRepo.findByProductionNumber(prodNumber);

    // if (existing == null) {
    // return ResponseEntity.status(404)
    // .body(new BaseResponse(404, "Error: Production '" + prodNumber + "' not
    // found.", null));
    // }

    // // 2. Map the 6 Fields you requested for the final page
    // existing.setFinishedQuantity(finalReq.getFinishedQuantity()); // 1. From UI
    // (Received/Finished mapping)
    // existing.setBalanceQuantity(finalReq.getBalanceQuantity()); // 2. Balance
    // existing.setScrapQuantity(finalReq.getScrapQuantity()); // 3. Scrap Qty
    // existing.setScrapType(finalReq.getScrapType()); // 4. Scrap Type
    // existing.setRemarks(finalReq.getRemarks()); // 5. Remarks
    // // Note: 'Received Quantity' usually comes from the Job Order linked earlier

    // // 3. Finalize Status
    // existing.setStatus("COMPLETED");
    // existing.setCompletedDate(java.time.LocalDate.now().toString());

    // // 4. Save the Final Record
    // Production saved = productionRepo.save(existing);

    // // 5. Sync with Job Order (Optional but recommended)
    // jobOrderRepo.updateStatusByJobNumber(existing.getJobOrderNumber(),
    // "COMPLETED");

    // return ResponseEntity.ok(new BaseResponse(200, "Production Fully Completed
    // Successfully", saved));

    // } catch (Exception e) {
    // return ResponseEntity.status(500)
    // .body(new BaseResponse(500, "Final Submit Error: " + e.getMessage(), null));
    // }
    // }

    // needed***********************

    @GetMapping("/get-daily-history/{prodNumber}")
    public ResponseEntity<Object> getDailyHistory(@PathVariable String prodNumber) {
        try {
            // 1. Fetch all records from the daily table using the method we added to the
            // Repo
            List<ProductionDailyProgress> history = dailyRepo.findByProductionNumber(prodNumber);

            // ERROR: If the list is empty, inform the user
            if (history.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new BaseResponse(404, "Error: No daily updates found for " + prodNumber, null));
            }

            // 2. Return the full list (ID 1, 2, 3...)
            return ResponseEntity.ok(new BaseResponse(200, "History Fetched Successfully", history));

        } catch (Exception e) {
            // ERROR: General System Failure
            return ResponseEntity.status(500)
                    .body(new BaseResponse(500, "Error Thrown: System failure. " + e.getMessage(), null));
        }
    }

}