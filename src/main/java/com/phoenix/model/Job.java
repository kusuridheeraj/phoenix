package com.phoenix.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_next_retry", columnList = "nextRetryAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Idempotency: If client sends this again, we ignore it.
    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String targetUrl;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status; // CREATED, PENDING, RUNNING, COMPLETED, FAILED, RETRY_PENDING

    private int attemptCount;

    private int maxRetries;

    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum JobStatus {
        CREATED,
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        RETRY_PENDING
    }
}
