# 🎯 Leave Service — Complete File-by-File Deep Dive

> **Purpose**: Go through every file in `leave-service`, understand **what** is written and **why**.

---

## 📂 Project Structure Overview

```
leave-service/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── resources/application.yml
    └── java/com/leavemanagement/leaveservice/
        ├── LeaveServiceApplication.java          ← Entry point
        ├── config/
        │   ├── RabbitMQConfig.java                ← Message queue setup
        │   └── SwaggerConfig.java                 ← API docs + JWT auth
        ├── entity/
        │   ├── LeaveRequest.java                  ← DB table: leave applications
        │   ├── LeaveBalance.java                  ← DB table: leave quotas
        │   └── Holiday.java                       ← DB table: company holidays
        ├── dto/
        │   ├── LeaveRequestDto.java               ← API payload for leave requests
        │   ├── LeaveBalanceDto.java                ← API payload for balances
        │   └── HolidayDto.java                    ← API payload for holidays
        ├── repository/
        │   ├── LeaveRequestRepository.java        ← DB queries for leave requests
        │   ├── LeaveBalanceRepository.java         ← DB queries for balances
        │   └── HolidayRepository.java             ← DB queries for holidays
        ├── service/
        │   ├── LeaveService.java                  ← Interface (contract)
        │   └── LeaveServiceImpl.java              ← Business logic
        ├── controller/
        │   ├── LeaveController.java               ← REST endpoints for leaves
        │   └── HolidayController.java             ← REST endpoints for holidays
        ├── exception/
        │   ├── BadRequestException.java           ← 400 error
        │   ├── ResourceNotFoundException.java     ← 404 error
        │   └── GlobalExceptionHandler.java        ← Centralized error handling
        └── listener/
            └── LeaveApprovalListener.java         ← RabbitMQ consumer
```

---

## 1️⃣ [pom.xml](file:///d:/Desktop/Leave-management/leave-service/pom.xml) — The Build File

**What**: Maven project descriptor — declares dependencies, plugins, and build config.

**Key dependencies & why we need each:**

