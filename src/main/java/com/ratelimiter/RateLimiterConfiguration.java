package com.ratelimiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfiguration {

    private final RateLimiterPojo rateLimiterPojo;

    public RateLimiterConfiguration(RateLimiterPojo rateLimiterPojo) {
        this.rateLimiterPojo = rateLimiterPojo;
    }


    @Bean
    public RateLimiterService rateLimiterService(RateLimiterPojo config) {
        long windowInSeconds = config.getWindowInSeconds() > 0 ? config.getWindowInSeconds() : 15L;
        return new RateLimiterService(config, windowInSeconds);
    }
}
