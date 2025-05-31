package com.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.ratelimiter.RateLimiterService;
import com.ratelimiter.RateLimiterPojo;

import java.util.Map;

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
    void testAllowRequestWithDefaultLimit() {
        assertTrue(rateLimiterService.allowRequest("user2", "nonConfiguredApi"));
        assertTrue(rateLimiterService.allowRequest("user2", "nonConfiguredApi"));
        assertFalse(rateLimiterService.allowRequest("user2", "nonConfiguredApi")); 
    }

    @Test
    void testDifferentUsersDontAffectEachOther() {
        assertTrue(rateLimiterService.allowRequest("user1", "getQuote"));
        assertTrue(rateLimiterService.allowRequest("user2", "getQuote")); 
    }

    @Test
    void testDifferentApisDontShareLimits() {
        assertTrue(rateLimiterService.allowRequest("user1", "searchQuote"));
        assertTrue(rateLimiterService.allowRequest("user1", "searchQuote"));
        assertTrue(rateLimiterService.allowRequest("user1", "searchQuote"));
        assertFalse(rateLimiterService.allowRequest("user1", "searchQuote"));
    }

}
