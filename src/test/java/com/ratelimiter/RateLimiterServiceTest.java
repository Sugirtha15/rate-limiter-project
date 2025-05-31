package com.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        RateLimiterPojo config = new RateLimiterPojo();
        config.setDefaultLimit(2);
        config.setWindowInSeconds(10L);
        config.setApiLimits(Map.of(
                "getQuote", 1,
                "searchQuote", 3
        ));

        rateLimiterService = new RateLimiterService(config, config.getWindowInSeconds());
    }

    @Test
    void testGetQuoteApiSpecificLimit() {
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest("user1", "getQuote")));

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.allowRequest("user1", "getQuote");
        });
    }

    @Test
    void testWindowReset() throws InterruptedException {
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest("user1", "getQuote")));

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.allowRequest("user1", "getQuote");
        });

        // Wait for window to expire (11 seconds)
        Thread.sleep(11000);

        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest("user1", "getQuote")));
    }

    @Test
    void testNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> 
            rateLimiterService.allowRequest(null, "getQuote"));

        assertThrows(IllegalArgumentException.class, () -> 
            rateLimiterService.allowRequest("user1", null));
    }

    @Test
    void testEmptyStrings() {
        assertThrows(IllegalArgumentException.class, () -> 
            rateLimiterService.allowRequest("", "getQuote"));

        assertThrows(IllegalArgumentException.class, () -> 
            rateLimiterService.allowRequest("user1", ""));

        assertThrows(IllegalArgumentException.class, () -> 
            rateLimiterService.allowRequest("   ", "getQuote")); // whitespace only
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    if (rateLimiterService.allowRequest("user1", "getQuote")) {
                        successCount.incrementAndGet();
                    }
                } catch (RateLimitExceededException e) {
                    // Expected when limit exceeded
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // Only 1 request should succeed for getQuote (limit = 1)
        assertEquals(1, successCount.get());
    }

    @Test
    void testMixedApiUsage() {
        String userId = "testUser";

        // Use getQuote (limit 1)
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "getQuote")));
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.allowRequest(userId, "getQuote");
        });

        // Use searchQuote (limit 3)
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "searchQuote")));
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "searchQuote")));
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "searchQuote")));
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.allowRequest(userId, "searchQuote");
        });

        // Use default API limit 2
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "otherApi")));
        assertDoesNotThrow(() -> assertTrue(rateLimiterService.allowRequest(userId, "otherApi")));
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.allowRequest(userId, "otherApi");
        });
    }
}
