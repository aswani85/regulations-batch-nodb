package com.example.regulationsbatch.config;

import com.example.regulationsbatch.util.RegGovClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobsCommonConfig {

    @Autowired
    private RegGovClient client;

    @Value("${reggov.apiKey}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            client.setApiKey(apiKey);
        }
    }
}

