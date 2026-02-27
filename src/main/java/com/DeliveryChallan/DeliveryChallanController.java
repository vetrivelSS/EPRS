package com.DeliveryChallan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/dc")
@CrossOrigin(origins = "*")
public class DeliveryChallanController {

    @Autowired
    private DeliveryChallanRepository dcRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // POST API: Store as JSON String
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDC(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> productionList = (List<Map<String, Object>>) request.get("productions");

            DeliveryChallan newDC = new DeliveryChallan();
            newDC.setChallanNumber("DC-" + java.time.LocalDate.now().getYear() + "-" + (dcRepository.count() + 1));
            newDC.setBillToAddress((String) request.get("billToAddress"));
            newDC.setShipToAddress((String) request.get("shipToAddress"));

            // Grand totals

            Double totalAmt = 0.0;
            Double totalQty = 0.0;
            for (Map<String, Object> item : productionList) {
                totalAmt += Double.parseDouble(item.get("totalAmount").toString());
                String qtyOnly = item.get("quentity").toString().replaceAll("[^0-9.]", "");
                totalQty += Double.parseDouble(qtyOnly);
            }
            newDC.setTotalAmount(totalAmt);
            newDC.setQuantityKg(totalQty);

            // Store JSON Array as String
            String productionsJson = objectMapper.writeValueAsString(productionList);
            newDC.setProductionsJson(productionsJson);

            // Header details from first entry
            Map<String, Object> first = productionList.get(0);
            newDC.setHsnCode(first.get("hsnCode").toString());
            newDC.setMaterial(first.get("metrails").toString());
            newDC.setTransport(first.get("transport").toString());
            newDC.setVehicleNumber(first.get("vehicleNumber").toString());

            DeliveryChallan savedDC = dcRepository.save(newDC);

            response.put("status", 200);
            response.put("message", "Success");
            response.put("data", savedDC);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET ALL API
//    @GetMapping("/all")
//    public ResponseEntity<Map<String, Object>> getAllDC() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<DeliveryChallan> allDCs = dcRepository.findAll();
//            List<Map<String, Object>> formattedList = new ArrayList<>();
//
//            for (DeliveryChallan dc : allDCs) {
//                Map<String, Object> data = new HashMap<>();
//                data.put("id", dc.getId());
//                data.put("challanNumber", dc.getChallanNumber());
//                data.put("totalAmount", dc.getTotalAmount());
//                data.put("quantityKg", dc.getQuantityKg());
//                data.put("billToAddress", dc.getBillToAddress());
//                data.put("shipToAddress", dc.getShipToAddress());
//
//                // Parse String back to JSON Array
//                if (dc.getProductionsJson() != null) {
//                    List<Map<String, Object>> productions = objectMapper.readValue(
//                            dc.getProductionsJson(),
//                            new TypeReference<List<Map<String, Object>>>() {
//                            });
//                    data.put("productions", productions);
//                }
//                formattedList.add(data);
//            }
//            response.put("status", 200);
//            response.put("data", formattedList);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("status", 500);
//            response.put("message", "Error: " + e.getMessage());
//            return ResponseEntity.status(500).body(response);
//        }
//    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateAndRegenerateDC(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Pazhaya DC record-ai fetch panni "Cancelled" endru maatrukirom
            DeliveryChallan oldDC = dcRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("DC not found with id: " + id));
            
            oldDC.setStatus("Cancelled"); // Status-ai Cancelled-aga maatrukirom
            dcRepository.save(oldDC);

            // 2. Pudhiya DC create panna data extraction
            List<Map<String, Object>> productionList = (List<Map<String, Object>>) request.get("productions");
            String billTo = (String) request.get("billToAddress");
            String shipTo = (String) request.get("shipToAddress");
            String customerName = (String) request.get("customerName");

            DeliveryChallan newDC = new DeliveryChallan();
            
            // --- Pudhiya DC Number Generate Seiyum Logic ---
            // Idhu sequence-ai increment seithu pudhu number tharum
            long nextCount = dcRepository.count() + 1;
            newDC.setChallanNumber("DC-" + (1234 + nextCount) + "-" + java.time.LocalDate.now().getYear());
            
            newDC.setBillToAddress(billTo);
            newDC.setShipToAddress(shipTo);
            newDC.setCustomerName(customerName);
            newDC.setStatus("Active"); // Pudhu DC eppothum Active-aga irukkum

            // Totals calculation
            Double totalAmt = 0.0;
            Double totalQty = 0.0;
            StringBuilder prodNumbers = new StringBuilder();

            for (int i = 0; i < productionList.size(); i++) {
                Map<String, Object> item = productionList.get(i);
                totalAmt += Double.parseDouble(item.get("totalAmount").toString());
                String qtyOnly = item.get("quentity").toString().replaceAll("[^0-9.]", "");
                totalQty += Double.parseDouble(qtyOnly);
                
                prodNumbers.append(item.get("productionNumber"));
                if (i < productionList.size() - 1) prodNumbers.append(", ");
            }

            newDC.setTotalAmount(totalAmt);
            newDC.setQuantityKg(totalQty);
            newDC.setProductionNumber(prodNumbers.toString());

            // JSON array-ai string-aga store panroam
            String productionsJson = objectMapper.writeValueAsString(productionList);
            newDC.setProductionsJson(productionsJson);

            // Common Header details (First entry-ilirundhu)
            Map<String, Object> first = productionList.get(0);
            newDC.setHsnCode(first.get("hsnCode").toString());
            newDC.setMaterial(first.get("metrails").toString());
            newDC.setTransport(first.get("transport").toString());
            newDC.setVehicleNumber(first.get("vehicleNumber").toString());

            // 3. Pudhu DC-ai save panroam
            DeliveryChallan savedDC = dcRepository.save(newDC);

            response.put("status", 200);
            response.put("message", "Old DC Cancelled & New DC Generated Successfully");
            response.put("data", savedDC); 

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllDCC() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Database-la irundhu ella records-aiyum edukkirom
            List<DeliveryChallan> allDCs = dcRepository.findAll();
            List<Map<String, Object>> formattedList = new ArrayList<>();

            for (DeliveryChallan dc : allDCs) {
                Map<String, Object> data = new HashMap<>();
                
                // Basic Fields mapping
                data.put("id", dc.getId());
                data.put("challanNumber", dc.getChallanNumber());
                data.put("customerName", dc.getCustomerName());
                data.put("billToAddress", dc.getBillToAddress());
                data.put("shipToAddress", dc.getShipToAddress());
                data.put("totalAmount", dc.getTotalAmount());
                data.put("quantityKg", dc.getQuantityKg());
                
                // --- MUKKIYAM: Status field-ai inge add seigiroam ---
                // Idhu 'Active' illai 'Cancelled' endru kaattum
                data.put("status", dc.getStatus() != null ? dc.getStatus() : "Active"); 
                
                data.put("transport", dc.getTransport());
                data.put("vehicleNumber", dc.getVehicleNumber());
                data.put("productionNumber", dc.getProductionNumber());

                // 2. JSON String-ai thirumba Array-aga mathugirom
                if (dc.getProductionsJson() != null) {
                    try {
                        List<Map<String, Object>> productions = objectMapper.readValue(
                            dc.getProductionsJson(), 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        data.put("productions", productions);
                    } catch (Exception e) {
                        data.put("productions", new ArrayList<>());
                    }
                } else {
                    data.put("productions", new ArrayList<>());
                }

                formattedList.add(data);
            }

            response.put("status", 200);
            response.put("message", "Success");
            response.put("data", formattedList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}