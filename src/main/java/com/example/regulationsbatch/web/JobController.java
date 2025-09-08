package com.example.regulationsbatch.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job commentsPdfJob;

    @PostMapping("/comments-pdf")
    public ResponseEntity<?> launchCommentsJob(@Valid @RequestBody CommentsJobRequest req) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("docketId", req.getDocketId())
                .addString("startCommentId", req.getStartCommentId(), true)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncher.run(commentsPdfJob, params);
        return ResponseEntity.ok("Job started: " + exec.getJobId());
    }
}
