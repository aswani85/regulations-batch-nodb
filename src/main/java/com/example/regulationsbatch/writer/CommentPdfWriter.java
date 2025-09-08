package com.example.regulationsbatch.writer;

import com.example.regulationsbatch.model.CommentRecord;
import com.example.regulationsbatch.util.PdfGenerator;
import com.example.regulationsbatch.util.RegGovClient;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@StepScope
public class CommentPdfWriter implements ItemWriter<CommentRecord> {

    private static final Logger log = LoggerFactory.getLogger(CommentPdfWriter.class);

    @Autowired
    private PdfGenerator pdfGenerator;

    /*@Autowired
    private SharePointUploader uploader;
    */
    @Autowired
    private RegGovClient client;

    @Override
    public void write(Chunk<? extends CommentRecord> chunk) throws Exception {
        for (CommentRecord c : chunk) {
            if (c == null) {
                continue; // safety check
            }

            // Generate PDF
            byte[] pdf = pdfGenerator.generateCommentPdf(c);
            String folder = (c.getDocketId() != null) ? c.getDocketId() : "";
            String pdfName = c.getCommentId() + ".pdf";

            //uploader.uploadBytes(folder, pdfName, pdf);
            log.info("Uploaded PDF for comment {} to folder {}", c.getCommentId(), folder);

            // Upload attachments if any
            if (c.getAttachmentUrls() != null && !c.getAttachmentUrls().isEmpty()) {
                for (String url : c.getAttachmentUrls()) {
                    try {
                        byte[] fileBytes = client.downloadBytes(url);

                        String fname = url.substring(url.lastIndexOf('/') + 1);
                        //fname = uploader.normalizeFileName(fname);

                        //uploader.uploadBytes(folder, c.getCommentId() + "_" + fname, fileBytes);
                        log.info("Uploaded attachment {} for comment {}", fname, c.getCommentId());

                    } catch (Exception ex) {
                        log.error("Failed to upload attachment {} for comment {}: {}",
                                url, c.getCommentId(), ex.getMessage(), ex);
                    }
                }
            }
        }
    }
}
