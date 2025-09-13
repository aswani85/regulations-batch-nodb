package com.example.regulationsbatch.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Component
public class SharePointUploader {

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String siteId;
    private final String driveId;
    private final String baseFolder;
    private final WebClient webClient;

    public SharePointUploader(WebClient.Builder builder,
                              @Value("${sharepoint.tenantId}") String tenantId,
                              @Value("${sharepoint.clientId}") String clientId,
                              @Value("${sharepoint.clientSecret}") String clientSecret,
                              @Value("${sharepoint.siteId}") String siteId,
                              @Value("${sharepoint.driveId}") String driveId,
                              @Value("${sharepoint.baseFolder:dockets}") String baseFolder) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.siteId = siteId;
        this.driveId = driveId;
        this.baseFolder = baseFolder;
        this.webClient = builder.baseUrl("https://graph.microsoft.com/v1.0").build();
    }

    private String acquireToken() throws MalformedURLException {
        ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                clientId,
                ClientCredentialFactory.createFromSecret(clientSecret))
                .authority("https://login.microsoftonline.com/" + tenantId)
                .build();
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(
                com.microsoft.aad.msal4j.ClientCredentialParameters.builder(Collections.singleton("https://graph.microsoft.com/.default")).build());
        IAuthenticationResult result = future.join();
        return result.accessToken();
    }

    public void uploadBytes(String folderPath, String fileName, byte[] content) throws Exception {
        String token = acquireToken();
        String normalizedFolder = baseFolder + (folderPath == null || folderPath.isBlank() ? "" : ("/" + folderPath));
        String filePath = normalizedFolder + "/" + fileName;

        webClient.put()
                .uri("/sites/{siteId}/drives/{driveId}/root:/{filePath}:/content", siteId, driveId, filePath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(content)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public String normalizeFileName(String original) {
        return FilenameUtils.getName(original);
    }
}
