package com.phoenix.repository;

import com.phoenix.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    // Find "Zombie Jobs": Running for too long
    List<Job> findByStatusAndUpdatedAtBefore(Job.JobStatus status, LocalDateTime cutoff);
}