| Dependency | Why |
|---|---|
| `spring-boot-starter-web` | Makes this a REST API (embedded Tomcat, `@RestController`, etc.) |
| `spring-boot-starter-data-jpa` | ORM layer — lets us use `@Entity`, `JpaRepository` to talk to MySQL |
| `mysql-connector-j` | JDBC driver so Java can connect to MySQL (`runtime` scope — only needed at runtime, not compile) |
| `spring-cloud-starter-netflix-eureka-client` | Registers this service with Eureka so other services can discover it by name |
| `spring-cloud-starter-config` | Pulls config from Config Server instead of hardcoding in [application.yml](file:///d:/Desktop/Leave-management/leave-service/src/main/resources/application.yml) |
| `spring-cloud-starter-openfeign` | Declarative HTTP client for inter-service calls (enabled but not actively used in leaves) |
| `spring-boot-starter-validation` | Bean validation — enables `@NotBlank`, `@NotNull` annotations on DTOs |
| `spring-boot-starter-amqp` | RabbitMQ support — for async message consumption |
| `springdoc-openapi-starter-webmvc-ui` | Auto-generates Swagger UI at `/swagger-ui.html` |
| `spring-boot-starter-test` | JUnit 5 + Mockito for unit testing |

**Build plugins:**
- **JaCoCo** (`jacoco-maven-plugin`) — generates code coverage reports for SonarQube
- **Spring Boot Maven Plugin** — packages into executable JAR, excludes Lombok from final build

**`dependencyManagement`** — imports Spring Cloud BOM (`2023.0.1`) so all Cloud starters use compatible versions.

> **Viva point**: "We use a BOM (Bill of Materials) to ensure version compatibility across all Spring Cloud libraries."

---

## 2️⃣ [application.yml](file:///d:/Desktop/Leave-management/leave-service/src/main/resources/application.yml) — Configuration

```yaml
spring:
  application:
    name: leave-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

**What**: Bootstrap config — only 2 things here:
1. **Service name** (`leave-service`) — this is the identity used by Eureka and Config Server
2. **Config Server URL** — tells Spring to fetch the *real* configuration (DB creds, port, Eureka URL, RabbitMQ) from Config Server

**Why so minimal?** All actual config lives in the **Config Server's Git repo** as `leave-service.yml`. This is the **externalized configuration** pattern — change config without rebuilding the JAR.

> **Viva point**: "`optional:` prefix means the app starts even if Config Server is down, using defaults."

---

## 3️⃣ [LeaveServiceApplication.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/LeaveServiceApplication.java) — Entry Point

```java
@SpringBootApplication    // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableDiscoveryClient    // Registers with Eureka
@EnableFeignClients       // Activates Feign client support
public class LeaveServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeaveServiceApplication.class, args);
    }
}
```

**What**: The main class that boots the entire application.

**Why each annotation:**
- `@SpringBootApplication` — composite annotation that enables auto-config, component scanning, and marks this as a config class
- `@EnableDiscoveryClient` — registers with Eureka Server so API Gateway and other services can find it
- `@EnableFeignClients` — enables declarative REST clients (if needed for inter-service calls)

> **Viva point**: "Spring Boot uses convention-over-configuration. This one class bootstraps embedded Tomcat, connects to DB, registers with Eureka, and sets up RabbitMQ — all based on the dependencies in [pom.xml](file:///d:/Desktop/Leave-management/leave-service/pom.xml)."

---

## 4️⃣ Entity Layer — The Database Tables

### 4a. [LeaveRequest.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/LeaveRequest.java)

**What**: JPA entity → maps to `leave_request` table in MySQL.

**Fields & purpose:**

| Field | Type | Why |
|---|---|---|
| [id](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/Holiday.java#7-53) | Long | Primary key, auto-incremented by DB (`IDENTITY` strategy) |
| `employeeCode` | String | Links to the identity-service user (no FK, cross-service reference) |
| `leaveType` | String | SICK, CASUAL, or EARNED |
| `startDate` / `endDate` | LocalDate | Date range of the leave |
| `reason` | String | Optional justification |
| `status` | String | State machine: `SUBMITTED` → `APPROVED`/`REJECTED`/`CANCELLED` |
| `managerComments` | String | Filled by manager during approval/rejection |
| `createdOn` / `updatedOn` | LocalDateTime | Audit timestamps |

**Key annotations:**
- `@Column(nullable = false)` — DB-level NOT NULL constraint
- `@Column(updatable = false)` on `createdOn` — once set, JPA won't overwrite it
- `@PrePersist` / `@PreUpdate` — JPA lifecycle callbacks that auto-set timestamps

> **Viva point**: "We use JPA lifecycle hooks (`@PrePersist`, `@PreUpdate`) instead of manually setting timestamps — this ensures consistency."

### 4b. [LeaveBalance.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/LeaveBalance.java)

**What**: Tracks how many leaves an employee has per type.

**Fields**: `employeeCode`, `leaveType`, `allocated` (total given), `consumed` (used so far).

**Special method**: [getAvailableBalance()](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/dto/LeaveBalanceDto.java#36-39) returns `allocated - consumed`. This is a **derived/calculated field** — not stored in DB but computed on the fly.

> **Viva point**: "LeaveBalance uses a calculated getter — the available balance is always `allocated - consumed`, avoiding stale data."

### 4c. [Holiday.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/Holiday.java)

**What**: Master data for company holidays.

**Key**: `@Column(unique = true)` on [date](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/LeaveRequest.java#143-147) — ensures no duplicate holiday entries.

---

## 5️⃣ DTO Layer — API Transfer Objects

### Why DTOs?
DTOs (Data Transfer Objects) **decouple the API contract from the database schema**. If we expose entities directly:
- Internal fields (like [id](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/Holiday.java#7-53), `createdOn`) leak to the client
- A DB schema change would break every API consumer
- Validation annotations mix with JPA annotations (messy)

### 5a. [LeaveRequestDto.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/dto/LeaveRequestDto.java)

**Validation annotations:**
- `@NotBlank("Leave type is required")` — must not be null or empty string
- `@NotNull("Start date is required")` — must not be null

These are triggered by `@Valid` in the controller. If validation fails → `MethodArgumentNotValidException` → caught by [GlobalExceptionHandler](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/exception/GlobalExceptionHandler.java#13-48).

### 5b. [LeaveBalanceDto.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/dto/LeaveBalanceDto.java)

Exposes `leaveType`, `allocated`, `consumed`, and `availableBalance` — a read-only summary for the frontend.

### 5c. [HolidayDto.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/dto/HolidayDto.java)

Simple: just [date](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/entity/LeaveRequest.java#143-147) + `description`. No validation because admin manually manages these.

---

## 6️⃣ Repository Layer — Database Access

### 6a. [LeaveRequestRepository.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveRequestRepository.java)

```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeCode(String employeeCode);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeCode = :employeeCode " +
           "AND lr.status IN ('SUBMITTED', 'APPROVED') " +
           "AND ((lr.startDate BETWEEN :startDate AND :endDate) OR ...)")
    List<LeaveRequest> findOverlappingRequests(...);
}
```

**Two query types:**
1. **Derived query** ([findByEmployeeCode](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveBalanceRepository.java#12-13)) — Spring auto-generates SQL from method name
2. **Custom JPQL** ([findOverlappingRequests](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveRequestRepository.java#16-22)) — prevents double-booking by finding any existing leave (SUBMITTED or APPROVED) that overlaps with the requested dates

> **Viva point**: "The overlap query checks if the new leave's start or end falls within an existing leave's range — this prevents an employee from applying for overlapping leaves."

### 6b. [LeaveBalanceRepository.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveBalanceRepository.java)

- [findByEmployeeCode(String)](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveBalanceRepository.java#12-13) — get all balances for an employee
- [findByEmployeeCodeAndLeaveType(String, String)](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/LeaveBalanceRepository.java#13-14) — find specific balance (needed when checking/deducting)

### 6c. [HolidayRepository.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/HolidayRepository.java)

- [findByDateBetween(LocalDate, LocalDate)](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/repository/HolidayRepository.java#12-13) — fetch holidays in a date range (Spring derives the query)

> **Viva point**: "All repositories extend `JpaRepository` which gives us CRUD operations for free. We only declare custom methods when the default ones aren't enough."

---

## 7️⃣ Service Layer — Business Logic

### 7a. [LeaveService.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveService.java) (Interface)

```java
public interface LeaveService {
    List<LeaveBalanceDto> getLeaveBalances(String employeeCode);
    LeaveRequestDto applyForLeave(String employeeCode, LeaveRequestDto dto);
    List<LeaveRequestDto> getLeaveHistory(String employeeCode);
    List<LeaveRequestDto> getTeamCalendar(LocalDate startDate, LocalDate endDate);
    List<LeaveRequestDto> getPendingApprovals();
    LeaveRequestDto updateLeaveStatus(Long id, String status, String managerComments);
}
```

**Why an interface?** → **Loose coupling** + **Dependency Inversion (SOLID 'D')**. The controller depends on the *contract*, not the implementation. Makes testing easier too — you can mock the interface.

### 7b. [LeaveServiceImpl.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java) (Implementation)

**The brain of the service.** Let's break down each method:

#### [applyForLeave()](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#34-67) — Most complex method
```
1. Validate: startDate must be before endDate
2. Check overlaps: query DB for conflicting leave requests
3. Calculate days: ChronoUnit.DAYS.between(start, end) + 1
4. Check balance: find leave balance for this type, verify sufficient days
5. Create & save the LeaveRequest with status "SUBMITTED"
6. Return the saved entity as a DTO
```
- `@Transactional` — wraps everything in a DB transaction. If any step fails, everything rolls back.

#### [updateLeaveStatus()](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#92-120) — Approval/Rejection flow
```
1. Find the leave request by ID (or throw 404)
2. Validate: only SUBMITTED requests can be processed
3. If REJECTED: manager comments are mandatory
4. If APPROVED: deduct balance (consumed += daysApplied)
5. Update status & save
```
- This method is called **two ways**: directly via REST API, OR via RabbitMQ listener (async approval from admin-service)

#### [getTeamCalendar()](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#73-83) — Filters in Java
```java
return leaveRequestRepository.findAll().stream()
    .filter(lr -> "APPROVED".equals(lr.getStatus()) && ...)
    .map(this::mapToRequestDto)
    .collect(Collectors.toList());
