package com.taskqueue.taskqueue.model;

import java.time.Instant;
import java.util.UUID;

public class Task {

    public enum Status {
        PENDING, RUNNING, DONE, FAILED
    }

    private final String id;
    private final String type;
    private final String payload;
    private Status status;
    private final Instant createdAt;
    private Instant updatedAt;
    private int retryCount;

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