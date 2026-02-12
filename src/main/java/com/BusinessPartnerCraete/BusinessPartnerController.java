package com.BusinessPartnerCraete;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.logincontroller.BaseResponse;

import java.util.List;
@CrossOrigin(value = "*")
@RestController
@RequestMapping("/api/business-partner")
public class BusinessPartnerController {

    @Autowired
    private BusinessPartnerRepository repository;

    // 1. CREATE PARTNER (POST)
    @PostMapping("/create")
    public ResponseEntity<?> createPartner(@RequestBody BusinessPartner partner) {
        BusinessPartner savedPartner = repository.save(partner);
        // Data-va List kulla wrap panni anuppuna thaan JSON-la [] varum
        return ResponseEntity.ok(new BaseResponse(200, "Partner Created Successfully", List.of(savedPartner)));
    }

    // 2. GET ALL PARTNERS (GET)
    @GetMapping("/all")
    public ResponseEntity<?> getAllPartners() {
        List<BusinessPartner> partners = repository.findAll();
        return ResponseEntity.ok(new BaseResponse(200, "Partners Fetched Successfully", partners));
    }

    // 3. UPDATE PARTNER (PUT)
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePartner(@PathVariable Long id, @RequestBody BusinessPartner details) {
        BusinessPartner partner = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found with id: " + id));

        // Fields update
        partner.setCustomerName(details.getCustomerName());
        partner.setGstin(details.getGstin());
        partner.setPaymentTerms(details.getPaymentTerms());
        partner.setContactPerson(details.getContactPerson());
        partner.setContactNumbers(details.getContactNumbers()); // Array update
        partner.setEmailId(details.getEmailId());
        partner.setBuildingNo(details.getBuildingNo());
        partner.setStreet(details.getStreet());
        partner.setCity(details.getCity());
        partner.setState(details.getState());
        partner.setPincode(details.getPincode());
        partner.setRegion(details.getRegion());

        BusinessPartner updatedPartner = repository.save(partner);
        return ResponseEntity.ok(new BaseResponse(200, "Partner Updated Successfully", List.of(updatedPartner)));
    }
}