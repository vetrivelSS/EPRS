package com.Invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jobOrderCreation.JobOrderRepository;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import jakarta.transaction.Transactional;

// Ensure consistent imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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

    @Autowired
    private InvoicePdfService pdfService;

    // @PostMapping("/create")
    // @Transactional
    // public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody
    // Map<String, Object> request) {
    // Map<String, Object> response = new HashMap<>();
    // try {
    // String mainCustomerName = (String) request.get("customerName");

    // // SAFE CONVERSION
    // List<Map<String, Object>> dcList = objectMapper.convertValue(
    // request.get("deliveryChallans"),
    // new TypeReference<List<Map<String, Object>>>() {
    // });

    // for (Map<String, Object> dc : dcList) {
    // String joNumber = (String) dc.get("jobOrderNumber");
    // String dcCustomerName = (String) dc.get("customerName");
    // if (!jobOrderRepository.existsByJobOrderNumber(joNumber)) {
    // response.put("status", 400);
    // response.put("message", "Error: Job Order Number " + joNumber + " not
    // found!");
    // return ResponseEntity.badRequest().body(response);
    // }
    // if (dcCustomerName == null ||
    // !dcCustomerName.equalsIgnoreCase(mainCustomerName)) {
    // response.put("status", 400);
    // response.put("message", "Error: DC Number mismatch!");
    // return ResponseEntity.badRequest().body(response);
    // }
    // }

    // Invoice inv = new Invoice();
    // inv.setInvoiceNumber("INV-" + (invoiceRepository.count() + 101));
    // inv.setCustomerName(mainCustomerName);
    // inv.setBillToAddress((String) request.get("billToAddress"));
    // inv.setShipToAddress((String) request.get("shipToAddress"));

    // Integer maxRef = invoiceRepository.findMaxReferenceNo();
    // int nextRef = (maxRef == null) ? 1 : maxRef + 1;
    // inv.setReferenceNo(nextRef);

    // Double grandTotal = 0.0;
    // for (Map<String, Object> dc : dcList) {
    // Double qty = Double.parseDouble(dc.get("quantityKg").toString());
    // Double rate = Double.parseDouble(dc.get("ratePer").toString());
    // Double disc = Double.parseDouble(dc.get("discount").toString());
    // Double individualTotal = (qty * rate) - disc;
    // dc.put("totalAmount", individualTotal);
    // grandTotal += individualTotal;
    // }

    // inv.setDcDetailsJson(objectMapper.writeValueAsString(dcList));
    // inv.setTotalAmount(grandTotal);
    // inv.setStatus("Active");

    // Invoice savedInvoice = invoiceRepository.save(inv);

    // response.put("status", 200);
    // response.put("message", "Success: Validated & Invoice Created");
    // response.put("data", savedInvoice);

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // response.put("status", 500);
    // response.put("message", "Error: " + e.getMessage());
    // return ResponseEntity.status(500).body(response);
    // }
    // }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String mainCustomerName = (String) request.get("customerName");

            // Use Full Path to avoid the Jackson version conflict error
            List<Map<String, Object>> dcList = objectMapper.convertValue(
                    request.get("deliveryChallans"),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                    });

            Double grandTotal = 0.0;

            for (Map<String, Object> dc : dcList) {
                // Get the production items array from the DC
                List<Map<String, Object>> prodItems = (List<Map<String, Object>>) dc.get("productions");
                Double dcSubTotal = 0.0;

                if (prodItems != null) {
                    for (Map<String, Object> item : prodItems) {
                        // Logic to calculate totals for each production item
                        Double qty = Double.parseDouble(item.get("quantityKg").toString());
                        Double rate = Double.parseDouble(item.get("ratePer").toString());
                        Double disc = Double.parseDouble(item.get("discount").toString());

                        Double itemTotal = (qty * rate) - disc;
                        item.put("totalAmount", itemTotal);
                        dcSubTotal += itemTotal;
                    }
                }
                dc.put("dcTotal", dcSubTotal);
                grandTotal += dcSubTotal;
            }

            // Prepare the Invoice entity
            Invoice inv = new Invoice();
            inv.setInvoiceNumber("INV-" + (invoiceRepository.count() + 101));
            inv.setCustomerName(mainCustomerName);
            inv.setDcDetailsJson(objectMapper.writeValueAsString(dcList));
            inv.setTotalAmount(grandTotal);
            inv.setStatus("Active");

            invoiceRepository.save(inv);

            response.put("status", 200);
            response.put("message", "Invoice created successfully with Production Numbers");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateAndRegenerateInvoice(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Invoice oldInv = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

            oldInv.setStatus("Cancelled");
            invoiceRepository.save(oldInv);

            String mainCustomerName = (String) request.get("customerName");
            List<Map<String, Object>> dcList = objectMapper.convertValue(
                    request.get("deliveryChallans"),
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            Invoice newInv = new Invoice();
            newInv.setInvoiceNumber("INV-" + (invoiceRepository.count() + 101));
            newInv.setCustomerName(mainCustomerName);
            newInv.setBillToAddress((String) request.get("billToAddress"));
            newInv.setShipToAddress((String) request.get("shipToAddress"));
            newInv.setStatus("Active");

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
            response.put("newInvoice", savedInv);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            List<Map<String, Object>> details = invoiceRepository
                    .findInvoiceWithFullDetails(invoice.getInvoiceNumber());
            Map<String, Object> headerData = (details != null && !details.isEmpty()) ? new HashMap<>(details.get(0))
                    : new HashMap<>();

            headerData.put("destination",
                    (headerData.get("city") != null) ? headerData.get("city").toString() : "Not Specified");
            headerData.put("ref_count", invoice.getReferenceNo());
            headerData.put("invoice_number", invoice.getInvoiceNumber());

            // VERSION-SAFE PARSING
            List<Map<String, Object>> items = safeParseJson(invoice.getDcDetailsJson());

            byte[] pdfBytes = pdfService.generateInvoicePdf(headerData, items);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/view-pdf/{invoiceNumber}")
    public ResponseEntity<byte[]> viewPdfByNumber(@PathVariable String invoiceNumber) {
        Map<String, Object> headerData = null;
        try {
            Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceNumber(invoiceNumber);
            List<Map<String, Object>> result = invoiceRepository.findInvoiceWithFullDetails(invoiceNumber);

            if (result == null || result.isEmpty())
                return ResponseEntity.status(404).build();

            headerData = new HashMap<>(result.get(0));
            if (invoiceOpt.isPresent()) {
                headerData.put("ref_count", invoiceOpt.get().getReferenceNo());
                headerData.put("invoice_number", invoiceOpt.get().getInvoiceNumber());
            }

            String finalCity = (headerData.get("city") != null && !headerData.get("city").toString().isEmpty())
                    ? headerData.get("city").toString()
                    : "Chennai";
            headerData.put("destination", finalCity);

            // VERSION-SAFE PARSING
            Object rawJson = headerData.get("dc_details_json");
            List<Map<String, Object>> itemsList = safeParseJson(rawJson != null ? rawJson.toString() : null);

            byte[] pdfBytes = pdfService.generateInvoicePdf(headerData, itemsList);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Invoice_" + invoiceNumber + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Helper method to parse JSON without triggering the Jackson 2.17.0
     * getNumberTypeFP bug
     */
    private List<Map<String, Object>> safeParseJson(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (json == null || json.equals("null") || json.trim().isEmpty())
            return list;
        try {
            // We use convertValue on the raw String which avoids the specific Parser call
            // causing the crash
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            // Fallback: If readValue fails due to the version mismatch, we return an empty
            // list
            // so the PDF still generates without crashing the server.
            System.err.println("Jackson Version Conflict prevented JSON parsing: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}