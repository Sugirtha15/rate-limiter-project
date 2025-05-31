# Rate Limiter

A plug-and-play Java rate limiter library to control API request limits based on User + API combination.

## Overview

This rate limiter monitors the number of requests made by a user for each API within a configurable time window. If the request count exceeds the allowed limit, subsequent requests are blocked until the window resets.

**Features:**

- Support for different rate limits per API.
- Configurable default rate limits applied when API-specific limits are not set.
- Rate limiting based on User + API combination.
- Easy integration in any Java or Spring Boot project.
- Thread-safe and production-ready.
- Includes automated tests for correctness.

### Prerequisites

- Java 8 
- Maven (for building and testing)
- Spring Boot 


## 1.Installation
Option 1: Add Source Files

Download the source files from this repository
Add them to your project under the com.ratelimiter package
Inject and use the `RateLimiterService` in your API controller or service layer

Example:
@Autowired
private RateLimiterService rateLimiterService;

Option 2: Maven Dependency
application.properties
xml<dependency>
    <groupId>com.ratelimiter</groupId>
    <artifactId>rateLimiter</artifactId>
    <version>1.0.0</version>
</dependency>

## 2.Configuration 
The rate limiter supports:

1.defaultLimit: Number of allowed requests per window if no API-specific limit is set.
2.windowInSeconds: Duration of the time window for rate limiting.
3.apiLimits: A map of API names to their specific request limits

## Configuration in Project:
application.properties file
server.port=8080
spring.main.allow-bean-definition-overriding=true
rate-limiter.defaultLimit=5
rate-limiter.windowInSeconds=15
 API specific limits (key = API name, value = limit)
rate-limiter.apiLimits.getQuote=1
rate-limiter.apiLimits.anotherApi=10

These properties are mapped to the RateLimiterPojo class:
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterPojo {
    private int defaultLimit;
    private long windowInSeconds;
    private Map<String, Integer> apiLimits;
    // Getters, Setters, and logic...
}

## Configuration class of Rate Limiter:
If the windowInSeconds is not specified in the project configuration, a default value of 15 seconds is used.
  @Bean
    public RateLimiterService rateLimiterService(RateLimiterPojo config) {
        long windowInSeconds = config.getWindowInSeconds() > 0 ? config.getWindowInSeconds() : 15L;
        return new RateLimiterService(config, windowInSeconds);
    }

## 3.RatelimiterService(logic class):
The main logic  in RateLimiterService:

1.Tracks requests per user per API using an in-memory map (ConcurrentHashMap)
2.Resets counter every time window
3.Returns true if request is allowed, false if rate limit is exceeded


## 3.Running Tests
Use Maven to run automated tests:
mvn test

| Test Method                      | Description                                           |
| -------------------------------- | ----------------------------------------------------- |
| `testGetQuoteApiSpecificLimit()` | Verifies API-specific rate limit (`getQuote = 1`)     |
| `testWindowReset()`              | Ensures limits reset after the configured time window |
| `testNullParameters()`           | Handles null user/API inputs with proper exceptions   |
| `testEmptyStrings()`             | Handles empty or whitespace-only inputs gracefully    |
| `testConcurrentAccess()`         | Validates thread-safety with concurrent requests      |
| `testMixedApiUsage()`            | Tests combination of specific and default limits      |


## 4.Dependencies

 Dependencies used:

- Spring Boot 2.x
- Java 8+
- JUnit 5 (for unit testing)
- Maven (for build and dependency management)

## 5.how to integrate it into a Java/Spring Boot project:

Example Usage:

    @GetMapping("/quote")
    public ResponseEntity<String> getQuote(@RequestParam String userId) {

        boolean allowed = rateLimiterService.allowRequest(userId, API_NAME);

        if (!allowed) {
            return ResponseEntity.status(429).body("Too many requests - Rate limit exceeded");
        }

        String quote = quoteService.getRandomQuote();
        return ResponseEntity.ok(quote);
    }
}

   ## GET /api/quote?userId=user123
Returns a random quote. Rate limited per user.

Success (200 OK):
"Believe in yourself."

Rate Limit Exceeded (429):
"Too many requests - Rate limit exceeded"

 
