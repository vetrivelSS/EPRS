package com.Invoice;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.util.List;
import java.util.Map;
import java.awt.Color;

@Service
public class InvoicePdfService {
    // document.add(mainLayout);
    public byte[] generateInvoicePdf(Invoice inv, List<Map<String, Object>> items) {
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

        // Section A: Company Details (At the very top left)
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

        // Section B: Consignee (Ship to) - Directly below company
        PdfPTable shipTable = new PdfPTable(1);
        shipTable.setWidthPercentage(100);
        PdfPCell sCell = new PdfPCell(new Phrase(
                "Consignee (Ship to):\n" + inv.getCustomerName() + "\n" + inv.getShipToAddress(),
                normalFont));
        sCell.setPadding(5);
        sCell.setMinimumHeight(80);
        shipTable.addCell(sCell);
        leftColumn.addElement(shipTable);

        // Section C: Buyer (Bill to) - Directly below Consignee
        PdfPTable billTable = new PdfPTable(1);
        billTable.setWidthPercentage(100);
        PdfPCell bCell = new PdfPCell(
                new Phrase("Buyer (Bill to):\n" + inv.getCustomerName() + "\n" + inv.getBillToAddress(),
                        normalFont));
        bCell.setPadding(5);
        bCell.setMinimumHeight(80);
        billTable.addCell(bCell);
        leftColumn.addElement(billTable);

        mainLayout.addCell(leftColumn);

        // --- RIGHT COLUMN: INVOICE DETAILS ---
        PdfPCell rightColumn = new PdfPCell();
        rightColumn.setPadding(0);

        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.getDefaultCell().setMinimumHeight(20); // Smaller height for rows

        metaTable.addCell(new Phrase("Invoice No:\n" + inv.getInvoiceNumber(), normalFont));
        metaTable.addCell(new Phrase("Dated:\n20-Dec-25", normalFont));

        metaTable.addCell(new Phrase("Delivery Note:\n", normalFont));
        metaTable.addCell(new Phrase("Mode of Payment:\n", normalFont));

        metaTable.addCell(new Phrase("Reference No. & Date:\n", normalFont));
        metaTable.addCell(new Phrase("Other Reference:\n", normalFont));

        metaTable.addCell(new Phrase("Buyer's Order No:\n", normalFont));
        metaTable.addCell(new Phrase("Dated:\n", normalFont));

        metaTable.addCell(new Phrase("Dispatch Doc No:\n", normalFont));
        metaTable.addCell(new Phrase("Delivery Note Date:\n", normalFont));

        metaTable.addCell(new Phrase("Dispatched through:\n", normalFont));
        metaTable.addCell(new Phrase("Destination:\n", normalFont));

        // Add enough empty rows to the right table to match the height of the left
        // column
        // for (int i = 0; i < 1; i++) {
        // metaTable.addCell(new Phrase(" \n ", normalFont));
        // metaTable.addCell(new Phrase(" \n ", normalFont));
        // }

        rightColumn.addElement(metaTable);
        mainLayout.addCell(rightColumn);

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
            itemTable.addCell(hCell);
        }

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            itemTable.addCell(createItemCell(String.valueOf(i + 1), normalFont, Element.ALIGN_CENTER));
            itemTable.addCell(createItemCell(item.get("material").toString(), normalFont, Element.ALIGN_LEFT));
            itemTable.addCell(createItemCell("853620", normalFont, Element.ALIGN_CENTER));
            itemTable.addCell(
                    createItemCell(item.get("quantityKg").toString() + " Pcs", normalFont, Element.ALIGN_RIGHT));
            itemTable.addCell(createItemCell(item.get("ratePer").toString(), normalFont, Element.ALIGN_RIGHT));
            itemTable.addCell(createItemCell(item.get("discount").toString(), normalFont, Element.ALIGN_RIGHT));
            itemTable.addCell(createItemCell(item.get("totalAmount").toString(), normalFont, Element.ALIGN_RIGHT));
        }

        // Fill empty space in Item Table
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++)
                itemTable.addCell(new PdfPCell(new Phrase(" ")));
        }
        document.add(itemTable);

        // --- 4. Amount in Words (New Placement: AFTER items, BEFORE HSN) ---
        PdfPTable wordsTable = new PdfPTable(1);
        wordsTable.setWidthPercentage(100);
        PdfPCell wordsCell = new PdfPCell();
        wordsCell.setPadding(5);
        wordsCell.addElement(new Phrase("Amount Chargeable (in words):", smallFont));
        wordsCell.addElement(new Phrase("Indian Rupees Seven Thousand Three Hundred Two Only", normalFont));
        wordsTable.addCell(wordsCell);
        document.add(wordsTable);

        // --- 5. HSN/SAC Tax Summary Table ---
        // 1. Create the main 5-column table
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

}
