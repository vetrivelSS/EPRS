package com.ledger;

import com.Invoice.Invoice;
import com.Invoice.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/ledger")
@CrossOrigin(origins = "*")
public class LedgerController {

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrUpdateLedger(@RequestBody Ledger ledgerRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // --- 1. VALIDATION CATCH ---
            if (ledgerRequest.getInvoiceNumber() == null || ledgerRequest.getInvoiceNumber().trim().isEmpty()) {
                response.put("status", 400);
                response.put("message", "Validation Failed: Invoice Number is a required field and cannot be null.");
                return ResponseEntity.badRequest().body(response);
            }

            // --- 2. INVOICE LOOKUP CATCH ---
            Invoice existingInvoice;
            try {
                Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceNumber(ledgerRequest.getInvoiceNumber());
                if (invoiceOpt.isEmpty()) {
                    response.put("status", 404);
                    response.put("message",
                            "Not Found: No invoice exists with Number " + ledgerRequest.getInvoiceNumber());
                    return ResponseEntity.status(404).body(response);
                }
                existingInvoice = invoiceOpt.get();
            } catch (DataAccessException e) {
                response.put("status", 500);
                response.put("message", "Database Connection Error: Failed to search Invoice table.");
                return ResponseEntity.status(500).body(response);
            }

            // --- 3. UPSERT & LEDGER NUMBER LOGIC CATCH ---
            try {
                Ledger existingLedger = ledgerRepository.findByInvoiceNumber(ledgerRequest.getInvoiceNumber());

                if (existingLedger != null) {
                    ledgerRequest.setId(existingLedger.getId());
                    ledgerRequest.setStatus("Settled Ledger");

                    if (existingLedger.getSettledDate() == null) {
                        ledgerRequest.setSettledDate(LocalDate.now());
                    } else {
                        ledgerRequest.setSettledDate(existingLedger.getSettledDate());
                    }

                    if (existingLedger.getLedgerNumber() == null) {
                        long currentCount = ledgerRepository.count();
                        ledgerRequest.setLedgerNumber(
                                "LGR-" + String.format("%02d", currentCount + 1) + "-" + LocalDate.now().getYear());
                    } else {
                        ledgerRequest.setLedgerNumber(existingLedger.getLedgerNumber());
                    }
                } else {
                    long nextSequence = ledgerRepository.count() + 1;
                    ledgerRequest.setLedgerNumber(
                            "LGR-" + String.format("%02d", nextSequence) + "-" + LocalDate.now().getYear());
                    ledgerRequest.setStatus("Settled Ledger");
                    ledgerRequest.setSettledDate(LocalDate.now());
                }
            } catch (Exception e) {
                response.put("status", 500);
                response.put("message", "Internal Error: Failed to generate Ledger sequence/status logic.");
                return ResponseEntity.status(500).body(response);
            }

            // --- 4. DATA MAPPING CATCH ---
            try {
                ledgerRequest.setPartnerName(existingInvoice.getCustomerName());
                ledgerRequest.setTotalAmount(existingInvoice.getTotalAmount());
            } catch (NullPointerException e) {
                response.put("status", 422);
                response.put("message", "Unprocessable Entity: The source invoice is missing Customer Name or Amount.");
                return ResponseEntity.status(422).body(response);
            }

            // --- 5. FINAL SAVE CATCH ---
            try {
                Ledger savedLedger = ledgerRepository.save(ledgerRequest);
                response.put("status", 200);
                response.put("message",
                        ledgerRequest.getId() != null ? "Ledger Updated" : "Ledger Created Successfully");
                response.put("data", savedLedger);
                return ResponseEntity.ok(response);
            } catch (DataIntegrityViolationException e) {
                response.put("status", 409);
                response.put("message", "Data Conflict: This Invoice is already assigned to a unique ledger record.");
                return ResponseEntity.status(409).body(response);
            }

        } catch (Exception e) {
            // CATCH-ALL for anything we missed
            response.put("status", 500);
            response.put("message", "Critical System Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllLedgerEntries() {
        Map<String, Object> response = new HashMap<>();

        try {
            // --- 1. DATABASE FETCH CATCH ---
            List<Ledger> allEntries;
            try {
                // Sorting by ID DESC ensures the newest "Settled Ledger" appears first
                allEntries = ledgerRepository.findAll(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "id"));
            } catch (DataAccessException e) {
                response.put("status", 500);
                response.put("message", "Database Error: Connection timed out or table 'ledgers' does not exist.");
                return ResponseEntity.status(500).body(response);
            }

            // --- 2. EMPTY DATA CHECK ---
            if (allEntries == null || allEntries.isEmpty()) {
                response.put("status", 200); // 200 is better for frontend than 204 for empty arrays
                response.put("message", "The ledger is currently empty. No records found.");
                response.put("data", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            // --- 3. SUCCESSFUL RESPONSE ---
            response.put("status", 200);
            response.put("message", "Successfully fetched " + allEntries.size() + " ledger entries.");
            response.put("data", allEntries);
            return ResponseEntity.ok(response);

        } catch (NullPointerException e) {
            response.put("status", 500);
            response.put("message", "Mapping Error: One or more records contain null values in required fields.");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            // FINAL SYSTEM CATCH-ALL
            response.put("status", 500);
            response.put("message", "Unexpected Critical Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/update-details/{invoiceNumber}")
    public ResponseEntity<Map<String, Object>> updateLedgerDetails(
            @PathVariable String invoiceNumber,
            @RequestBody Ledger updateRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            // --- 1. PATH VALIDATION ---
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                response.put("status", 400);
                response.put("message", "Invalid Request: Invoice Number in URL cannot be empty.");
                return ResponseEntity.badRequest().body(response);
            }

            // --- 2. DATABASE LOOKUP CATCH ---
            Ledger ledger;
            try {
                ledger = ledgerRepository.findByInvoiceNumber(invoiceNumber);
                if (ledger == null) {
                    response.put("status", 404);
                    response.put("message", "Not Found: No ledger entry exists for Invoice " + invoiceNumber);
                    return ResponseEntity.status(404).body(response);
                }
            } catch (DataAccessException e) {
                response.put("status", 500);
                response.put("message", "Database Error: Could not reach the ledger table to verify record.");
                return ResponseEntity.status(500).body(response);
            }

            // --- 3. DATA MAPPING & UPDATING ---
            try {
                // Field 1: Payment Terms
                if (updateRequest.getPaymentTerms() != null) {
                    ledger.setPaymentTerms(updateRequest.getPaymentTerms());
                }

                // Field 2: Due Days
                if (updateRequest.getDueDays() != null) {
                    ledger.setDueDays(updateRequest.getDueDays());
                }

                // Field 3: Invoice Created Date
                if (updateRequest.getInvoiceCreatedDate() != null) {
                    ledger.setInvoiceCreatedDate(updateRequest.getInvoiceCreatedDate());
                }
            } catch (Exception e) {
                response.put("status", 422);
                response.put("message", "Unprocessable Data: The values provided in the request body are invalid.");
                return ResponseEntity.status(422).body(response);
            }

            // --- 4. FINAL SAVE CATCH ---
            try {
                Ledger updatedLedger = ledgerRepository.save(ledger);

                response.put("status", 200);
                response.put("message", "Success: Ledger details updated for " + invoiceNumber);
                response.put("data", updatedLedger);
                return ResponseEntity.ok(response);

            } catch (DataIntegrityViolationException e) {
                response.put("status", 409);
                response.put("message", "Conflict: Data integrity violation occurred during update.");
                return ResponseEntity.status(409).body(response);
            }

        } catch (Exception e) {
            // FINAL SYSTEM CATCH
            response.put("status", 500);
            response.put("message", "Critical System Failure: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}