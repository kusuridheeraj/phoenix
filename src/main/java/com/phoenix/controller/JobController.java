package com.phoenix.controller;

import com.phoenix.model.Job;
import com.phoenix.repository.JobRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<Job> submitJob(@RequestBody JobRequest request) {
        log.info("Received job submission: {}", request);

        // 1. Idempotency Check
        Optional<Job> existing = jobRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotency hit for key: {}", request.getIdempotencyKey());
            return ResponseEntity.ok(existing.get());
        }

        // 2. Create Job (Failure-First: DB Persist BEFORE Queue)
        Job job = Job.builder()
                .targetUrl(request.getTargetUrl())
                .payload(request.getPayload())
                .idempotencyKey(request.getIdempotencyKey())
                .maxRetries(5)
                .attemptCount(0)
                .status(Job.JobStatus.CREATED) // Not queued yet
                .build();

        Job savedJob = jobRepository.save(job);

        // TODO: Push to Redis Queue

        return ResponseEntity.created(URI.create("/api/v1/jobs/" + savedJob.getId()))
                .body(savedJob);
    }

    @Data
    public static class JobRequest {
        private String targetUrl;
        private String payload;
        private String idempotencyKey;
    }
}
