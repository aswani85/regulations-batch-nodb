package com.example.regulationsbatch.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class RegGovClient {

    private final WebClient webClient;
    private final RetryTemplate retryTemplate = createRetryTemplate();

    @Value("${job.apiKey:}")
    private String apiKey;

    public RegGovClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.regulations.gov/v4").build();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate rt = new RetryTemplate();
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(2000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30000);
        rt.setBackOffPolicy(backOff);
        return rt;
    }

    public JsonNode getPaged(String endpoint, Map<String,String> params) {
        return retryTemplate.execute(ctx -> {
            WebClient.RequestHeadersUriSpec<?> req = webClient.get();
            WebClient.RequestHeadersSpec<?> spec = req.uri(uriBuilder -> {
                uriBuilder.path(endpoint);
                if (params != null) params.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            })
            .header("X-Api-Key", apiKey)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            return spec.retrieve()
                    .onStatus(status -> status.value() == 429, (ClientResponse r) -> Mono.error(new RuntimeException("Rate limited")))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMinutes(2));
        });
    }

    public JsonNode getDetail(String endpoint) {
        return retryTemplate.execute(ctx -> {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path(endpoint).build())
                    .header("X-Api-Key", apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (ClientResponse r) -> Mono.error(new RuntimeException("Rate limited")))
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofMinutes(2));
        });
    }

    public byte[] downloadBytes(String url) {
        return retryTemplate.execute(ctx -> {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block(Duration.ofMinutes(2));
        });
    }
}
