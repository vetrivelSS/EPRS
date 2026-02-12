package com.WorkOrderCreate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.logincontroller.BaseResponse;

import io.jsonwebtoken.io.IOException;
import tools.jackson.databind.ObjectMapper;
@CrossOrigin(value = "*")
@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderRepository repository;

    // File store panna folder path

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createWorkOrder(
            @RequestParam("data") String jsonData,
            @RequestParam(value = "cadFile", required = false) MultipartFile cadFile,
            @RequestParam(value = "bomFile", required = false) MultipartFile bomFile) throws java.io.IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkOrder workOrder = objectMapper.readValue(jsonData, WorkOrder.class);

            // Path-ah check panni create panna correct-ana logic
            File storageDir = new File("/data/storage");
            if (!storageDir.exists()) {
                boolean created = storageDir.mkdirs(); // Root folder create panna try pannum
                if(!created) {
                    // Root-la mudila na project folder-kulle 'storage' nu oru folder create pannum
                    storageDir = new File("storage");
                    storageDir.mkdirs();
                }
            }
            
            String finalPath = storageDir.getAbsolutePath() + File.separator;

            // 1. Save CAD Drawing
            if (cadFile != null && !cadFile.isEmpty()) {
                String cadFileName = System.currentTimeMillis() + "_" + cadFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(finalPath + cadFileName);
                Files.write(path, cadFile.getBytes());
                workOrder.setCadDrawingPath(path.toString());
            }

            // 2. Save BOM Excel
            if (bomFile != null && !bomFile.isEmpty()) {
                String bomFileName = System.currentTimeMillis() + "_" + bomFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(finalPath + bomFileName);
                Files.write(path, bomFile.getBytes());
                workOrder.setBomExcelPath(path.toString());
            }

            WorkOrder saved = repository.save(workOrder);
            return ResponseEntity.ok(new BaseResponse(200, "Work Order Created: " + saved.getWorkOrderNumber(), List.of(saved)));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Error: " + e.getMessage(), null));
        }
    }
    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateWorkOrder(
            @PathVariable Long id,
            @RequestParam("data") String jsonData,
            @RequestParam(value = "cadFile", required = false) MultipartFile cadFile,
            @RequestParam(value = "bomFile", required = false) MultipartFile bomFile) throws java.io.IOException {

        try {
            // 1. Existing Work Order check
            WorkOrder existingOrder = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Work Order not found with id: " + id));

            // 2. Map JSON to temporary object
            ObjectMapper objectMapper = new ObjectMapper();
            WorkOrder updatedDetails = objectMapper.readValue(jsonData, WorkOrder.class);

            // 3. Storage path logic (Safety fix)
            File storageDir = new File("storage"); // Project folder-kulle store aagum
            if (!storageDir.exists()) storageDir.mkdirs();
            String finalPath = storageDir.getAbsolutePath() + File.separator;

            // 4. Update fields
            existingOrder.setCustomer(updatedDetails.getCustomer());
            existingOrder.setPurchaseOrderNumber(updatedDetails.getPurchaseOrderNumber());
            existingOrder.setMaterial(updatedDetails.getMaterial());
            existingOrder.setThickness(updatedDetails.getThickness());
            existingOrder.setQuantityNo(updatedDetails.getQuantityNo());
            existingOrder.setLocation(updatedDetails.getLocation());
            // ... mattha ella fields-aiyum inga set pannunga

            // 5. Update CAD File (If new file provided)
            if (cadFile != null && !cadFile.isEmpty()) {
                // Palaiya file-ah delete panna logic (optional)
                String cadFileName = System.currentTimeMillis() + "_" + cadFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(finalPath + cadFileName);
                Files.write(path, cadFile.getBytes());
                existingOrder.setCadDrawingPath(path.toString());
            }

            // 6. Update BOM File (If new file provided)
            if (bomFile != null && !bomFile.isEmpty()) {
                String bomFileName = System.currentTimeMillis() + "_" + bomFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(finalPath + bomFileName);
                Files.write(path, bomFile.getBytes());
                existingOrder.setBomExcelPath(path.toString());
            }

            // 7. Save updated order
            WorkOrder saved = repository.save(existingOrder);

            return ResponseEntity.ok(new BaseResponse(200, "Work Order Updated Successfully", List.of(saved)));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Update failed: " + e.getMessage(), null));
        }
    }
    @GetMapping("/details/{won}")
    public ResponseEntity<?> getByWON(@PathVariable String won) {
        WorkOrder order = repository.findByWorkOrderNumber(won)
                .orElseThrow(() -> new RuntimeException("Work Order not found!"));
                
        return ResponseEntity.ok(new BaseResponse(200, "Found", List.of(order)));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllWorkOrders() {
        List<WorkOrder> orders = repository.findAll();
        return ResponseEntity.ok(new BaseResponse(200, "Success", orders));
    }
}
