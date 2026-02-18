package com.deliveryChallan;
import org.springframework.web.bind.annotation.RestController;

import com.jobOrderCreation.BaseResponse;
import com.jobOrderCreation.JobOrder;
import com.jobOrderCreation.JobOrderRepository;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


@RestController
	@RequestMapping("/api/delivery-challan")
	@CrossOrigin("*")
	public class DeliveryChallanController {

	    @Autowired private DeliveryChallanRepository dcRepo;
	    @Autowired private JobOrderRepository jobRepo;

	    // 1. GET DATA BY JOB NUMBER (To auto-fill Customer, Material, Quantity)
	    @GetMapping("/fetch-job-details/{jobNumber}")
	    public ResponseEntity<?> getJobDetails(@PathVariable String jobNumber) {
	        JobOrder job = jobRepo.findByJobOrderNumber(jobNumber);
	        if (job == null) {
	            return ResponseEntity.status(404).body("Job Order not found");
	        }
	        
	        Map<String, String> response = new HashMap<>();
	        response.put("customerName", job.getCustomerName());
	        response.put("material", job.getMaterial());
	        response.put("quantity", job.getQuantityKg()); 
	        
	        return ResponseEntity.ok(response);
	    }

	    // 2. SAVE DELIVERY CHALLAN
	    @PostMapping("/save")
	    @Transactional
	    public ResponseEntity<?> saveChallan(@RequestBody DeliveryChallan req) {
	        try { 
	            String generatedDC = "DC-" + System.currentTimeMillis() % 10000 + "-2026";
	            req.setChallanNumber(generatedDC);
	            
	            if (req.getCreatedDate() == null) req.setCreatedDate("18/02/2026");

	            DeliveryChallan saved = dcRepo.save(req);

	            jobRepo.updateStatusByJobNumber(req.getJobOrderNumber(), "DISPATCHED");

	            return ResponseEntity.ok(new BaseResponse<>(200, "Success", saved));
	        } catch (Exception e) {
	            return ResponseEntity.status(400).body("Error saving Challan: " + e.getMessage());
	        }
	    }
	    @PutMapping("/update/{id}")
	    @Transactional
	    public ResponseEntity<?> updateChallan(@PathVariable Long id,
	                                           @RequestBody DeliveryChallan req) {

	        DeliveryChallan existing = dcRepo.findById(id)
	                .orElseThrow(() -> new RuntimeException("Challan not found"));

	        existing.setVehicleNumber(req.getVehicleNumber());
	        existing.setHsnCode(req.getHsnCode());
	        existing.setTransport(req.getTransport());
	        existing.setBillToAddress(req.getBillToAddress());
	        existing.setShipToAddress(req.getShipToAddress());
	        existing.setMaterial(req.getMaterial());
	        existing.setQuantity(req.getQuantityKg());

	        DeliveryChallan saved = dcRepo.save(existing);

	        return ResponseEntity.ok(saved);
	    }

	}
