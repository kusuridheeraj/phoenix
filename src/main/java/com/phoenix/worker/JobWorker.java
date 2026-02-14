package com.phoenix.worker;

import com.phoenix.model.Job;
import com.phoenix.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobWorker {

    private final JobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;
    
    // Simulate processing time
    private static final long PROCESS_TIME_MS = 1000;

    /**
     * The Main Loop.
     * In a real app, this would pop from Redis Queue (BLPOP).
     * For MVP, we poll DB for PENDING jobs and compete for them.
     */
    @Scheduled(fixedDelay = 1000)
    public void processJobs() {
        // 1. Find PENDING jobs (LIMIT 10 to avoid OOM)
        // Note: In prod, we'd use Redis List POP here. 
        // We are simulating the "Competition" aspect via Locking.
        
        // Mock finding jobs (Repository method needs to be added or we use existing)
        // For simplicity, let's assume we pull a list of IDs from Redis in a real flow.
        // Here we just scan DB for demo purposes.
        List<Job> pendingJobs = jobRepository.findByStatusAndUpdatedAtBefore(
            Job.JobStatus.PENDING, 
            java.time.LocalDateTime.now()
        );

        for (Job job : pendingJobs) {
            attemptJob(job);
        }
    }

    private void attemptJob(Job job) {
        String lockKey = "job:lock:" + job.getId();
        String workerId = java.util.UUID.randomUUID().toString();

        // 2. ACQUIRE LOCK (The "Competition")
        // SET job:lock:123 worker-uuid NX EX 30
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
            lockKey, 
            workerId, 
            Duration.ofSeconds(30)
        );

        if (Boolean.TRUE.equals(acquired)) {
            try {
                log.info("🔒 Worker {} acquired lock for Job {}", workerId, job.getId());
                executeJob(job);
            } finally {
                // Release lock? 
                // In this pattern, we might keep it until done or let it expire.
                // Better to delete if done.
                redisTemplate.delete(lockKey);
            }
        } else {
            // Lost the race. Another worker is handling it.
            // log.debug("Worker {} lost race for Job {}", workerId, job.getId());
        }
    }

    private void executeJob(Job job) {
        try {
            // Update status to RUNNING
            job.setStatus(Job.JobStatus.RUNNING);
            jobRepository.save(job);

            // Execute (HTTP Request)
            log.info("🚀 Executing Webhook to: {}", job.getTargetUrl());
            Thread.sleep(PROCESS_TIME_MS); // Simulate network

            // Success
            job.setStatus(Job.JobStatus.COMPLETED);
            jobRepository.save(job);
            log.info("✅ Job {} completed.", job.getId());

        } catch (Exception e) {
            log.error("❌ Job {} failed: {}", job.getId(), e.getMessage());
            // Retry logic would go here (update status to RETRY_PENDING)
            job.setStatus(Job.JobStatus.FAILED);
            jobRepository.save(job);
        }
    }
}
