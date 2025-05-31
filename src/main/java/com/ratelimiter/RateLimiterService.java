	package com.ratelimiter;
	
	import java.time.Instant;
	import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
	
	//@Service
	public class RateLimiterService {
	
	    private final RateLimiterPojo config; 
	    private final long windowInSeconds;
	
	    private final ConcurrentHashMap<String, Window> requestMap = new ConcurrentHashMap<>();
	
	    public RateLimiterService(RateLimiterPojo config, long windowInSeconds) {
	        this.config = config;
	        this.windowInSeconds = windowInSeconds;
	    }	
	
	    public boolean allowRequest(String userId, String api) {
	        String key = userId + ":" + api;
	        Window window = requestMap.computeIfAbsent(key, k -> new Window(Instant.now().getEpochSecond(), 0));
	        long currentTime = Instant.now().getEpochSecond();
	
	        synchronized (window) {
	            if (currentTime - window.startTime >= windowInSeconds) {
	                window.startTime = currentTime;
	                window.counter = 1;
	                return true;
	            }
	
	            int allowedLimit = config.getLimitForApi(api);
	            if (window.counter < allowedLimit) {
	                window.counter++;
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	
	    static class Window {
	        long startTime;
	        int counter;
	
	        Window(long startTime, int counter) {
	            this.startTime = startTime;
	            this.counter = counter;
	        }
	    }
	}
