# 🔗 URL Shortener — Spring Boot Backend Project

> Complete Project Guide · Sprint Plan · Interview Prep

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📑 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [System Design](#3-system-design)
4. [Project Structure](#4-project-structure)
5. [API Design](#5-api-design)
6. [Key Code Snippets](#6-key-code-snippets)
7. [Sprint Plan (3 Weeks)](#7-sprint-plan-3-weeks)
8. [Local Setup Guide](#8-local-setup-guide)
9. [Testing Strategy](#9-testing-strategy)
10. [Interview Preparation](#10-interview-preparation)
11. [Optional Enhancements](#11-optional-enhancements)
12. [GitHub Repository Tips](#12-github-repository-tips)
13. [Final Checklist](#13-final-checklist)

---

## 1. Project Overview

A URL Shortener converts long, complex URLs into short, memorable links — just like bit.ly or TinyURL. This project is a classic backend engineering challenge that tests your understanding of REST APIs, hashing algorithms, caching, database design, and system scalability.

### What It Does

| Feature | Description |
|---|---|
| **Shorten URLs** | User submits a long URL, receives a short code (e.g., `short.ly/aB3xK9`) |
| **Redirect** | Visiting the short URL redirects the user to the original URL |
| **Analytics** | Track how many times each link was clicked and when |
| **Custom Aliases** | Let users define their o

wn short code (e.g., `short.ly/my-project`) |
| **Expiry** | Links can have an optional expiration date (returns `410 Gone` after expiry) |
| **Rate Limiting** | Prevent abuse by limiting how many URLs a user/IP can shorten per minute |

### Why This Project?

| Skill Tested | How It Appears in This Project |
|---|---|
| REST API Design | GET, POST, DELETE endpoints with proper HTTP status codes |
| Hashing / Encoding | Base62 encoding to generate short codes |
| Database Design | Relational schema with indexing strategy |
| Caching (Redis) | Cache hot links to reduce DB load by 90%+ |
| Rate Limiting | Token bucket via Redis to prevent abuse |
| Exception Handling | Global error handler with RFC 7807 Problem Details |
| Testing | JUnit 5 + Mockito unit tests + MockMvc integration tests |
| Docker | Containerize app + DB + Redis with docker-compose |
| System Design | Discuss scalability, collision handling, TTL in interviews |

---

## 2. Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | LTS, records, text blocks, sealed classes |
| Framework | Spring Boot 3.x | REST API, DI, auto-configuration |
| Security | Spring Security + JWT | Stateless auth (optional advanced feature) |
| ORM | Spring Data JPA + Hibernate | Database operations |
| Database | MySQL 8 | Persistent URL storage |
| Cache | Redis 7 | Hot-link caching + rate limiting |
| Migration | Flyway | Version-controlled DB schema changes |
| Docs | SpringDoc OpenAPI 3 (Swagger) | Auto-generated API documentation |
| Testing | JUnit 5 + Mockito + MockMvc | Unit & integration tests |
| Build Tool | Maven | Dependency management |
| Containers | Docker + Docker Compose | Local dev environment |
| Validation | Bean Validation (JSR-380) | Request payload validation |

---

## 3. System Design

### 3.1 Architecture Diagram

```
Client  →  Controller  →  Service  →  Repository  →  MySQL
                ↕                          ↕
           Redis Cache             Flyway Migrations
```

### 3.2 Short Code Generation (Base62)

When a long URL is submitted, the system generates a unique 7-character short code using Base62 encoding:

```
Alphabet : 0-9 A-Z a-z  (62 characters)
7 chars  : 62^7 = 3.5 trillion unique codes

Algorithm:
  1. Insert URL into DB → get auto-increment ID (e.g., 12345)
  2. Encode the ID to Base62  → e.g., "3D7kP"
  3. Pad to 7 chars if needed → "aB3D7kP"
  4. Store and return to client

Collision-free because each DB ID is unique.
```

### 3.3 Redirect Flow

```
GET /r/{shortCode}

1. Check Redis cache for shortCode
   ├── HIT  → return cached originalUrl  (fast path, ~1ms)
   └── MISS → query MySQL for shortCode
               ├── Found + not expired → cache in Redis (TTL 24h)
               │                       → return 302 Redirect
               ├── Found + expired     → return 410 Gone
               └── Not found           → return 404 Not Found
```

### 3.4 Database Schema

```sql
TABLE: urls
┌─────────────────┬──────────────────┬─────────────────────────────┐
│ Column          │ Type             │ Notes                       │
├─────────────────┼──────────────────┼─────────────────────────────┤
│ id              │ BIGINT PK AI     │ Auto-increment, used for B62│
│ original_url    │ TEXT NOT NULL    │ The full long URL           │
│ short_code      │ VARCHAR(10) UQ   │ 7-char Base62 code          │
│ custom_alias    │ VARCHAR(50) UQ   │ Optional user-defined alias │
│ click_count     │ BIGINT DEFAULT 0 │ Total clicks                │
│ expires_at      │ DATETIME NULL    │ Optional expiry             │
│ created_at      │ DATETIME         │ Creation timestamp          │
│ created_by_ip   │ VARCHAR(45)      │ IP address of creator       │
└─────────────────┴──────────────────┴─────────────────────────────┘

INDEX: idx_short_code ON urls(short_code)   -- fast redirect lookup
INDEX: idx_expires_at ON urls(expires_at)   -- fast expiry cleanup
```

---

## 4. Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/yourname/urlshortener/
│   │   │   ├── UrlShortenerApplication.java      # Entry point
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java            # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── UrlService.java               # Interface
│   │   │   │   └── UrlServiceImpl.java           # Business logic
│   │   │   ├── repository/
│   │   │   │   └── UrlRepository.java            # JPA repository
│   │   │   ├── model/
│   │   │   │   └── Url.java                      # JPA entity
│   │   │   ├── dto/
│   │   │   │   ├── ShortenRequest.java           # Request DTO
│   │   │   │   └── ShortenResponse.java          # Response DTO
│   │   │   ├── util/
│   │   │   │   └── Base62Encoder.java            # Encoding logic
│   │   │   ├── cache/
│   │   │   │   └── RedisCacheService.java        # Redis operations
│   │   │   ├── ratelimit/
│   │   │   │   └── RateLimiterService.java       # Token bucket
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java   # @ControllerAdvice
│   │   │       ├── UrlNotFoundException.java
│   │   │       └── UrlExpiredException.java
│   │   └── resources/
│   │       ├── application.yml                   # App configuration
│   │       ├── application-dev.yml               # Dev overrides
│   │       └── db/migration/
│   │           └── V1__create_urls_table.sql     # Flyway migration
│   └── test/
│       └── java/com/yourname/urlshortener/
│           ├── service/UrlServiceTest.java       # Unit tests
│           ├── controller/UrlControllerTest.java # Integration tests
│           └── util/Base62EncoderTest.java
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 5. API Design

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/urls` | Shorten a long URL | 201 Created, 400 Bad Request |
| `GET` | `/r/{shortCode}` | Redirect to original URL | 302 Found, 404, 410 Gone |
| `GET` | `/api/urls/{shortCode}` | Get URL metadata & stats | 200 OK, 404 Not Found |
| `DELETE` | `/api/urls/{shortCode}` | Delete a short URL | 204 No Content, 404 |
| `GET` | `/api/urls` | List all URLs (paginated) | 200 OK |
| `GET` | `/actuator/health` | Health check endpoint | 200 OK |

### 5.1 Request / Response Examples

#### POST `/api/urls` — Shorten a URL

**Request Body:**
```json
{
  "originalUrl": "https://www.example.com/very/long/path?utm=campaign&source=google",
  "customAlias": "my-link",
  "expiresInDays": 30
}
```

**Response — 201 Created:**
```json
{
  "shortCode": "aB3D7kP",
  "shortUrl": "http://localhost:8080/r/aB3D7kP",
  "originalUrl": "https://www.example.com/very/long/path?utm=campaign&source=google",
  "expiresAt": "2024-02-14T10:30:00Z",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### GET `/api/urls/{shortCode}` — Get Stats

```json
{
  "shortCode": "aB3D7kP",
  "shortUrl": "http://localhost:8080/r/aB3D7kP",
  "originalUrl": "https://www.example.com/...",
  "clickCount": 142,
  "createdAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-02-14T10:30:00Z"
}
```

#### Error Response (RFC 7807 ProblemDetail)

```json
{
  "type": "about:blank",
  "title": "URL Not Found",
  "status": 404,
  "detail": "No URL found for short code: xyz1234",
  "instance": "/api/urls/xyz1234"
}
```

---

## 6. Key Code Snippets

### 6.1 Base62 Encoder

```java
public class Base62Encoder {
    private static final String ALPHABET =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    public static String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int)(id % BASE)));
            id /= BASE;
        }
        while (sb.length() < 7) sb.append(ALPHABET.charAt(0));
        return sb.reverse().toString();
    }
}
```

### 6.2 URL Entity (JPA)

```java
@Entity
@Table(name = "urls")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Url {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "short_code", unique = true, length = 10)
    private String shortCode;

    @Column(name = "custom_alias", unique = true, length = 50)
    private String customAlias;

    @Column(name = "click_count")
    private Long clickCount = 0L;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
```

### 6.3 Shorten Request DTO

```java
@Data
public class ShortenRequest {

    @NotBlank(message = "Original URL is required")
    @org.hibernate.validator.constraints.URL(message = "Must be a valid URL")
    private String originalUrl;

    @Size(min = 3, max = 50, message = "Alias must be 3–50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "Only alphanumeric, dash, underscore allowed")
    private String customAlias;   // optional

    @Min(1) @Max(365)
    private Integer expiresInDays;  // optional
}
```

### 6.4 Service Layer

```java
@Service @RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final RedisCacheService cacheService;
    private final RateLimiterService rateLimiter;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request, String clientIp) {
        rateLimiter.checkLimit(clientIp);   // throws RateLimitExceededException if exceeded

        Url url = Url.builder()
            .originalUrl(request.getOriginalUrl())
            .customAlias(request.getCustomAlias())
            .expiresAt(resolveExpiry(request.getExpiresInDays()))
            .createdByIp(clientIp)
            .build();

        Url saved = urlRepository.save(url);
        String code = request.getCustomAlias() != null
            ? request.getCustomAlias()
            : Base62Encoder.encode(saved.getId());

        saved.setShortCode(code);
        urlRepository.save(saved);
        cacheService.cache(code, saved.getOriginalUrl());

        return toResponse(saved);
    }

    public String resolve(String shortCode) {
        String cached = cacheService.get(shortCode);
        if (cached != null) return cached;   // cache hit

        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.isExpired()) throw new UrlExpiredException(shortCode);

        urlRepository.incrementClickCount(url.getId());
        cacheService.cache(shortCode, url.getOriginalUrl());
        return url.getOriginalUrl();
    }

    private LocalDateTime resolveExpiry(Integer days) {
        return days != null ? LocalDateTime.now().plusDays(days) : null;
    }
}
```

### 6.5 Controller

```java
@RestController @RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/urls")
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        ShortenResponse response = urlService.shorten(request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolve(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(originalUrl))
            .build();
    }

    @GetMapping("/api/urls/{shortCode}")
    public ResponseEntity<ShortenResponse> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getStats(shortCode));
    }

    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        urlService.delete(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/urls")
    public ResponseEntity<Page<ShortenResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(urlService.list(pageable));
    }
}
```

### 6.6 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(UrlNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("URL Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ProblemDetail> handleExpired(UrlExpiredException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.GONE, ex.getMessage());
        pd.setTitle("URL Expired");
        return ResponseEntity.status(HttpStatus.GONE).body(pd);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimit(RateLimitExceededException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Try again later.");
        pd.setTitle("Rate Limit Exceeded");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Validation Failed");
        return ResponseEntity.badRequest().body(pd);
    }
}
```

### 6.7 Flyway Migration

```sql
-- V1__create_urls_table.sql
CREATE TABLE urls (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    original_url  TEXT         NOT NULL,
    short_code    VARCHAR(10)  UNIQUE,
    custom_alias  VARCHAR(50)  UNIQUE,
    click_count   BIGINT       NOT NULL DEFAULT 0,
    expires_at    DATETIME     NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_ip VARCHAR(45),
    PRIMARY KEY (id)
);

CREATE INDEX idx_short_code ON urls (short_code);
CREATE INDEX idx_expires_at ON urls (expires_at);
```

---

## 7. Sprint Plan (3 Weeks)

> **Commit daily. Push to GitHub with meaningful messages.** Each sprint is 1 week.

### ✅ Sprint 1 — Foundation (Week 1)

**Goal:** Working shorten + redirect endpoints with MySQL persistence  
**Done When:** You can POST a URL and GET redirected from the short link

| Day | Task | Deliverable |
|---|---|---|
| Day 1 | Set up Spring Boot project via start.spring.io | Project compiles and runs |
| Day 1 | Add dependencies: JPA, MySQL, Validation, Lombok | `pom.xml` configured |
| Day 2 | Create `Url` entity + `UrlRepository` | Entity mapped to DB table |
| Day 2 | Write Flyway migration `V1__create_urls_table.sql` | Table auto-created on startup |
| Day 3 | Implement `Base62Encoder` utility class | Unit test passes for `encode()` |
| Day 3 | Implement `UrlService` — `shorten()` method | Service logic tested |
| Day 4 | Implement `UrlController` — `POST /api/urls` | Postman: 201 response works |
| Day 4 | Implement `GET /r/{shortCode}` redirect | Browser redirect works |
| Day 5 | Add Bean Validation (`@NotBlank`, `@URL` on DTO) | 400 returned for invalid input |
| Day 5 | Add `GlobalExceptionHandler` with `ProblemDetail` | Errors return structured JSON |

---

### ✅ Sprint 2 — Redis, Rate Limiting & Stats (Week 2)

**Goal:** Add caching, rate limiting, click analytics, and expiry  
**Done When:** Redis caches redirects; exceeded rate limit returns 429; expired links return 410

| Day | Task | Deliverable |
|---|---|---|
| Day 1 | Add Redis to docker-compose + Spring config | Redis connects on startup |
| Day 1 | Implement `RedisCacheService` (get, cache, evict) | Cache operations work |
| Day 2 | Integrate cache into redirect flow (cache-aside) | Second redirect is faster |
| Day 2 | Add `click_count` increment on redirect | Counter updates in DB |
| Day 3 | Implement `RateLimiterService` using Redis counters | Too many requests → 429 |
| Day 3 | Add IP extraction from `HttpServletRequest` | Rate limited per IP |
| Day 4 | Add `expires_at` support in shorten + redirect | Expired links → 410 |
| Day 4 | Add `GET /api/urls/{shortCode}` stats endpoint | Returns metadata + count |
| Day 5 | Add `DELETE /api/urls/{shortCode}` | 204 + cache eviction |
| Day 5 | Add `GET /api/urls` with `Pageable` | Paginated list works |

---

### ✅ Sprint 3 — Tests, Docs & Docker (Week 3)

**Goal:** Production-ready project with tests, Swagger docs, and Docker  
**Done When:** `mvn test` passes; `docker-compose up` works end to end; Swagger UI is accessible

| Day | Task | Deliverable |
|---|---|---|
| Day 1 | Write unit tests for `Base62Encoder` | Edge cases: 0, large IDs |
| Day 1 | Write unit tests for `UrlService` with Mockito | Service mocked and tested |
| Day 2 | Write integration tests for `UrlController` with MockMvc | All endpoints tested |
| Day 2 | Write tests for rate limiting and expiry edge cases | ≥ 80% code coverage |
| Day 3 | Add SpringDoc OpenAPI — annotate DTOs and Controller | Swagger UI at `/swagger-ui` |
| Day 3 | Write `Dockerfile` (multi-stage build) | `docker build` works |
| Day 4 | Write `docker-compose.yml` (app + MySQL + Redis) | `docker-compose up` works |
| Day 4 | Configure `application-prod.yml` with env variables | No hardcoded secrets |
| Day 5 | Write README.md on GitHub | Professional project page |
| Day 5 | Final review + cleanup + push to GitHub | Ready to show recruiters |

---

## 8. Local Setup Guide

### 8.1 Prerequisites

- Java 17+ (JDK)
- Maven 3.8+
- Docker & Docker Compose
- Postman (for API testing)

### 8.2 Clone & Run

```bash
# 1. Clone the repo
git clone https://github.com/yourusername/url-shortener.git
cd url-shortener

# 2. Start MySQL + Redis with Docker
docker-compose up -d mysql redis

# 3. Run the Spring Boot app
mvn spring-boot:run

# OR run everything including the app
docker-compose up --build

# 4. Access Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

### 8.3 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/urlshortener
    username: ${DB_USER:root}
    password: ${DB_PASS:password}
  jpa:
    hibernate:
      ddl-auto: validate        # Flyway manages schema
    show-sql: false
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  base-url: ${BASE_URL:http://localhost:8080}
  rate-limit:
    max-requests: 10
    window-seconds: 60
  cache:
    url-ttl-hours: 24
```

### 8.4 docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: urlshortener
    ports:
      - '3306:3306'
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - '6379:6379'

  app:
    build: .
    ports:
      - '8080:8080'
    environment:
      DB_USER: root
      DB_PASS: password
      REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

### 8.5 Dockerfile

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 9. Testing Strategy

| Test Type | Tool | What to Test |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Base62Encoder, UrlService, RateLimiterService with mocked dependencies |
| Integration Tests | MockMvc + H2 | Controller endpoints — full request/response cycle |
| Cache Tests | Testcontainers Redis | Cache hit/miss behavior |
| Edge Cases | JUnit 5 | Duplicate alias, expired URL, invalid URL format, rate limit boundary |

### 9.1 Sample Unit Test

```java
@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock private UrlRepository urlRepository;
    @Mock private RedisCacheService cacheService;
    @Mock private RateLimiterService rateLimiter;
    @InjectMocks private UrlServiceImpl urlService;

    @Test
    void shorten_validUrl_returnsShortCode() {
        ShortenRequest req = new ShortenRequest();
        req.setOriginalUrl("https://example.com/very/long/url");
        Url saved = Url.builder().id(1L).originalUrl(req.getOriginalUrl()).build();
        when(urlRepository.save(any())).thenReturn(saved);

        ShortenResponse response = urlService.shorten(req, "127.0.0.1");

        assertNotNull(response.getShortCode());
        assertEquals(7, response.getShortCode().length());
        verify(cacheService).cache(anyString(), anyString());
    }

    @Test
    void resolve_expiredUrl_throwsUrlExpiredException() {
        Url url = Url.builder()
            .shortCode("abc123")
            .expiresAt(LocalDateTime.now().minusDays(1))  // already expired
            .build();
        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

        assertThrows(UrlExpiredException.class, () -> urlService.resolve("abc123"));
    }

    @Test
    void resolve_cachedUrl_doesNotHitDatabase() {
        when(cacheService.get("abc123")).thenReturn("https://example.com");

        String result = urlService.resolve("abc123");

        assertEquals("https://example.com", result);
        verifyNoInteractions(urlRepository);  // DB never called
    }
}
```

### 9.2 Sample Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void postUrl_validRequest_returns201() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://example.com");

        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").isNotEmpty())
            .andExpect(jsonPath("$.shortUrl").isNotEmpty());
    }

    @Test
    void postUrl_invalidUrl_returns400() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("not-a-url");

        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Failed"));
    }
}
```

---

## 10. Interview Preparation

### 10.1 System Design Questions

| Question | Key Points to Cover |
|---|---|
| How do you generate short codes? | Base62 encoding of auto-increment DB ID. Collision-free. 62^7 = 3.5 trillion unique codes. |
| Why Base62 and not MD5/SHA? | Hash functions can collide; Base62 on a unique ID guarantees uniqueness. URL-safe characters. |
| How does Redis help here? | Cache-aside pattern: check Redis first, fallback to DB. Reduces DB load, sub-millisecond redirects. |
| How do you scale to millions of URLs? | Horizontal scaling, DB read replicas, consistent hashing for Redis cluster, CDN for redirect traffic. |
| How do you handle custom alias conflicts? | Unique constraint in DB + catch `DataIntegrityViolationException`, return 409 Conflict. |
| What happens when Redis goes down? | Application falls back to DB. Use Redis Sentinel or Cluster for HA in production. |
| How does rate limiting work? | Redis `INCR` + `EXPIRE`: count requests per IP per window. Token bucket algorithm. |
| How would you handle 10,000 redirects/sec? | Load balancer → multiple app instances → Redis cluster → MySQL read replicas + connection pool. |

### 10.2 Java / Spring Questions

| Question | Answer |
|---|---|
| What is `@Transactional`? | Wraps method in a DB transaction. Rolls back on `RuntimeException`. Used in `shorten()` for atomicity. |
| `@Controller` vs `@RestController`? | `@RestController` = `@Controller` + `@ResponseBody`. Serializes return values to JSON automatically. |
| What is Spring Data JPA? | Abstraction over JPA/Hibernate. Repository interfaces auto-implement CRUD. No boilerplate SQL. |
| What is Flyway? | DB migration tool. Version-controlled SQL scripts run in order. Prevents schema drift. |
| What is Bean Validation? | JSR-380 annotations (`@NotNull`, `@URL`) on DTOs. `@Valid` in controller triggers validation. |
| Explain `ProblemDetail` | RFC 7807 standard error response. Spring 6 built-in. Returns type, title, status, detail fields. |
| What is cache-aside pattern? | App checks cache first → on miss, reads DB, writes to cache. Cache never gets stale on delete. |

### 10.3 How to Present in Interviews

> **"Tell me about a project you built."**

*"I built a URL shortener using Spring Boot 3, MySQL, and Redis. The core challenge was generating unique short codes — I solved it with Base62 encoding of the auto-increment database ID, giving 3.5 trillion possible codes with zero collision risk. I added Redis as a cache layer using the cache-aside pattern, which reduces database load by roughly 90% for popular links. I implemented rate limiting using Redis atomic counters — 10 requests per IP per 60 seconds. The project is fully dockerized and has over 80% test coverage with JUnit 5 and Mockito."*

---

## 11. Optional Enhancements

Add these after completing the core project to impress senior engineers:

| Enhancement | Why It Impresses | Difficulty |
|---|---|---|
| JWT Authentication | Users own their links; auth is table-stakes knowledge | Medium |
| QR Code Generation | Visual feature; shows ZXing library integration | Easy |
| Click Analytics | Track city, browser, device per click (MaxMind/UA parser) | Medium |
| Custom Domain Support | Multiple base domains (e.g., `go.mycompany.com`) | Hard |
| Scheduled Cleanup Job | `@Scheduled` task to delete expired links nightly | Easy |
| Async Click Tracking | `@Async` on click count — don't slow the redirect | Easy |
| Testcontainers | Use real MySQL + Redis in tests instead of H2/mocks | Medium |
| GitHub Actions CI/CD | Auto-run tests on every push — shows DevOps awareness | Easy |
| Micrometer + Prometheus | Expose `/actuator/metrics` for redirect latency | Medium |

---

## 12. GitHub Repository Tips

### Repository Setup
- **Name:** `url-shortener-springboot` (specific, searchable)
- **Description:** `REST API URL shortener with Spring Boot, Redis caching, rate limiting & Docker`
- **Topics:** `java` `spring-boot` `redis` `mysql` `rest-api` `docker` `backend`
- **License:** MIT

### Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add Base62 encoder for short code generation
feat: implement cache-aside pattern with Redis
fix: handle duplicate custom alias with 409 Conflict
test: add unit tests for UrlService with Mockito
docs: add API examples to README
chore: add Dockerfile and docker-compose
refactor: extract rate limit logic to dedicated service
perf: add async click count increment
```

### Badges to Add

```markdown
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Build](https://github.com/you/repo/actions/workflows/ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-85%25-success)
```

---

## 13. Final Checklist

Go through this before adding the project to your resume or sharing with a recruiter:

- [ ] All 6 API endpoints work end-to-end
- [ ] Redis caching is functional (verify with logs)
- [ ] Rate limiting returns `429` after threshold
- [ ] Expired URLs return `410 Gone`
- [ ] Unit tests pass (`mvn test`)
- [ ] Integration tests pass
- [ ] Swagger UI accessible at `/swagger-ui/index.html`
- [ ] `docker-compose up` brings everything up cleanly
- [ ] No hardcoded passwords (use environment variables)
- [ ] GitHub repo has README with setup steps
- [ ] Commit history is clean and meaningful
- [ ] You can explain every piece of code in an interview

---

> **Good luck with your placements!**  
> *Build it. Break it. Understand it. Then explain it confidently.*
