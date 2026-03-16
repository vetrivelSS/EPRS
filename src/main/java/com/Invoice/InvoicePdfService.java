package com.Invoice;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Color;

@Service
public class InvoicePdfService {
    // document.add(mainLayout);
    // public byte[] generateInvoicePdf(Invoice inv, List<Map<String, Object>>
    // items) {
    // *******************
    public byte[] generateInvoicePdf(Map<String, Object> inv, List<Map<String, Object>> items) {

        // ************
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Set page size and margins to match the clean look
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, out);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        // --- ADD THIS BORDER LOGIC ---
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                cb.setLineWidth(1.0f); // Professional thin line

                // Calculate position based on your Document margins (20, 20, 20, 20)
                float x = document.leftMargin();
                float y = document.bottomMargin();
                float width = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                float height = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();

                cb.rectangle(x, y, width, height);
                cb.stroke();
            }
        });
        // ----------------------------

        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font smallBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7); // Fixed your error
        // 1. Get current System Date
        String systemDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        Object refObj = inv.get("ref_count");
        int count = (refObj != null) ? Integer.parseInt(refObj.toString()) : 1;
        String referenceNo = String.format("%02d", count);
        // This turns 1 -> "01", 2 -> "02", etc.

        // --- 1. Top Title ---
        Paragraph title = new Paragraph("GST INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // --- 2. Main Header Table (Company Info | Invoice Details) ---
        PdfPTable mainLayout = new PdfPTable(2);
        mainLayout.setWidthPercentage(100);
        mainLayout.setWidths(new float[] { 1.2f, 1f });

        // --- LEFT COLUMN: COMPANY + CONSIGNEE + BUYER ---
        PdfPCell leftColumn = new PdfPCell();
        leftColumn.setPadding(0);

        // Section A: Company Details (Already in your code)
        PdfPTable companyTable = new PdfPTable(1);
        companyTable.setWidthPercentage(100);
        PdfPCell cCell = new PdfPCell();
        cCell.setPadding(5);
        cCell.addElement(new Phrase("SHINE LIGHT INDIA", boldFont));
        cCell.addElement(new Phrase(
                "\"Arihant Darshan Building\"\n46/41, Anna Pillai Street, Chennai - 600079\nGSTIN: 33AAMPS... ",
                normalFont));
        companyTable.addCell(cCell);
        leftColumn.addElement(companyTable);

        // --- FIXED Section B: Consignee (Ship to) ---
        PdfPTable shipTable = new PdfPTable(1); // ADDED THIS LINE
        shipTable.setWidthPercentage(100); // ADDED THIS LINE

        String customer = inv.get("customer_name") != null ? inv.get("customer_name").toString() : "N/A";
        String shipTo = inv.get("ship_to_address") != null ? inv.get("ship_to_address").toString() : "";

        PdfPCell sCell = new PdfPCell(new Phrase("Consignee (Ship to):\n" + customer + "\n" + shipTo, normalFont));
        sCell.setPadding(5);
        sCell.setMinimumHeight(80);
        shipTable.addCell(sCell);
        leftColumn.addElement(shipTable); // Add shipTable to the column

        // --- FIXED Section C: Buyer (Bill to) ---
        PdfPTable billTable = new PdfPTable(1); // ADDED THIS LINE
        billTable.setWidthPercentage(100); // ADDED THIS LINE

        String billTo = inv.get("billing_address") != null ? inv.get("billing_address").toString() : "";

        PdfPCell bCell = new PdfPCell(new Phrase("Buyer (Bill to):\n" + customer + "\n" + billTo, normalFont));
        bCell.setPadding(5);
        bCell.setMinimumHeight(80);
        billTable.addCell(bCell);
        leftColumn.addElement(billTable); // Add billTable to the column

        mainLayout.addCell(leftColumn);

        // --- RIGHT COLUMN: INVOICE DETAILS ---
        PdfPCell rightColumn = new PdfPCell();
        rightColumn.setPadding(0);

        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.getDefaultCell().setMinimumHeight(20); // Smaller height for rows

        metaTable.addCell(
                new Phrase("Invoice No:\n" + String.valueOf(inv.getOrDefault("invoice_number", "N/A")), normalFont));
        metaTable.addCell(new Phrase("Dated:\n" + systemDate, normalFont));
        metaTable.addCell(
                new Phrase("Delivery Note:\n" + String.valueOf(inv.getOrDefault("challan_number", "-")), normalFont));
        metaTable.addCell(
                new Phrase("Mode of Payment:\n" + String.valueOf(inv.getOrDefault("payment_terms", "-")), normalFont));
        metaTable.addCell(new Phrase("Reference No:\n" + referenceNo, normalFont));
        metaTable.addCell(new Phrase(
                "Buyer's Order No:\n " + String.valueOf(inv.getOrDefault("purchase_order_number", "-")), normalFont));
        metaTable.addCell(new Phrase("Delivery Note Date:\n" + String.valueOf(inv.getOrDefault("created_date", "-")),
                normalFont));

        String destinationVal = String.valueOf(inv.getOrDefault("destination", "-"));
        metaTable.addCell(new Phrase("Destination:\n" + destinationVal, normalFont));
        rightColumn.addElement(metaTable);
        mainLayout.addCell(rightColumn);
        // **

        // ****************
        document.add(mainLayout);

        // --- 3. Items Table ---
        PdfPTable itemTable = new PdfPTable(7);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[] { 0.5f, 4, 1.5f, 1, 1, 1, 1.5f });

        String[] headers = { "Sl", "Description of Goods", "HSN/SAC", "Quantity", "Rate", "Disc %", "Amount" };
        for (String h : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, boldFont));
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setBackgroundColor(Color.LIGHT_GRAY);
            hCell.setPadding(5);
            itemTable.addCell(hCell);
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> itemsList = new ArrayList<>();
        try {
            // Get the string: "[{"jobOrderNumber":"JOB-0044", ...}]"
            String jsonStr = String.valueOf(inv.get("dc_details_json"));

            // Convert that string into a Java List so we can loop through it
            itemsList = mapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            // If the JSON is empty or malformed, use the provided 'items' list as fallback
            itemsList = items;
        }

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);

            itemTable.addCell(createItemCell(String.valueOf(i + 1), normalFont, Element.ALIGN_CENTER));

            // 1. Get Values (Check your Map keys! They must match exactly)
            String material = String.valueOf(item.getOrDefault("material", "N/A"));
            String width = String.valueOf(item.getOrDefault("width", "0"));
            String length = String.valueOf(item.getOrDefault("length", "0"));
            String thickness = String.valueOf(item.getOrDefault("thickness", "0"));
            String quantityKg = String.valueOf(item.getOrDefault("quantityKg", "0"));
            // 2. Format exactly like Image 2: "Material _ Length x Width x Thickness -
            // Quantity Sheets"
            String fullDescription = String.format("%s _ %s x %s x %smm - %s Sheets",
                    material, length, width, thickness, quantityKg);

            // 3. Add to Table
            itemTable.addCell(createItemCell(fullDescription, normalFont, Element.ALIGN_LEFT));

            // Remaining columns
            itemTable.addCell(createItemCell("853620", normalFont, Element.ALIGN_CENTER));
            String quantityStr = String.valueOf(item.getOrDefault("quantityKg", "0"));

            itemTable.addCell(createItemCell(quantityStr + " Kg", normalFont, Element.ALIGN_RIGHT));
            itemTable.addCell(
                    createItemCell(String.valueOf(item.getOrDefault("ratePer", "0")), normalFont, Element.ALIGN_RIGHT));
            itemTable.addCell(createItemCell("Kg", normalFont, Element.ALIGN_CENTER));
            String totalAmountSt = String.valueOf(item.getOrDefault("totalAmount", "0"));
            itemTable.addCell(createItemCell(totalAmountSt, normalFont, Element.ALIGN_RIGHT));

            // itemTable.addCell(createItemCell(String.valueOf(item.getOrDefault("totalAmount",
            // "0")), normalFont,
            // Element.ALIGN_RIGHT));
        }
        // Fill empty space in Item Table
        // for (int i = 0; i < 5; i++) {
        // for (int j = 0; j < 7; j++)
        // itemTable.addCell(new PdfPCell(new Phrase(" ")));
        // }
        for (int i = 0; i < 6; i++) {
            PdfPCell spacer = new PdfPCell(new Phrase(" "));
            itemTable.addCell(spacer);
        }

        // Subtotal Row
        PdfPCell subTotalVal = new PdfPCell(
                new Phrase(String.valueOf(inv.getOrDefault("taxable_value", "0.00")), boldFont));
        subTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(subTotalVal);
        // --- SUB-TOTAL ROW ---

        addCleanTaxRow(itemTable, "CGST",
                String.valueOf(inv.getOrDefault("cgst_amount", "0.00")), normalFont);
        addCleanTaxRow(itemTable, "SGST",
                String.valueOf(inv.getOrDefault("sgst_amount", "0.00")), normalFont);
        addCleanTaxRow(itemTable, "Round Off",
                String.valueOf(inv.getOrDefault("discount", "0.00")), normalFont);

        // E. GRAND TOTAL ROW (With Borders again)

        PdfPCell totalLabel = new PdfPCell(new Phrase("Total", boldFont));
        totalLabel.setColspan(3);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalLabel);

        // Safely handle quantity_number
        String qtyTotal = String.valueOf(inv.getOrDefault("quantity_number", "0"));
        itemTable.addCell(createItemCell(qtyTotal + " Kg", boldFont,
                Element.ALIGN_RIGHT));

        itemTable.addCell(new PdfPCell(new Phrase(""))); // Rate
        itemTable.addCell(new PdfPCell(new Phrase(""))); // Per

        // Safely handle total_amount
        String grandTotalVal = String.valueOf(inv.getOrDefault("total_amount",
                "0.00"));
        PdfPCell finalAmount = new PdfPCell(new Phrase("₹ " + grandTotalVal,
                boldFont));
        finalAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(finalAmount);
        document.add(itemTable);
        // --- 4. Amount in Words (New Placement: AFTER items, BEFORE HSN) ---
        PdfPTable wordsTable = new PdfPTable(1);
        wordsTable.setWidthPercentage(100);
        PdfPCell wordsCell = new PdfPCell();
        wordsCell.setPadding(5);
        wordsCell.addElement(new Phrase("Amount Chargeable (in words):", smallFont));
        wordsCell.addElement(
                new Phrase("Indian Rupees " +
                        convertToWords(inv.get("total_amount").toString()) + " Only", boldFont));
        wordsTable.addCell(wordsCell);
        document.add(wordsTable);

        PdfPTable taxTable = new PdfPTable(5);
        taxTable.setWidthPercentage(100);
        taxTable.setSpacingBefore(10);
        taxTable.setWidths(new float[] { 1.5f, 2f, 2.5f, 2.5f, 2f });

        // --- COLUMN 1: HSN/SAC ---
        PdfPCell hsnHeader = new PdfPCell(new Phrase("HSN/SAC", smallBoldFont));
        hsnHeader.setBackgroundColor(Color.LIGHT_GRAY);
        hsnHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        hsnHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        hsnHeader.setPadding(5);
        taxTable.addCell(hsnHeader);

        // --- COLUMN 2: Taxable Value ---
        PdfPCell taxableHeader = new PdfPCell(new Phrase("Taxable Value", smallBoldFont));
        taxableHeader.setBackgroundColor(Color.LIGHT_GRAY);
        taxableHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        taxableHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        taxableHeader.setPadding(5);
        taxTable.addCell(taxableHeader);

        // --- COLUMN 3: Central Tax (SPLIT INTO RATE/AMOUNT) ---
        PdfPTable cgstNestedHeader = new PdfPTable(1);
        cgstNestedHeader.addCell(createSubHeaderTitle("Central Tax", smallBoldFont));
        PdfPTable cgstSubGrid = new PdfPTable(2);
        cgstSubGrid.addCell(createSubHeaderLabel("Rate", smallBoldFont));
        cgstSubGrid.addCell(createSubHeaderLabel("Amount", smallBoldFont));
        cgstNestedHeader.addCell(cgstSubGrid);

        PdfPCell cgstCell = new PdfPCell(cgstNestedHeader);
        cgstCell.setPadding(0); // Important to fit the nested table
        taxTable.addCell(cgstCell);

        // --- COLUMN 4: State Tax (SPLIT INTO RATE/AMOUNT) ---
        PdfPTable sgstNestedHeader = new PdfPTable(1);
        sgstNestedHeader.addCell(createSubHeaderTitle("State Tax", smallBoldFont));
        PdfPTable sgstSubGrid = new PdfPTable(2);
        sgstSubGrid.addCell(createSubHeaderLabel("Rate", smallBoldFont));
        sgstSubGrid.addCell(createSubHeaderLabel("Amount", smallBoldFont));
        sgstNestedHeader.addCell(sgstSubGrid);

        PdfPCell sgstCell = new PdfPCell(sgstNestedHeader);
        sgstCell.setPadding(0);
        taxTable.addCell(sgstCell);

        // --- COLUMN 5: Total Tax ---
        PdfPCell totalHeader = new PdfPCell(new Phrase("Total Tax", smallBoldFont));
        totalHeader.setBackgroundColor(Color.LIGHT_GRAY);
        totalHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalHeader.setPadding(5);
        taxTable.addCell(totalHeader);

        // --- ADD AN EMPTY ROW AS REQUESTED ---
        for (int i = 0; i < 5; i++) {
            // If it's the CGST or SGST column, we add the 2-column empty split
            if (i == 2 || i == 3) {
                PdfPTable emptySplit = new PdfPTable(2);
                emptySplit.addCell(new Phrase(" "));
                emptySplit.addCell(new Phrase(" "));
                PdfPCell emptyCell = new PdfPCell(emptySplit);
                emptyCell.setPadding(0);
                taxTable.addCell(emptyCell);
            } else {
                taxTable.addCell(new Phrase(" "));
            }
        }

        document.add(taxTable);

        // --- 6. Bank and Footer ---

        // --- 6. Unified Declaration & Bank Details Box (SINGLE BOX) ---

        // document.add(unifiedFooter);
        PdfPTable box1Table = new PdfPTable(1);
        box1Table.setWidthPercentage(100);
        box1Table.setSpacingBefore(10);

        PdfPCell box1Cell = new PdfPCell();
        box1Cell.setPadding(8);

        // 1. Tax Amount in Words (Full Width)
        Paragraph taxWords = new Paragraph();
        taxWords.add(new Phrase("Tax Amount (in words): ", smallBoldFont));
        taxWords.add(new Phrase("Indian Rupees One Thousand One Hundred Seven and Six Paise Only", smallFont));
        box1Cell.addElement(taxWords);

        // 2. Company's PAN (Full Width)
        Paragraph panPara = new Paragraph();
        panPara.add(new Phrase("Company's PAN: ", smallBoldFont));
        panPara.add(new Phrase("AAMPS5157N", smallFont)); // Replace with actual PAN
        box1Cell.addElement(panPara);

        // Add a small spacer line or gap
        box1Cell.addElement(new Phrase("\n"));

        // 3. Create a 2-column layout for Declaration and Bank Details
        PdfPTable innerBox1 = new PdfPTable(2);
        innerBox1.setWidthPercentage(100);
        innerBox1.setWidths(new float[] { 1.3f, 1f });

        // Left Column: Declaration
        PdfPCell declCell = new PdfPCell();
        declCell.setBorder(Rectangle.NO_BORDER);
        Paragraph declPara = new Paragraph();
        declPara.add(new Phrase("Declaration:\n", smallBoldFont));
        declPara.add(new Phrase(
                "We declare that this invoice shows the actual price of the goods described and that all particulars are true and correct.",
                smallFont));
        declCell.addElement(declPara);
        innerBox1.addCell(declCell);

        // Right Column: Bank Details
        PdfPCell bankCell = new PdfPCell();
        bankCell.setBorder(Rectangle.NO_BORDER);
        bankCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph bankPara = new Paragraph();
        bankPara.setAlignment(Element.ALIGN_RIGHT);
        bankPara.add(new Phrase("Company's Bank Details:\n", smallBoldFont));
        bankPara.add(new Phrase("Bank Name: YES BANK\nA/c No: 092781900000602\nIFSC: YESB0000927", smallFont));
        bankCell.addElement(bankPara);
        innerBox1.addCell(bankCell);

        box1Cell.addElement(innerBox1);
        box1Table.addCell(box1Cell);
        document.add(box1Table);
        // --- 7. Signatory (Outside the box, at the bottom right) ---

        PdfPTable signatureBoxTable = new PdfPTable(1);
        signatureBoxTable.setWidthPercentage(100);
        signatureBoxTable.setSpacingBefore(0); // Set to 0 if you want them touching, or 5 for a small gap

        PdfPCell signatureContainer = new PdfPCell();
        signatureContainer.setPadding(8);

        // Create the 2-column layout inside the signature box
        PdfPTable sigInner = new PdfPTable(2);
        sigInner.setWidthPercentage(100);
        sigInner.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Left: Customer's Seal and Signature
        PdfPCell custCell = new PdfPCell(new Phrase("\n\n\nCustomer's Seal and Signature", smallFont));
        custCell.setBorder(Rectangle.NO_BORDER);
        custCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        sigInner.addCell(custCell);

        // Right: Authorised Signatory
        PdfPCell authCell = new PdfPCell();
        authCell.setBorder(Rectangle.NO_BORDER);
        authCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph p1 = new Paragraph("for SHINE LIGHT INDIA", smallBoldFont);
        p1.setAlignment(Element.ALIGN_RIGHT);
        authCell.addElement(p1);

        authCell.addElement(new Phrase("\n\n\n")); // Space for physical signature/stamp

        Paragraph p2 = new Paragraph("Authorised Signatory", smallFont);
        p2.setAlignment(Element.ALIGN_RIGHT);
        authCell.addElement(p2);

        sigInner.addCell(authCell);

        // Add the inner table to the container cell
        signatureContainer.addElement(sigInner);
        signatureBoxTable.addCell(signatureContainer);

        document.add(signatureBoxTable);

        // --- FINAL WRAP UP ---
        document.close();
        return out.toByteArray();
    }

    // Helper Methods
    private void addMetaCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label + ": " + value, font));
        cell.setMinimumHeight(18);
        table.addCell(cell);
    }

    private PdfPCell createItemCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell createAddressCell(String text, Font font, boolean borderBottom) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(borderBottom ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createTaxHeader(String text, Font font, int rowspan, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setRowspan(rowspan);
        cell.setColspan(colspan);
        cell.setPadding(3);
        return cell;
    }

    // --- HELPER METHODS FOR THE NESTED LOOK ---
    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createSubHeaderTitle(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPadding(2);
        return cell;
    }

    private PdfPCell createSubHeaderLabel(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private void addCleanTaxRow(PdfPTable table, String label, String value, Font font) {
        // Spacer for first 5 columns to keep left side clean
        PdfPCell leftSpacer = new PdfPCell(new Phrase(" "));
        leftSpacer.setColspan(5);
        leftSpacer.setBorder(Rectangle.LEFT);
        table.addCell(leftSpacer);

        // Column 6 (Disc %): The Tax Label
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        // Column 7 (Amount): The Tax Value
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(Rectangle.RIGHT);
        table.addCell(valueCell);
    }

    private void addCleanTaxRowForGrid(PdfPTable table, String label, String value, Font font) {
        // Fill first 5 columns with empty cells
        for (int i = 0; i < 5; i++) {
            PdfPCell empty = new PdfPCell(new Phrase(" "));
            // Maintain vertical borders only
            empty.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            table.addCell(empty);
        }
        // 6th Column: Label
        PdfPCell lCell = new PdfPCell(new Phrase(label, font));
        lCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(lCell);

        // 7th Column: Value
        PdfPCell vCell = new PdfPCell(new Phrase(value, font));
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(vCell);
    }

    // --- ADD THIS METHOD AT THE BOTTOM OF YOUR CLASS ---
    public String convertToWords(String amountStr) {
        try {
            if (amountStr == null || amountStr.isEmpty())
                return "Zero";

            // Remove commas if any and convert to double
            double amount = Double.parseDouble(amountStr.replace(",", ""));
            long bytes = (long) amount;

            if (bytes == 0)
                return "Zero";

            String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                    "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen",
                    "Nineteen" };
            String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

            return convertPower(bytes, units, tens).trim();
        } catch (Exception e) {
            return "Error in conversion";
        }
    }

    private String convertPower(long n, String[] units, String[] tens) {
        if (n < 20)
            return units[(int) n];
        if (n < 100)
            return tens[(int) (n / 10)] + " " + units[(int) (n % 10)];
        if (n < 1000)
            return units[(int) (n / 100)] + " Hundred " + convertPower(n % 100, units, tens);
        if (n < 100000)
            return convertPower(n / 1000, units, tens) + " Thousand " + convertPower(n % 1000, units, tens);
        if (n < 10000000)
            return convertPower(n / 100000, units, tens) + " Lakh " + convertPower(n % 100000, units, tens);
        return convertPower(n / 10000000, units, tens) + " Crore " + convertPower(n % 10000000, units, tens);
    }

    private void addTaxRow(PdfPTable table, String label, double value, Font font) {
        // Columns 1 to 5 are empty
        for (int i = 0; i < 5; i++) {
            table.addCell(new PdfPCell(new Phrase(" ")));
        }
        // Column 6 is the Label
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellLabel);

        // Column 7 is the Value
        PdfPCell cellValue = new PdfPCell(new Phrase(String.format("%.2f", value), font));
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }
}
