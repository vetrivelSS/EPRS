package com.deliveryChallan;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.awt.Color;

@Service
public class PdfGeneratorService {

    public byte[] generateChallanPdf(DeliveryChallan dc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, out);

        document.open();

        // Fonts
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // Main Border Table
        PdfPTable mainTable = new PdfPTable(3);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{3.5f, 3f, 2.5f});

        // 1. TOP LEFT: Company Logo & Address
        PdfPCell companyCell = new PdfPCell();
        companyCell.addElement(new Phrase("Eswari Electricals Private Ltd", headerFont));
        companyCell.addElement(new Phrase("No-64, Industrial Estate, Perungudi, Chennai 600096", normalFont));
        companyCell.addElement(new Phrase("GSTIN: 33AAACE8247L1ZH", normalFont));
        mainTable.addCell(companyCell);

        // 2. TOP MIDDLE: Title & Consignee
        PdfPCell consigneeCell = new PdfPCell();
        Paragraph title = new Paragraph("INTER-UNIT DELIVERY CHALLAN", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        consigneeCell.addElement(title);
        consigneeCell.addElement(new Phrase("\nTO:", headerFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getCustomerName(), normalFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getShipToAddress(), normalFont));
        mainTable.addCell(consigneeCell);

        // 3. TOP RIGHT: DC Info Table (Nested)
        PdfPTable dcInfo = new PdfPTable(1);
        dcInfo.addCell(createLabelValueCell("DC NO", dc.getChallanNumber(), headerFont));
        dcInfo.addCell(createLabelValueCell("DATE", dc.getCreatedDate(), headerFont));
        dcInfo.addCell(createLabelValueCell("VEHICLE NO", dc.getVehicleNumber(), headerFont));
        PdfPCell infoCell = new PdfPCell(dcInfo);
        infoCell.setPadding(0);
        mainTable.addCell(infoCell);

        document.add(mainTable);

     // 4. ITEMS TABLE
        PdfPTable itemTable = new PdfPTable(5); // Correctly set to 5 columns
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{1.5f, 1f, 1.5f, 5f, 2f});
        
        // Headers (5 headers)
        itemTable.addCell(new PdfPCell(new Phrase("QUANTITY", headerFont)));
        itemTable.addCell(new PdfPCell(new Phrase("UNITS", headerFont)));
        itemTable.addCell(new PdfPCell(new Phrase("HSN CODE", headerFont)));
        itemTable.addCell(new PdfPCell(new Phrase("DESCRIPTION", headerFont)));
        itemTable.addCell(new PdfPCell(new Phrase("VALUE", headerFont)));

        // --- (Added only 5 cells per row) ---
        
        // Row 1
        itemTable.addCell(new Phrase(dc.getQuantityKg(), normalFont));
        itemTable.addCell(new Phrase(dc.getUnits(), normalFont));
        itemTable.addCell(new Phrase(dc.getHsnCode(), normalFont));
        itemTable.addCell(new Phrase(dc.getMaterial(), normalFont));
        itemTable.addCell(new Phrase("0.00", normalFont));

        for (int i = 0; i < 4; i++) {
            itemTable.addCell(new Phrase(" ", normalFont));
            itemTable.addCell(new Phrase(" ", normalFont));
            itemTable.addCell(new Phrase(" ", normalFont));
            itemTable.addCell(new Phrase(" ", normalFont));
            itemTable.addCell(new Phrase(" ", normalFont));
        }
        
        // Total Row
        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL VALUE", headerFont));
        totalLabel.setColspan(4); // Spans Quantity, Units, HSN, and Description
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalLabel);
        itemTable.addCell(new Phrase("0.00", headerFont)); 
        document.add(itemTable);
        
        // 5. FOOTER SECTION
        Paragraph note = new Paragraph("\nThis Material has been sent for STOCK TRANSFER Purpose only...", smallFont);
        document.add(note);

        document.close();
        return out.toByteArray();
    }

    // Helper for DC Info labels
    private PdfPCell createLabelValueCell(String label, String value, Font font) {
        PdfPCell cell = new PdfPCell();
        Paragraph p = new Paragraph(label, font);
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        Paragraph v = new Paragraph(value != null ? value : "", font);
        v.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(v);
        return cell;
    }
}