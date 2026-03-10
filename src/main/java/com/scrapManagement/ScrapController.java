// package com.scrapManagement;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import com.productionCreation.ProductionResult;

// import org.springframework.http.ResponseEntity;

// @RestController
// @RequestMapping("/api/scrap")
// public class ScrapController {

// @Autowired
// private ScrapService scrapService;

// @Autowired
// private ScrapService ProductionResultRepository;

// @GetMapping("/scrap")
// public ResponseEntity<Object> getScrap(@RequestParam String type) {

// Map<String, Object> response = new HashMap<>();

// try {

// List<ProductionResult> list =
// ProductionResultRepository.findByScrapType(type);

// response.put("status", 200);
// response.put("message", type + " Scrap List");
// response.put("data", list);

// return ResponseEntity.ok(response);

// } catch (Exception e) {

// response.put("status", 500);
// response.put("message", "Server Error: " + e.getMessage());
// response.put("data", null);

// return ResponseEntity.status(500).body(response);
// }
// }
// }