package com.example.regulationsbatch.processor;

import com.example.regulationsbatch.model.CommentRecord;
import com.example.regulationsbatch.util.RegGovClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class CommentsDetailProcessor implements ItemProcessor<CommentRecord, CommentRecord> {

    @Autowired
    private RegGovClient client;

    @Override
    public CommentRecord process(CommentRecord item) throws Exception {
        JsonNode resp = client.getDetail("/comments/" + item.getCommentId() + "?include=attachments");
        JsonNode a = resp.path("data").path("attributes");
        item.setComment(a.path("comment").asText(null));
        item.setCommentOn(a.path("commentOn").asText(null));
        item.setCommentOnDocumentId(a.path("commentOnDocumentId").asText(null));
        item.setDocketId(a.path("docketId").asText(null));
        item.setOrganization(a.path("organization").asText(null));
        item.setPostedDate(a.path("postedDate").asText(null));
        item.setModifyDate(a.path("modifyDate").asText(null));

        List<String> urls = new ArrayList<>();
        if (resp.has("included") && resp.path("included").isArray()) {
            for (JsonNode inc : resp.path("included")) {
                if (inc.path("attributes").has("fileFormats")) {
                    for (JsonNode ff : inc.path("attributes").path("fileFormats")) {
                        String url = ff.path("fileUrl").asText(null);
                        if (url != null && !url.isEmpty()) urls.add(url);
                    }
                }
            }
        }
        item.setAttachmentUrls(urls);
        return item;
    }
}
