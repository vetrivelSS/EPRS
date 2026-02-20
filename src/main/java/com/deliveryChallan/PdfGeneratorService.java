package com.deliveryChallan;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public byte[] generateChallanPdf(DeliveryChallan dc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30f, 30f, 30f, 20f);
        PdfWriter.getInstance(document, out);

        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // --- OUTER BORDER TABLE ---
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        
        PdfPCell mainLayoutCell = new PdfPCell();
        mainLayoutCell.setPadding(10f);      
        mainLayoutCell.setBorderWidth(1.5f); 

        // --- 1. TOP HEADER TABLE ---
        PdfPTable mainTable = new PdfPTable(3);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{3.5f, 3f, 2.5f});

        PdfPCell companyCell = new PdfPCell();
        companyCell.setPadding(5);
        try {
            String logoPath = "C:\\Users\\Vishnu\\Downloads\\Untitled design.png";
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(90, 60); 
            logo.setAlignment(Element.ALIGN_LEFT);
            companyCell.addElement(logo);
        } catch (Exception e) {
            System.out.println("Logo Error: " + e.getMessage());
        }
        companyCell.addElement(new Paragraph("Eswari Electricals Private Ltd", headerFont));
        companyCell.addElement(new Paragraph("No-64, Industrial Estate, Perungudi,\nChennai 600096\n42152122/23", headerFont));
        companyCell.addElement(new Paragraph("\nGSTIN: 33AAACE8247L1ZH", headerFont));
        mainTable.addCell(companyCell);
        
        PdfPCell consigneeCell = new PdfPCell();
        Paragraph title = new Paragraph("INTER-UNIT DELIVERY CHALLAN", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        consigneeCell.addElement(title);
        consigneeCell.addElement(new Phrase("\nTO:", headerFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getCustomerName(), headerFont));
        consigneeCell.addElement(new Phrase("\n" + dc.getShipToAddress(), normalFont));
        mainTable.addCell(consigneeCell);

        PdfPTable dcInfo = new PdfPTable(1);
        dcInfo.addCell(createLabelValueCell("DC NO", dc.getChallanNumber(), headerFont));
        dcInfo.addCell(createLabelValueCell("DATE", dc.getCreatedDate(), headerFont));
        dcInfo.addCell(createLabelValueCell("VEHICLE NO", dc.getVehicleNumber(), headerFont));
        PdfPCell infoCell = new PdfPCell(dcInfo);
        infoCell.setPadding(0);
        mainTable.addCell(infoCell);

        mainLayoutCell.addElement(mainTable);

        // --- 2. ITEMS TABLE ---
        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setSpacingBefore(10f);
        itemTable.setWidths(new float[]{1.5f, 1f, 1.5f, 5f, 2f});
        
        String[] headers = {"QUANTITY", "UNITS", "HSN CODE", "DESCRIPTION", "VALUE"};
        for (String h : headers) {
            itemTable.addCell(new PdfPCell(new Phrase(h, headerFont)));
        }

        // Add 1 Data Row
        itemTable.addCell(new Phrase(dc.getQuantityKg(), normalFont));
        itemTable.addCell(new Phrase(dc.getUnits(), normalFont));
        itemTable.addCell(new Phrase(dc.getHsnCode(), normalFont));
        itemTable.addCell(new Phrase(dc.getMaterial(), normalFont));
        itemTable.addCell(new Phrase("0.00", normalFont));

        // Add 5 empty rows to make exactly 6 grid rows
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                itemTable.addCell(new Phrase(" ", normalFont));
            }
        }
        
        // TOTAL VALUE ROW
        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL VALUE", headerFont));
        totalLabel.setColspan(4);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalLabel);

        PdfPCell totalValCell = new PdfPCell(new Phrase("0.00", headerFont));
        itemTable.addCell(totalValCell);

        // --- THE CLEAN STRETCHER (Removes internal vertical lines) ---
        PdfPCell stretchCell = new PdfPCell(new Phrase(" "));
        stretchCell.setColspan(5); // Merges all 5 columns to remove vertical lines
        stretchCell.setMinimumHeight(350f); // Adjust this value to fill the page
        // Only draw the outer LEFT and RIGHT borders
        stretchCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT); 
        itemTable.addCell(stretchCell);

        // Bottom border line for the item table
        PdfPCell bottomBorder = new PdfPCell(new Phrase(" "));
        bottomBorder.setColspan(5);
        bottomBorder.setFixedHeight(1f);
        bottomBorder.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        itemTable.addCell(bottomBorder);

        mainLayoutCell.addElement(itemTable);
        
        // --- 3. FOOTER NOTE ---
        PdfPTable noteBox = new PdfPTable(1);
        noteBox.setWidthPercentage(100);
        noteBox.setSpacingBefore(5f);
        PdfPCell noteCell = new PdfPCell(new Phrase("This Material has been sent for STOCK TRANSFER Purpose only...", smallFont));
        noteCell.setPadding(5f);
        noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        noteBox.addCell(noteCell);
        mainLayoutCell.addElement(noteBox);

        // --- 4. SIGNATURE SECTION ---
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(10f);

        PdfPCell sealCell = new PdfPCell(new Phrase("RECEIVED SEAL & SIGNATURE\n\n\n\n", headerFont));
        sealCell.setMinimumHeight(60f);
        footerTable.addCell(sealCell);

        PdfPCell prepCell = new PdfPCell(new Phrase("PREPARED BY\nFOR ESWARI ELECTRICALS PVT. LTD.,\n\n\n", headerFont));
        prepCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerTable.addCell(prepCell);

        mainLayoutCell.addElement(footerTable);

        // --- 5. REG OFFICE FOOTER ---
        Paragraph footerText = new Paragraph(
            "\nESWARI ELECTRICALS PVT LTD.,\n" +
            "REGD OFF: PLOT NO.64 INDUSTRIAL ESTATE, PERUNGUDI, CHENNAI 600096.\n" +
            "+9144 24961693 / 42152122", smallFont);
        footerText.setAlignment(Element.ALIGN_CENTER);
        mainLayoutCell.addElement(footerText);

        outerTable.addCell(mainLayoutCell);
        document.add(outerTable);

        document.close();
        return out.toByteArray();
    }

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