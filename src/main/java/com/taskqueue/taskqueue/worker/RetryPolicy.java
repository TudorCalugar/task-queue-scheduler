package com.taskqueue.taskqueue.worker;

public class RetryPolicy {

    private final int maxRetries;
    private final long baseBackoffMs;

    public RetryPolicy(int maxRetries, long baseBackoffMs) {
        this.maxRetries = maxRetries;
        this.baseBackoffMs = baseBackoffMs;
    }

    // mai are voie sa reincerce? (retryCount = cate esecuri a avut deja)
    public boolean shouldRetry(int retryCount) {
        return retryCount <= maxRetries;
    }

    // cat sa astepte inainte de reincercarea curenta (backoff exponential)
    // retryCount=1 -> base*2^0, retryCount=2 -> base*2^1, etc.
    public long backoffMillis(int retryCount) {
        return baseBackoffMs * (1L << (retryCount - 1));
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}