# 🏢 Leave & Timesheet Management System

A robust, enterprise-grade Spring Boot microservices platform designed to orchestrate employee timesheet logging, leave applications, manager approvals, and identity management seamlessly.

---

## 🏗️ Architecture & Component Services

This platform is divided into **7 specialized microservices**, which interact via high-speed asynchronous Event Streaming (RabbitMQ) and synchronous OpenFeign clients, all routed through a resilient, fault-tolerant central Gateway.

| Service Name | Port | Database Name | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **`config-server`** | `8888` | *None* | Centralized configuration hub. Pulls `.yml` properties from your GitHub config repository and serves them to all other services at boot. |
| **`eureka-server`** | `8761` | *None* | Netflix Eureka Registry. All services register here with their dynamic IPs to allow internal discovery and load balancing. |
| **`api-gateway`** | `8080` | *None* | The single entry point for all API traffic. Routes traffic to specific microservices based on paths (e.g., `/auth/**`). Secures endpoints using a global `JwtAuthenticationFilter`. |
| **`identity-service`** | `8081` | `auth_db` | Handles User Registration, Authentication (Login), and JWT Token generation. |
| **`timesheet-service`** | `8082` | `timesheet_db` | Captures daily work hours logged against projects and compiles them into weekly timesheets. |
| **`leave-service`** | `8083` | `leave_db` | Manages holiday schedules, tracks employee leave balances, and processes new leave application requests. |
| **`admin-service`** | `8084` | `admin_db` | The orchestrator for approving requests and managing master data (Leave Policies). It publishes asynchronous Events to RabbitMQ to update Timesheet and Leave services seamlessly. |

---

## 🚀 Setup & Getting Started

### 1. Database Setup (Fully Automated)
You **DO NOT** need to manually create any databases or tables! 
Ensure you have a MySQL server running on `localhost:3306` with the credentials configured in your GitHub config `yml` files (username: `root`, password: `Mahadev12@a`). 

Because the JDBC URLs contain `createDatabaseIfNotExist=true` and Hibernate uses `ddl-auto: update`, **Spring Boot will automatically create the `auth_db`, `timesheet_db`, `leave_db`, and `admin_db` databases and all required tables the first time the services boot!**

### 2. The Boot Sequence (Critical)
Because microservices depend on centralized config and discovery, **you must start the services in this exact order**:

1. **`config-server`** -> Wait for it to fully initialize on port `8888`.
2. **`eureka-server`** -> Wait for it to map on port `8761`. (Check `http://localhost:8761` in your browser).
3. **`api-gateway`** -> Wait for it to bind to port `8080`.
4. **`identity-service`, `timesheet-service`, `leave-service`, `admin-service`** -> You can start these concurrently. Ensure they successfully register with Eureka.

### 3. API Documentation (Swagger)
Once everything is connected, you can browse all APIs comprehensively via the centralized Gateway Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

*(Use the "Select a definition" dropdown in the top right to switch between the APIs for Identity, Leave, Admin, and Timesheet).*

---

## 🛣️ System Use Cases & Core Data Flow

To successfully test the system end-to-end, you must understand the data dependencies. Below is the primary user journey and how the microservices interlink:

### Stage 1: Initial Master Data Setup
*Before an employee can do anything, specific data must exist.*
1. **Create a Project:** An employee cannot log hours without a project. 
   - *Action:* Manually insert a dummy project into the `timesheet_db.project` table: `INSERT INTO project (project_code, name, active, created_on) VALUES ('PRJ-ALPHA', 'Alpha Web App', 1, NOW());`
2. **Create a Leave Policy:** The system needs rules to calculate leave.
   - *Action:* Send a `POST` request to `admin-service` (`/admin/master/policies`) to create a policy (e.g., Earned Leave).

### Stage 2: Employee Onboarding & Authentication
1. **Signup:** 
   - *Action:* Hit `POST /api/auth/signup` (Routed to `identity-service`). Provide an `employeeCode` (e.g., `EMP001`), name, email, and password.
