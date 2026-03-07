// package com.scrap;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.productionCreation.MaterialBalance;
// import com.productionCreation.MaterialBalanceRepository;

// import org.springframework.dao.DataAccessException;
// import java.time.LocalDate;
// import java.util.*;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.LinkedHashMap;
// @RestController
// @RequestMapping("/api/scrap")
// @CrossOrigin(origins = "*")
// public class ScrapController {

    
//     @Autowired
//     private MaterialBalanceRepository scrapRepository;

//     // 1. FETCH BY JOB NUMBER (Production Number)
//    @GetMapping("/fetch-by-type/{scrapType}")
// public ResponseEntity<Map<String, Object>> getScrapByType(@PathVariable String scrapType) {
//     Map<String, Object> response = new HashMap<>();
//     try {
//         // 1. Normalize the type (converts 'return' to 'RETURN')
//         String type = scrapType.trim().toUpperCase();

//         // 2. Fetch the LIST from your repository
//         List<MaterialBalance> scrapList = scrapRepository.findByScrapType(type);

//         if (scrapList == null || scrapList.isEmpty()) {
//             response.put("status", 404);
//             response.put("message", "No records found for scrap type: " + type);
//             return ResponseEntity.status(404).body(response);
//         }

//         // 3. Process the list into the format your UI expects
//         List<Map<String, Object>> dataList = new ArrayList<>();
        
//         for (MaterialBalance scrap : scrapList) {
//             Map<String, Object> data = new LinkedHashMap<>();
//             data.put("Job Number", scrap.getJobNumber());
//             data.put("Material", scrap.getMaterial());
//             data.put("Thickness", scrap.getThickness());
//             data.put("Process", scrap.getProcess());
//             data.put("Scrap Quantity", scrap.getScrapQuantity());
//             data.put("Scrap Type", scrap.getScrapType());
//             data.put("Production Date", scrap.getProductionDate());
//             data.put("Returned Date", scrap.getReturnedDate());
//             dataList.add(data);
//         }

//         response.put("status", 200);
//         response.put("data", dataList);
//         return ResponseEntity.ok(response);

//     } catch (Exception e) {
//         response.put("status", 500);
//         response.put("message", "Error fetching scrap data: " + e.getMessage());
//         return ResponseEntity.status(500).body(response);
//     }
// }
// // }
// //     @GetMapping("/fetch/{jobNumber}")
// //     public ResponseEntity<Map<String, Object>> getScrapDetails(@PathVariable String jobNumber) {
// //         Map<String, Object> response = new HashMap<>();
// //         try {
// //             Scrap scrap = scrapRepository.findByJobNumber(jobNumber);

// //             if (scrap == null) {
// //                 response.put("status", 404);
// //                 response.put("message", "Job Number " + jobNumber + " not found.");
// //                 return ResponseEntity.status(404).body(response);
// //             }

// //             // Returning the specific fields requested from the UI
// //             Map<String, Object> data = new LinkedHashMap<>();
// //             data.put("Job Number", scrap.getJobNumber());
// //             data.put("Material", scrap.getMaterial());
// //             data.put("Thickness", scrap.getThickness());
// //             data.put("Process", scrap.getProcess());
// //             data.put("Scrap Quantity", scrap.getScrapQuantity());
// //             data.put("Scrap Type", scrap.getScrapType());
// //             data.put("Production Date", scrap.getProductionDate());
// //             data.put("Returned Date", scrap.getReturnedDate());

// //             response.put("status", 200);
// //             response.put("data", data);
// //             return ResponseEntity.ok(response);

// //         } catch (Exception e) {
// //             response.put("status", 500);
// //             response.put("message", "Error fetching data: " + e.getMessage());
// //             return ResponseEntity.status(500).body(response);
// //         }
// //     }

// //     // 2. MOVE TO SOLD (The "Move to Sold" button logic)
// //     @PutMapping("/sell/{jobNumber}")
// //     public ResponseEntity<Map<String, Object>> moveToSold(@PathVariable String jobNumber) {
// //         Map<String, Object> response = new HashMap<>();
// //         try {
// //             Scrap scrap = scrapRepository.findByJobNumber(jobNumber);

// //             if (scrap == null) {
// //                 response.put("status", 404);
// //                 response.put("message", "Cannot find scrap record to sell.");
// //                 return ResponseEntity.status(404).body(response);
// //             }

// //             // Update status and set the Sold Date to today
// //             scrap.setStatus("Sold");
// //             scrap.setSoldDate(LocalDate.now());
// //             scrapRepository.save(scrap);

// //             response.put("status", 200);
// //             response.put("message", "Successfully moved to Sold status");
// //             return ResponseEntity.ok(response);

// //         } catch (DataAccessException e) {
// //             response.put("status", 500);
// //             response.put("message", "Database Error: Could not update status.");
// //             return ResponseEntity.status(500).body(response);
// //         }
// //     }

// //     // 3. GET ALL BY STATUS (For the "Available" and "Sold" tabs)
// //     @GetMapping("/list")
// //     public ResponseEntity<List<Scrap>> getScrapsByStatus(@RequestParam String status) {
// //         return ResponseEntity.ok(scrapRepository.findByStatusIgnoreCase(status));
// //     }
// // }
// }
