package com.taskqueue.taskqueue.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    public enum Status {
        PENDING, RUNNING, DONE, FAILED
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private int retryCount;

    // JPA are nevoie de un constructor gol (fara argumente)
    protected Task() {
    }

    public Task(String type, String payload) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.retryCount = 0;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getRetryCount() { return retryCount; }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}