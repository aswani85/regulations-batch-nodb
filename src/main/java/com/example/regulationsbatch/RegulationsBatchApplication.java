package com.example.regulationsbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RegulationsBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegulationsBatchApplication.class, args);
    }
}
