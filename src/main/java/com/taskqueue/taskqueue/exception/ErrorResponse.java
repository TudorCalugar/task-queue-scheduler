package com.taskqueue.taskqueue.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        Map<String, String> details
) {
    public ErrorResponse(int status, String error, Map<String, String> details) {
        this(Instant.now(), status, error, details);
    }
}