package com.DeliveryChallan;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfGeneratorService {
    @Autowired
    private ObjectMapper objectMapper;

    public byte[] generateChallanPdf(DeliveryChallan dc) {
        if (dc == null) {
            System.out.println("DEBUG: The DeliveryChallan object is NULL. Check your Repository.");
            return new byte[0];
        }

        System.out.println("DEBUG: Fetching data for DC: " + dc.getChallanNumber());
        if (dc.getProductionsJson() == null || dc.getProductionsJson().isEmpty()) {
            System.out.println("DEBUG: productionsJson is NULL or EMPTY in the database.");
        } else {
            System.out.println("DEBUG: productionsJson contains: " + dc.getProductionsJson());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Margin bottom 200f to reserve space for the footer
        // Reduce 200f to 100f to give the table more room to grow
        Document document = new Document(PageSize.A4, 30f, 30f, 40f, 100f);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        // 1. GLOBAL FONTS // Your existing fonts for the rest of the document
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        // Inside your font section
        Font noteFont = FontFactory.getFont(FontFactory.HELVETICA, 11); // Increased size
        Font noteBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12); // Bold and bigger
        // --- 1. GLOBAL FONTS ---
        // Change these lines to the new sizes for your header boxes
        Font boxLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font boxValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        // --- 1. OUTER BORDER ---
        PdfContentByte cb = writer.getDirectContent();
        cb.rectangle(20, 20, PageSize.A4.getWidth() - 40, PageSize.A4.getHeight() - 40);
        cb.setLineWidth(1.2f);
        cb.stroke();

        // --- 2. TOP HEADER SECTION ---
        PdfPTable mainTable = new PdfPTable(3);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[] { 3.5f, 3.5f, 3f });

        // A. COMPANY DETAILS (TOP LEFT)
        PdfPCell companyCell = new PdfPCell();
        companyCell.setPadding(8);
        try {
            Image logo = Image.getInstance("C:\\Users\\Vishnu\\Downloads\\Untitled design.png");
            logo.scaleToFit(80, 50);
            companyCell.addElement(logo);
        } catch (Exception e) {
        }

        Paragraph companyInfo = new Paragraph();
        companyInfo.setLeading(12f);
        companyInfo.add(new Chunk("Eswari Electricals Private Ltd\n", boldFont));
        companyInfo
                .add(new Chunk("No-64, Industrial Estate,\nPerungudi, Chennai\n600096\n42152122/23\n\n", normalFont));
        companyInfo.add(new Chunk("GSTIN: ", boldFont));
        companyInfo.add(new Chunk("33AAACE8247L1ZH", boldFont));
        companyCell.addElement(companyInfo);
        mainTable.addCell(companyCell);

        // B. TO: ADDRESS (CENTER)
        PdfPCell consigneeCell = new PdfPCell();
        consigneeCell.setPadding(8);
        consigneeCell.addElement(new Paragraph("INTER-UNIT DELIVERY CHALLAN", boldFont));
        consigneeCell.addElement(new Phrase("\nTO:", boldFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getCustomerName(), boldFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getShipToAddress(), normalFont));
        mainTable.addCell(consigneeCell);

        // C. DC INFO (TOP RIGHT) - Now with even spacing
        PdfPTable dcInfoTable = new PdfPTable(1);
        dcInfoTable.addCell(createLabelValueCell("DC NO", dc.getChallanNumber(), boldFont));
        dcInfoTable.addCell(createLabelValueCell("DATE", dc.getCreatedDate(), boldFont));
        dcInfoTable.addCell(createLabelValueCell("VEHICLE NO", dc.getVehicleNumber(), boldFont));

        PdfPCell infoContainer = new PdfPCell(dcInfoTable);
        infoContainer.setPadding(0);
        mainTable.addCell(infoContainer);

        document.add(mainTable);

        // --- SPACE BEFORE ITEMS TABLE ---
        Paragraph tableSpacer = new Paragraph(" ");
        tableSpacer.setSpacingBefore(15f);
        tableSpacer.setSpacingAfter(10f);
        document.add(tableSpacer);

        // --- 3. DYNAMIC ITEMS TABLE ---
        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        // Current: { 1.5f, 1f, 1.5f, 5f, 2f }
        // Updated: More balanced widths for Quantity and Description
        itemTable.setWidths(new float[] { 2f, 1.5f, 2f, 4.5f, 2.5f });

        // Table Headers
        String[] headers = { "QUANTITY", "UNITS", "HSN CODE", "DESCRIPTION", "VALUE" };
        for (String h : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, boldFont));
            hCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setPadding(6);
            itemTable.addCell(hCell);
        }

        double grandTotal = 0.0;

        // --- FIX: Declare the list outside the try block ---
        List<Map<String, Object>> items = null;

        try {
            items = objectMapper.readValue(dc.getProductionsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            // Log for debugging
            System.out.println("DEBUG: Number of items found: " + (items != null ? items.size() : 0));

        } catch (Exception e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        }

        if (items != null) {
            for (Map<String, Object> item : items) {
                // 1. Add cells for Quantity, Units, HSN, and Description
                itemTable.addCell(createPaddedCell(item.get("quentity").toString(), normalFont, 8));
                itemTable.addCell(createPaddedCell("Kg", normalFont, 8));
                itemTable.addCell(createPaddedCell(item.get("hsnCode").toString(), normalFont, 8));
                itemTable.addCell(createPaddedCell(item.get("metrails").toString(), normalFont, 8));

                // --- INSERT THE NEW SAFE PARSING LOGIC HERE ---
                Object totalObj = item.get("totalAmount");
                double val = 0.0;
                if (totalObj != null) {
                    try {
                        val = Double.parseDouble(totalObj.toString());
                    } catch (NumberFormatException e) {
                        System.err.println("DEBUG: Could not parse amount: " + totalObj);
                    }
                }
                grandTotal += val;
                // ----------------------------------------------

                // 2. Add the Value cell to the table
                PdfPCell vCell = createPaddedCell(String.format("%.2f", val), normalFont, 5);
                vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemTable.addCell(vCell);
            }
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL VALUE", boldFont));
        totalLabel.setColspan(4);
        totalLabel.setPadding(8);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalLabel);

        PdfPCell totalValCell = new PdfPCell(new Phrase(String.format("%.2f", grandTotal), boldFont));
        totalValCell.setPadding(8);
        totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalValCell);

        document.add(itemTable);

        // --- 4. FIXED FOOTER ---
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setTotalWidth(PageSize.A4.getWidth() - 60);
        // 1. Create a Phrase to hold the sentence
        Phrase notePhrase = new Phrase();

        // Use noteFont (11pt) instead of normalFont
        notePhrase.add(new Chunk(
                "\nThis Material has been sent for STOCK TRANSFER Purpose only and it does not have Any commercial Value. Its Approximate value ",
                noteFont));

        // Use noteBoldFont (12pt) for the Rs. and Total
        String formattedTotal = String.format("Rs. %,.2f /-", grandTotal);
        notePhrase.add(new Chunk(formattedTotal, noteBoldFont));

        notePhrase.add(new Chunk("\n", noteFont));

        PdfPCell noteCell = new PdfPCell(notePhrase);
        noteCell.setPadding(12f); // Increased padding for a larger box
        noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerTable.addCell(noteCell);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        // 1. Create the cell for the Left side
        PdfPCell receivedCell = new PdfPCell(new Phrase("\nRECEIVED SEAL & SIGNATURE\n\n\n\n", boldFont));

        // 2. Set the horizontal alignment to Center
        receivedCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        // 3. Add the cell to the table
        sigTable.addCell(receivedCell);
        PdfPCell prepCell = new PdfPCell(
                new Phrase("\nPREPARED BY\nFOR ESWARI ELECTRICALS PVT. LTD.,\n\n\n\n", boldFont));
        prepCell.setPadding(10);
        prepCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        sigTable.addCell(prepCell);
        footerTable.addCell(new PdfPCell(sigTable));

        PdfPCell footerSpacer = new PdfPCell(new Phrase(" "));
        footerSpacer.setBorder(Rectangle.NO_BORDER);
        footerSpacer.setFixedHeight(15f);
        footerTable.addCell(footerSpacer);

        Paragraph addressPara = new Paragraph();
        addressPara.add(new Chunk("ESWARI ELECTRICALS PVT LTD.,\n", boldFont));
        addressPara.add(new Chunk("REGD OFF: PLOT NO.64 INDUSTRIAL ESTATE, PERUNGUDI, CHENNAI 600096.\n", normalFont));
        addressPara.add(new Chunk("+9144 24961693 / 42152122", normalFont));

        PdfPCell addrCell = new PdfPCell(addressPara);
        addrCell.setBorder(Rectangle.BOX);
        addrCell.setBorderWidth(1.2f);
        addrCell.setPadding(10f);
        addrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        addrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        addrCell.setBackgroundColor(new java.awt.Color(245, 245, 245));
        footerTable.addCell(addrCell);

        footerTable.writeSelectedRows(0, -1, 30, 245, writer.getDirectContent());

        document.close();
        return out.toByteArray();
    }

    private PdfPCell createPaddedCell(String text, Font font, float padding) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(padding);
        return cell;
    }

    private PdfPCell createLabelValueCell(String label, String value, Font font) {
        PdfPCell cell = new PdfPCell();

        // Sets the fixed height so all boxes are even
        cell.setFixedHeight(45f);

        // ALIGN TOP: This ensures the label stays at the very top of the box
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3); // Small padding so text doesn't touch the top line

        Paragraph p = new Paragraph();
        // Leading controls the space between lines.
        // 12f is tight, leaving the rest of the 45f height as empty space
        p.setLeading(12f);
        p.setAlignment(Element.ALIGN_CENTER);

        // The Label (e.g., DATE) - Stays at the top
        p.add(new Chunk(label + "\n", font));

        // The Value - Will appear below the label, leaving empty space at the bottom
        p.add(new Chunk(value != null ? value : "", font));

        cell.addElement(p);
        return cell;
    }
}