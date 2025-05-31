package com.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterPojo {

    private int defaultLimit;
    private long windowInSeconds;
    private Map<String, Integer> apiLimits;

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public long getWindowInSeconds() {
        return windowInSeconds;
    }

    public void setWindowInSeconds(long windowInSeconds) {
        this.windowInSeconds = windowInSeconds;
    }

    public Map<String, Integer> getApiLimits() {
        return apiLimits;
    }

    public void setApiLimits(Map<String, Integer> apiLimits) {
        this.apiLimits = apiLimits;
    }
    public int getLimitForApi(String api) {
        if (apiLimits != null && apiLimits.containsKey(api)) {
            return apiLimits.get(api);
        }
        return defaultLimit;
    }

}
