package com.example.regulationsbatch.util;

import com.example.regulationsbatch.model.CommentRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PdfGenerator {

    public byte[] generateCommentPdf(CommentRecord comment) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 11);
            content.setLeading(14.5f);
            content.newLineAtOffset(50, 700);

            writeLine(content, "Comment ID: " + safe(comment.getCommentId()));
            writeLine(content, "Docket ID: " + safe(comment.getDocketId()));
            writeLine(content, "Document ID: " + safe(comment.getCommentOnDocumentId()));
            writeLine(content, "Posted Date: " + safe(comment.getPostedDate()));
            writeLine(content, "Organization: " + safe(comment.getOrganization()));
            writeLine(content, "Title: " + safe(comment.getTitle()));
            writeLine(content, "");
            writeLine(content, "Comment:");
            writeWrapped(content, safe(comment.getComment()));
            content.endText();
            content.close();

            doc.save(out);
            return out.toByteArray();
        }
    }

    private void writeLine(PDPageContentStream content, String s) throws IOException {
        content.showText(s);
        content.newLine();
    }

    private void writeWrapped(PDPageContentStream content, String s) throws IOException {
        if (s == null) { content.showText("N/A"); content.newLine(); return; }
        int width = 80;
        int pos = 0;
        while (pos < s.length()) {
            int end = Math.min(pos + width, s.length());
            content.showText(s.substring(pos, end));
            content.newLine();
            pos = end;
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
