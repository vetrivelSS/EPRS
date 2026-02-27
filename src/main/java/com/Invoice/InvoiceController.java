package com.Invoice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jobOrderCreation.JobOrderRepository;

import jakarta.transaction.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private JobOrderRepository jobOrderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. Multiple Invoice / DC Insert API
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String mainCustomerName = (String) request.get("customerName"); // Header-il ulla Customer
            List<Map<String, Object>> dcList = (List<Map<String, Object>>) request.get("deliveryChallans");

            // --- VALIDATION LOGIC START ---
            for (Map<String, Object> dc : dcList) {
                String joNumber = (String) dc.get("jobOrderNumber");
                String dcCustomerName = (String) dc.get("customerName"); // Individual DC-il ulla Customer

                // 1. Job Order Number Validation
                if (!jobOrderRepository.existsByJobOrderNumber(joNumber)) {
                    response.put("status", 400);
                    response.put("message", "Error: Job Order Number " + joNumber + " not found!");
                    return ResponseEntity.badRequest().body(response);
                }

                // 2. Customer Name Matching Validation
                // DC-il ulla customer name, Main invoice customer-udan match aaganum
                if (dcCustomerName == null || !dcCustomerName.equalsIgnoreCase(mainCustomerName)) {
                    response.put("status", 400);
                    response.put("message", "Error: DC Number " + dc.get("dcNumber") + " belongs to a different customer (" + dcCustomerName + "). Customer name mismatch!");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            // --- VALIDATION LOGIC END ---

            Invoice inv = new Invoice();
            inv.setInvoiceNumber("INV-" + (invoiceRepository.count() + 101));
            inv.setCustomerName(mainCustomerName);
            inv.setBillToAddress((String) request.get("billToAddress"));
            inv.setShipToAddress((String) request.get("shipToAddress"));

            Double grandTotal = 0.0;
            for (Map<String, Object> dc : dcList) {
                Double qty = Double.parseDouble(dc.get("quantityKg").toString());
                Double rate = Double.parseDouble(dc.get("ratePer").toString());
                Double disc = Double.parseDouble(dc.get("discount").toString());

                Double individualTotal = (qty * rate) - disc;
                dc.put("totalAmount", individualTotal); //
                grandTotal += individualTotal;
            }

            inv.setDcDetailsJson(objectMapper.writeValueAsString(dcList));
            inv.setTotalAmount(grandTotal);
            inv.setStatus("Active"); //

            Invoice savedInvoice = invoiceRepository.save(inv);

            response.put("status", 200);
            response.put("message", "Success: Validated & Invoice Created");
            response.put("data", savedInvoice);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateAndRegenerateInvoice(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Pazhaya Invoice-ai fetch panni 'Cancelled' panroam
            Invoice oldInv = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
            
            oldInv.setStatus("Cancelled"); 
            invoiceRepository.save(oldInv);

            // 2. Pudhiya Invoice details-ai edukirom
            String mainCustomerName = (String) request.get("customerName");
            List<Map<String, Object>> dcList = (List<Map<String, Object>>) request.get("deliveryChallans");

            // --- CUSTOMER VALIDATION ---
            for (Map<String, Object> dc : dcList) {
                String dcCustomer = (String) dc.get("customerName");
                if (dcCustomer == null || !dcCustomer.equalsIgnoreCase(mainCustomerName)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Customer Mismatch in DC List!"));
                }
            }

            // 3. Pudhiya Invoice record create panroam
            Invoice newInv = new Invoice();
            long nextInvCount = invoiceRepository.count() + 101;
            newInv.setInvoiceNumber("INV-" + nextInvCount); // Pudhu Invoice No
            
            newInv.setCustomerName(mainCustomerName);
            newInv.setBillToAddress((String) request.get("billToAddress"));
            newInv.setShipToAddress((String) request.get("shipToAddress"));
            newInv.setStatus("Active");

            // Totals Calculation
            Double grandTotal = 0.0;
            for (Map<String, Object> dc : dcList) {
                Double qty = Double.parseDouble(dc.get("quantityKg").toString());
                Double rate = Double.parseDouble(dc.get("ratePer").toString());
                Double disc = Double.parseDouble(dc.get("discount").toString());
                
                Double dcTotal = (qty * rate) - disc;
                dc.put("totalAmount", dcTotal);
                grandTotal += dcTotal;
            }

            newInv.setDcDetailsJson(objectMapper.writeValueAsString(dcList));
            newInv.setTotalAmount(grandTotal);

            Invoice savedInv = invoiceRepository.save(newInv);

            response.put("status", 200);
            response.put("message", "Old Invoice Cancelled & New Invoice Generated");
            response.put("newInvoice", savedInv);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Update Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    // 2. GET ALL Invoices API (Including Multiple DC details)
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllInvoices() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            List<Map<String, Object>> formattedList = new ArrayList<>();

            for (Invoice inv : invoices) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", inv.getId());
                data.put("invoiceNumber", inv.getInvoiceNumber());
                data.put("customerName", inv.getCustomerName());
                data.put("totalAmount", inv.getTotalAmount());
                data.put("status", inv.getStatus());

                // JSON Details-ai thirumba list-aga mathukirom
                if (inv.getDcDetailsJson() != null) {
                    List<Map<String, Object>> dcs = objectMapper.readValue(
                        inv.getDcDetailsJson(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    data.put("selectedDCs", dcs);
                }
                formattedList.add(data);
            }
            response.put("status", 200);
            response.put("data", formattedList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 500);
            return ResponseEntity.status(500).body(response);
        }
    }
}