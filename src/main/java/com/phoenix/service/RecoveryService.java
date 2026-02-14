package com.phoenix.service;

import com.phoenix.model.Job;
import com.phoenix.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryService {

    private final JobRepository jobRepository;

    /**
     * The Zombie Hunter.
     * Runs every minute to find jobs that started > 5 minutes ago but never finished.
     * This handles: Worker Crashes, Network Partitions, OOM Kills.
     */
    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    @Transactional
    public void recoverStuckJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        
        List<Job> zombies = jobRepository.findByStatusAndUpdatedAtBefore(
            Job.JobStatus.RUNNING, 
            cutoff
        );

        if (zombies.isEmpty()) {
            return;
        }

        log.warn("🧟 Found {} stuck jobs (Zombies). Recovering...", zombies.size());

        for (Job job : zombies) {
            log.info("Recovering job ID: {}. Resetting to PENDING.", job.getId());
            
            // Critical Decision: Do we count this as an attempt?
            // Yes, because it might have failed due to poison pill payload.
            job.setStatus(Job.JobStatus.PENDING);
            job.setAttemptCount(job.getAttemptCount() + 1);
            
            // If it has exceeded max retries, fail it (Dead Letter Queue logic)
            if (job.getAttemptCount() > job.getMaxRetries()) {
                log.error("Job {} exceeded max retries during recovery. Moving to FAILED.", job.getId());
                job.setStatus(Job.JobStatus.FAILED);
            }
            
            jobRepository.save(job);
        }
    }
}
