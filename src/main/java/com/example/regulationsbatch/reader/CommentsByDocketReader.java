package com.example.regulationsbatch.reader;

import com.example.regulationsbatch.model.CommentRecord;
import com.example.regulationsbatch.util.RegGovClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Queue;

@Component
@StepScope
public class CommentsByDocketReader implements ItemStreamReader<CommentRecord>, ItemStream {

    @Autowired
    private RegGovClient client;

    @Value("#{jobParameters['docketId']}")
    private String docketId;

    @Value("#{jobParameters['startCommentId']}")
    private String startCommentId;

    private Queue<JsonNode> buffer = new ArrayDeque<>();
    private Queue<String> documentIds = new ArrayDeque<>();
    private int docPage = 1;
    private volatile boolean reachedStart = false;

    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public CommentRecord read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // Load next batch if buffer empty
        while (buffer.isEmpty()) {
            if (documentIds.isEmpty()) {
                JsonNode docsResp = client.getPaged("/documents", java.util.Map.of("filter[docketId]", docketId, "page[size]","250","page[number]", String.valueOf(docPage)));
                // increment page for next call
                docPage++;
                JsonNode docsData = docsResp.path("data");
                if (docsData.isArray() && docsData.size() > 0) {
                    for (JsonNode d : docsData) {
                        documentIds.add(d.path("id").asText());
                    }
                } else {
                    System.out.println("There are no documents with given docketId: " + docketId);
                    return null; // no more documents
                }
            }
            String docId = documentIds.poll();
            // get objectId
            JsonNode docDetail = client.getDetail("/documents/" + docId);
            String objectId = docDetail.path("data").path("attributes").path("objectId").asText(null);
            if (objectId == null || objectId.isEmpty()) continue;
            int page = 1;
            while (true) {
                JsonNode resp = client.getPaged("/comments", java.util.Map.of("filter[commentOnId]", objectId, "page[size]","250","page[number]", String.valueOf(page++)));
                JsonNode data = resp.path("data");
                if (!data.isArray() || data.size() == 0) break;
                for (JsonNode c : data) buffer.add(c);
                boolean last = resp.path("meta").path("lastPage").asBoolean(false);
                if (last) break;
            }
        }

        JsonNode node = buffer.poll();
        if (node == null) return null;

        String cid = node.path("id").asText();
        // If startCommentId is null/empty -> process all. If provided, skip until we see it, then start AFTER it.
        if (startCommentId != null && !startCommentId.isBlank() && !reachedStart) {
            if (cid.equals(startCommentId)) {
                reachedStart = true;
                saveState(cid);
                return read(); // start after provided id
            } else {
                // skip this one and continue
                saveState(cid);
                return read();
            }
        }

        // create CommentRecord header object; processor will fetch detail
        CommentRecord r = new CommentRecord();
        r.setCommentId(cid);
        JsonNode a = node.path("attributes");
        r.setAgencyId(a.path("agencyId").asText(null));
        r.setDocumentType(a.path("documentType").asText(null));
        r.setLastModifiedDate(a.path("lastModifiedDate").asText(null));
        r.setObjectId(a.path("objectId").asText(null));
        r.setPostedDate(a.path("postedDate").asText(null));
        r.setTitle(a.path("title").asText(null));
        r.setWithdrawn(a.path("withdrawn").isMissingNode()? null : (a.path("withdrawn").asBoolean(false) ? 1 : 0));
        saveState(cid);
        return r;
    }

    private void saveState(String lastCommentId) {
        if (stepExecution != null) {
            ExecutionContext ec = stepExecution.getExecutionContext();
            ec.putInt("docPage", docPage);
            ec.put("reachedStart", reachedStart);
            ec.putString("lastCommentId", lastCommentId);
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext != null) {
            if (executionContext.containsKey("docPage")) {
                this.docPage = executionContext.getInt("docPage");
            }
            if (executionContext.containsKey("reachedStart")) {
                this.reachedStart = (Boolean) executionContext.get("reachedStart");
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext != null) {
            executionContext.putInt("docPage", docPage);
            executionContext.put("reachedStart", reachedStart);
        }
    }

    @Override
    public void close() throws ItemStreamException {}
}