2. **Login:** 
   - *Action:* Hit `POST /api/auth/login`. You will receive a long `token` string. 
   - *Required:* For all following requests, you must include this in the HTTP headers: `Authorization: Bearer <token>`.
3. **Provide Leave Balance:**
   - *Action:* Before applying for leave, `EMP001` needs a balance. Manually insert this into `leave_db.leave_balance`: `INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed) VALUES ('EMP001', 'EARNED', 20.0, 0.0);`

### Stage 3: The Daily Work Flow
1. **Logging Hours:**
   - *Action:* Employee makes a `POST /api/timesheets` request (Routed to `timesheet-service`), passing daily hours mapped to `PRJ-ALPHA`. 
   - The timesheet is saved as a `DRAFT`.
2. **Submitting for Approval:**
   - *Action:* Employee calls `PUT /api/timesheets/{id}/submit`. Status changes to `SUBMITTED`.

### Stage 4: Taking Time Off
1. **Applying for Leave:**
   - *Action:* Employee makes a `POST /api/leaves` request (Routed to `leave-service`). 
   - *Internal Logic:* The `leave-service` internally queries its DB to ensure `EMP001` has enough balance (from the dummy data inserted earlier). If yes, the leave is created as `PENDING`.

### Stage 5: Managerial Approvals (Microservice Interlinking)
*The `admin-service` does not have timesheet or leave data in its own database. It orchestrates changes by communicating with the other services.*
1. **Approve Timesheet/Leave:**
   - *Action:* The manager hits the `admin-service` endpoints:
     - `PUT /api/admin/approvals/timesheets/{id}?status=APPROVED`
     - `PUT /api/admin/approvals/leaves/{id}?status=APPROVED`
   - *Internal Logic:* `admin-service` implements an **Asynchronous Event-Driven Architecture**. Instead of waiting for slower REST API calls, the Admin Service instantly publishes a JSON event payload to a **RabbitMQ Exchange**. 
   - The `timesheet-service` and `leave-service` act as independent **Consumers**, silently catching these messages from their queues and seamlessly updating the underlying databases in the background. If a service is down, the message waits safely in the queue until the service reboots!
   - Every read-call or API edge route is guarded by **Resilience4j Circuit Breakers** which provide instant clean JSON fallbacks to users instead of crashing servers during outages.
2. **Balance Deduction:**
   - Once the Leave is approved, the `leave-service` permanently deducts the days from `EMP001`'s balance.

---
**Tech Stack:** Java 17+, Spring Boot 3.x, Spring Cloud Gateway/Config/Eureka, OpenFeign, RabbitMQ (AMQP), Resilience4j Circuit Breakers, JWT Security, MySQL, Swagger OpenAPI.

---

## Observability With Loki and Grafana

The Docker Compose stack now includes a lightweight log observability pipeline:

| Component | Port | Purpose |
| :--- | :--- | :--- |
| **`loki`** | `3100` | Central log store |
| **`grafana`** | `3000` | Log exploration UI |
| **`promtail`** | `9080` | Ships Spring Boot log files into Loki |

### How It Works

- Each Spring Boot container now writes logs to `/logs/app.log`.
- Those log files are mounted into `./observability/logs/<service-name>/app.log` on the host.
- `promtail` tails those files and adds a `service` label based on the folder name.
- Grafana is pre-provisioned with Loki as the default datasource.

### Start the Full Stack

Run:

```bash
docker compose up --build
```

### Open the UIs

- Grafana: [http://localhost:3000](http://localhost:3000)
- Loki API: [http://localhost:3100/ready](http://localhost:3100/ready)
- Swagger Gateway: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Default Grafana login:

- Username: `admin`
- Password: `admin`

You can override these with:

- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`

### View Logs in Grafana

After logging in to Grafana:

1. Open **Explore**.
2. Select the **Loki** datasource.
3. Run a query such as:

```logql
{job="leave-management"}
```

Or filter a single service:

```logql
{job="leave-management", service="identity-service"}
```