```
- Loads all requests, filters in memory. Simple approach for a small dataset.

#### [getPendingApprovals()](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java#54-59) — Same pattern
Filters for `status == "SUBMITTED"` in memory.

#### Mapper methods ([mapToBalanceDto](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#121-129), [mapToRequestDto](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#130-141))
Private helper methods that convert Entity → DTO manually. An alternative is using MapStruct, but manual mapping is simpler for a small project.

> **Viva point**: "`@Transactional` on [applyForLeave](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#34-67) ensures that if the balance check passes but the save fails, nothing is committed — atomicity."

---

## 8️⃣ Controller Layer — REST API

### 8a. [LeaveController.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java)

| Endpoint | Method | Purpose | Auth Info |
|---|---|---|---|
| `GET /api/leave/balance/{userId}` | [getLeaveBalances](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java#25-30) | Fetch all leave balances | userId from path |
| `POST /api/leave/requests` | [createLeaveRequest](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java#31-38) | Apply for leave | Employee code from `X-Employee-Code` header |
| `GET /api/leave/history` | [getLeaveHistory](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#68-72) | View past requests | Employee code from header |
| `GET /api/leave/team-calendar` | [getTeamCalendar](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveServiceImpl.java#73-83) | View team's approved leaves | Date range as query params |
| `GET /api/leave/pending-approvals` | [getPendingApprovals](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java#54-59) | Manager: view pending requests | — |
| `PUT /api/leave/{id}/status` | [updateStatus](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/LeaveController.java#60-68) | Approve/reject a request | ID from path, status + comments as params |

**Key patterns:**
- `@RequestHeader("X-Employee-Code")` — API Gateway injects this after JWT validation. The leave-service **doesn't do auth itself** — it trusts the gateway.
- `@Valid @RequestBody` — triggers bean validation on the DTO
- `@DateTimeFormat(iso = ISO.DATE)` — tells Spring how to parse date query params (`2025-03-28`)
- `ResponseEntity.ok(...)` — returns HTTP 200 with body

### 8b. [HolidayController.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/controller/HolidayController.java)

Two simple endpoints:
- `GET /api/holidays` — list all holidays
- `POST /api/holidays` — add a holiday

**Note**: This controller directly uses the repository (no service layer) — acceptable for simple CRUD master data.

> **Viva point**: "The `X-Employee-Code` header comes from the API Gateway which extracts it from the JWT. This means the leave-service is stateless — it doesn't handle authentication."

---

## 9️⃣ Config Layer

### 9a. [RabbitMQConfig.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/config/RabbitMQConfig.java)

```java
@Configuration
public class RabbitMQConfig {
    public static final String LEAVE_QUEUE = "leave-approval-queue";

