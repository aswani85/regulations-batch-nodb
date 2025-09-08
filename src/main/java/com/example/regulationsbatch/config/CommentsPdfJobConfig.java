package com.example.regulationsbatch.config;

import com.example.regulationsbatch.model.CommentRecord;
import com.example.regulationsbatch.processor.CommentsDetailProcessor;
import com.example.regulationsbatch.reader.CommentsByDocketReader;
import com.example.regulationsbatch.writer.CommentPdfWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CommentsPdfJobConfig {

    // We inject JobRepository and TransactionManager for JobBuilder/StepBuilder
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public CommentsPdfJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // --- Job bean ---
    @Bean
    public Job commentsPdfJob(JobRepository jobRepository, Step commentsPdfStep) {
        return new JobBuilder("commentsPdfJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(commentsPdfStep)
                .build();
    }

    // --- Step bean ---
    @Bean
    public Step commentsPdfStep(JobRepository jobRepository,
                                CommentsByDocketReader reader,
                                CommentsDetailProcessor processor,
                                CommentPdfWriter writer) {
        return new StepBuilder("commentsPdfStep", jobRepository)
                .<CommentRecord, CommentRecord>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
