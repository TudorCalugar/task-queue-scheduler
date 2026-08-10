package com.taskqueue.taskqueue.worker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyTest {

    private final RetryPolicy policy = new RetryPolicy(3, 1000);

    @Test
    void backoff_creste_exponential() {
        assertThat(policy.backoffMillis(1)).isEqualTo(1000);  // 1000 * 2^0
        assertThat(policy.backoffMillis(2)).isEqualTo(2000);  // 1000 * 2^1
        assertThat(policy.backoffMillis(3)).isEqualTo(4000);  // 1000 * 2^2
    }

    @Test
    void shouldRetry_permite_pana_la_maxRetries() {
        assertThat(policy.shouldRetry(1)).isTrue();
        assertThat(policy.shouldRetry(2)).isTrue();
        assertThat(policy.shouldRetry(3)).isTrue();
    }

    @Test
    void shouldRetry_refuza_dupa_maxRetries() {
        assertThat(policy.shouldRetry(4)).isFalse();
        assertThat(policy.shouldRetry(5)).isFalse();
    }

    @Test
    void backoff_respecta_baza_configurata() {
        RetryPolicy customPolicy = new RetryPolicy(3, 500);
        assertThat(customPolicy.backoffMillis(1)).isEqualTo(500);
        assertThat(customPolicy.backoffMillis(2)).isEqualTo(1000);
    }
}