    @Bean
    public Queue leaveQueue() {
        return new Queue(LEAVE_QUEUE);  // declares queue in RabbitMQ
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();  // JSON serialization
    }
}
```

**What**: Declares the RabbitMQ queue and message format.

**Why**:
- The [Queue](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/config/RabbitMQConfig.java#14-18) bean ensures the queue exists when the service starts
- `Jackson2JsonMessageConverter` — messages are sent/received as JSON (not default Java serialization) for interoperability

> **Viva point**: "The admin-service publishes approval/rejection messages to this queue, and the leave-service consumes them asynchronously — this is the **pub/sub pattern** using RabbitMQ."

### 9b. [SwaggerConfig.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/config/SwaggerConfig.java)

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

**What**: Configures Swagger UI (at `/swagger-ui.html`) with a global "Authorize" button for JWT.

**Why**: Without this, you'd have to manually add `Authorization: Bearer <token>` to every request in Swagger. This config adds a lock icon → paste your JWT once → all requests are authorized.

---

## 🔟 Exception Layer

### 10a. [BadRequestException.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/exception/BadRequestException.java)

```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException { ... }
```
Thrown when: invalid dates, overlapping leaves, insufficient balance, missing comments on rejection.

### 10b. [ResourceNotFoundException.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/exception/ResourceNotFoundException.java)

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException { ... }
```
Thrown when: leave request ID doesn't exist, balance record not found.

