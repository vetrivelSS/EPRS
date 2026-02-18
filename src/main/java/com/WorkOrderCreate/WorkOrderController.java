package com.WorkOrderCreate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

            // 1. Ungaloda Root Path
            String rootPath = "/data/ERP_Documents/"; 
            
            // 2. Partner Name (Customer Name) vechu folder name create panrom
            // Space iruntha athai underscore (_) ah mathidum
            String partnerFolderName = workOrder.getCustomer().trim().replaceAll("\\s+", "_");
            
            // 3. Full Path build panrom: /data/ERP_Documents/Partner_Name/
            File partnerDir = new File(rootPath + partnerFolderName);
            
            // 4. Partner name-la folder illai na, puthusa create pannum
            if (!partnerDir.exists()) {
                boolean isCreated = partnerDir.mkdirs(); // mkdirs() intermediate folders-aiyum create pannum
                if (!isCreated) {
                    return ResponseEntity.status(500).body(new BaseResponse(500, "Permission Denied: Folder create panna mudiyalai at " + rootPath, null));
                }
            }
            
            String uploadFolderPath = partnerDir.getAbsolutePath() + File.separator;

            // 5. CAD Drawing File Save
            if (cadFile != null && !cadFile.isEmpty()) {
                String cadFileName = "CAD_" + System.currentTimeMillis() + "_" + cadFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(uploadFolderPath + cadFileName);
                Files.write(path, cadFile.getBytes());
                workOrder.setCadDrawingPath(path.toString()); // DB-la full path save aagum
            }

            // 6. BOM Excel File Save
            if (bomFile != null && !bomFile.isEmpty()) {
                String bomFileName = "BOM_" + System.currentTimeMillis() + "_" + bomFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path path = Paths.get(uploadFolderPath + bomFileName);
                Files.write(path, bomFile.getBytes());
                workOrder.setBomExcelPath(path.toString());
            }

            // 7. Database-la save pannuvom (WON automatic-ah generate aagum)
            WorkOrder saved = repository.save(workOrder);
            
            return ResponseEntity.ok(new BaseResponse(200, "Work Order Created: " + saved.getWorkOrderNumber(), List.of(saved)));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new BaseResponse(500, "Storage Error: " + e.getMessage(), null));
        }
    }
    @GetMapping("/download/{id}/{type}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @PathVariable String type) throws java.io.IOException {
        try {
            // 1. Fetch Work Order from DB
            WorkOrder workOrder = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Work Order not found"));

            // 2. Decide which path to use based on type (cad or bom)
            String filePath = type.equalsIgnoreCase("cad") ? 
                              workOrder.getCadDrawingPath() : 
                              workOrder.getBomExcelPath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 3. Load file as Resource
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            // 4. Content Type detect pannuvom (PDF, Image, Excel, etc.)
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
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
    @GetMapping("/search/{won}")
    public ResponseEntity<?> getWorkOrderByWON(@PathVariable String won) {
        // Repository-la WON vechu find panrom
        return repository.findByWorkOrderNumber(won)
                .map(order -> ResponseEntity.ok(new BaseResponse(200, "Found", List.of(order))))
                .orElse(ResponseEntity.status(404).body(new BaseResponse(404, "Work Order Not Found", null)));
    }
}