### 10c. [GlobalExceptionHandler.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/exception/GlobalExceptionHandler.java)

```java
@ControllerAdvice  // intercepts exceptions from ALL controllers
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)     → 404 JSON
    @ExceptionHandler(BadRequestException.class)           → 400 JSON
    @ExceptionHandler(MethodArgumentNotValidException.class) → 400 with field-level errors
    @ExceptionHandler(Exception.class)                     → 500 catch-all
}
```

**What**: Centralized error handling — converts exceptions into clean JSON responses.

**Why**: Without this, Spring returns ugly HTML error pages. With `@ControllerAdvice`, every exception returns a structured `{"error": "message"}` JSON.

The `MethodArgumentNotValidException` handler is special — it maps each field's validation error:
```json
{ "leaveType": "Leave type is required", "startDate": "Start date is required" }
```

> **Viva point**: "`@ControllerAdvice` is a cross-cutting concern — it applies to all controllers without modifying them. This follows the **Single Responsibility Principle**."

---

## 1️⃣1️⃣ Listener Layer — Async Processing

### [LeaveApprovalListener.java](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/listener/LeaveApprovalListener.java)

```java
@Component
public class LeaveApprovalListener {
    @RabbitListener(queues = RabbitMQConfig.LEAVE_QUEUE)
    public void handleLeaveApproval(Map<String, Object> payload) {
        Long id = Long.valueOf(payload.get("id").toString());
        String status = (String) payload.get("status");
        String comments = (String) payload.get("comments");
        leaveService.updateLeaveStatus(id, status, comments);
    }
}
```

**What**: RabbitMQ consumer that listens on `leave-approval-queue`.

**Flow**:
1. Manager calls admin-service → "approve leave #5"
2. Admin-service publishes `{id: 5, status: "APPROVED", comments: "OK"}` to RabbitMQ
3. This listener picks it up and calls `leaveService.updateLeaveStatus()`
4. Balance gets deducted, status updated

**Why async?** → **Decouples** admin-service from leave-service. Admin doesn't need to know leave-service's URL or wait for it. If leave-service is temporarily down, the message stays in the queue and gets processed when it comes back up.

> **Viva point**: "This is **event-driven architecture**. The admin-service is the producer, leave-service is the consumer. RabbitMQ acts as a buffer, providing reliability and loose coupling."

---

## 1️⃣2️⃣ [Dockerfile](file:///d:/Desktop/Leave-management/leave-service/Dockerfile) — Containerization

```dockerfile
# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/leave-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**What**: Multi-stage Docker build.

**Why multi-stage?**
- Stage 1 uses full Maven + JDK image (~500MB) to compile
- Stage 2 uses slim JRE-only image (~200MB) to run
- Final image is **small & secure** — no compiler, no source code

> **Viva point**: "Multi-stage builds reduce the Docker image size by ~60% because we discard the build tools in the final image."

---

## 🧠 Request Flow Summary

```
Client → API Gateway (JWT check) → Leave Controller
                                        ↓
                                   LeaveService
                                   (validation, business rules)
                                        ↓
                                   Repository → MySQL

Admin-Service → RabbitMQ → LeaveApprovalListener → LeaveService → Repository → MySQL
```

---

## 💡 Key Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| **Layered Architecture** | Controller → Service → Repository | Separation of concerns |
| **DTO Pattern** | DTOs separate from entities | Decouple API from DB schema |
| **Repository Pattern** | `JpaRepository` | Abstract away SQL queries |
| **Interface Segregation** | [LeaveService](file:///d:/Desktop/Leave-management/leave-service/src/main/java/com/leavemanagement/leaveservice/service/LeaveService.java#9-17) interface | Loose coupling, testability |
| **Event-Driven / Pub-Sub** | RabbitMQ listener | Async, decoupled communication |
| **Externalized Config** | Config Server | Change config without rebuild |
| **Service Discovery** | Eureka client | Dynamic service location |
| **Global Exception Handling** | `@ControllerAdvice` | Consistent error responses |
