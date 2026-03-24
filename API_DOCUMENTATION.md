# Timesheet & Leave Management System - API Documentation & Guide

This document provides a comprehensive overview of the architecture, the endpoints available across the microservices, and a step-by-step guide on how to test the application flows using sample data.

---

## 🏗️ Architecture & Services Overview

The project consists of 7 Spring Boot microservices. 

1. **`config-server`**: Centralized configuration management. Connects to your GitHub config repo.
2. **`eureka-server`**: Service Registry. All other microservices register here to find each other.
3. **`api-gateway`**: The entry point for all API calls. Runs on a central port (e.g., `8080`). It houses the `JwtAuthenticationFilter` which validates tokens and forwards the `X-Employee-Code` header downstream.
4. **`identity-service`**: Handles User Registration, Login, and JWT Token generation.
5. **`timesheet-service`**: Manages logging daily work hours against projects.
6. **`leave-service`**: Manages leave applications, balance tracking, and holidays.
7. **`admin-service`**: Orchestrates approvals (via OpenFeign calls to Timesheet/Leave) and manages Master Data like Leave Policies.

### 🚦 Boot Sequence
To start the application locally, run them in this specific order:
1. `config-server` (wait until it starts completely)
2. `eureka-server` (wait until it starts completely)
3. `api-gateway`
4. `identity-service`, `timesheet-service`, `leave-service`, `admin-service` (order doesn't matter for these)

All API requests should be sent to the **API Gateway** port (e.g. `http://localhost:8080`) which will route them to the respective microservices.

---

## 🚀 Step-by-Step Testing Flow & Endpoints

> **Note:** For all requests (except `/api/auth/**`), you must attach the JWT token in your HTTP Headers: `Authorization: Bearer <YOUR_JWT_TOKEN>`

### 1. Identity & Auth Service (`identity-service`)

**Signup a new employee:**
- **URL**: `POST /api/auth/signup`
- **Payload**:
  ```json
  {
    "employeeCode": "EMP001",
    "name": "John Doe",
    "email": "john.doe@company.com",
    "password": "password123"
  }
  ```

**Login to get a JWT Token:**
- **URL**: `POST /api/auth/login`
- **Payload**:
  ```json
  {
    "usernameOrEmail": "EMP001",
    "password": "password123"
  }
  ```
- **Response**: You will receive a long `token` string. Copy this to use in all subsequent requests.

---

### 2. Admin Service (`admin-service`)

*Provides centralized configurations and approvals.*

**Create a Leave Policy:**
- **URL**: `POST /api/admin/master/policies`
- **Payload**:
  ```json
  {
    "policyCode": "ANNUAL_LEAVE_2026",
    "leaveType": "EARNED",
    "annualAllocation": 20.0,
    "carryForwardAllowed": true,
    "maxCarryForwardDays": 5
  }
  ```

**Get all Leave Policies:**
- **URL**: `GET /api/admin/master/policies`

---

### 3. Leave Service (`leave-service`)

*Before testing leave applications, ensure the employee has a balance in the `leave_balance` database table, as the service strictly checks for sufficient balance before applying. You can insert dummy data manually into MySQL for balances.*

**Create a Holiday (Master Data):**
- **URL**: `POST /api/holidays`
- **Payload**:
  ```json
  {
    "date": "2026-12-25",
    "description": "Christmas Day"
  }
  ```

**Check Leave Balance:**
- **URL**: `GET /api/leaves/balances/EMP001`

**Apply for a Leave:**
- **URL**: `POST /api/leaves`
- **Payload**:
  ```json
  {
    "leaveType": "EARNED",
    "startDate": "2026-05-10",
    "endDate": "2026-05-15",
    "reason": "Family Vacation"
  }
  ```

**Get My Leaves:**
- **URL**: `GET /api/leaves/employee/EMP001`

---

### 4. Timesheet Service (`timesheet-service`)

*Note: You must have a Project in the database to log a timesheet entry. Insert a project manually into the `project` table in MySQL (e.g., `INSERT INTO project (project_code, name, active) VALUES ('PRJ-ALPHA', 'Alpha Web App', true);`).*

**Log a Weekly Timesheet with Daily Entries:**
- **URL**: `POST /api/timesheets`
- **Payload**:
  ```json
  {
    "employeeCode": "EMP001",
    "weekStartDate": "2026-03-23",
    "entries": [
      {
        "projectCode": "PRJ-ALPHA",
        "workDate": "2026-03-23",
        "hours": 8.0,
        "taskSummary": "Developed Login API"
      },
      {
        "projectCode": "PRJ-ALPHA",
        "workDate": "2026-03-24",
        "hours": 7.5,
        "taskSummary": "Wrote unit tests for Login API"
      }
    ]
  }
  ```

**Submit a Timesheet for Approval:**
- **URL**: `PUT /api/timesheets/{timesheetId}/submit`
- **Response**: Changes status from `DRAFT` to `SUBMITTED`.

---

### 5. Centralized Approvals (Testing Feign Inter-service comms)

*The Admin service talks to the Timesheet and Leave services directly over the internal network using Feign Clients to update statuses.*

**Approve a Leave Request:**
- **URL**: `PUT /api/admin/approvals/leaves/{leaveId}`
- **Query Params**: `?status=APPROVED&comments=Enjoy your vacation!`

**Approve a Timesheet:**
- **URL**: `PUT /api/admin/approvals/timesheets/{timesheetId}`
- **Query Params**: `?status=APPROVED&comments=Good work.`

---

## 📄 Swagger Documentation UI
Thanks to the Spring Cloud Gateway and `springdoc` integration, you do **not** need to open each microservice's swagger page individually. The Swagger documentation is centrally aggregated.

1. Once the gateway and microservices are up, open your browser and navigate to:
   **`http://localhost:<GATEWAY_PORT>/webjars/swagger-ui/index.html`** (or just `http://localhost:<GATEWAY_PORT>/swagger-ui.html`).
2. In the top-right corner of the Swagger UI, you will see a **"Select a definition"** dropdown.
3. Use this dropdown to switch between the APIs for:
   - **Identity Service** (`/auth/v3/api-docs`)
   - **Admin Service** (`/admin/v3/api-docs`)
   - **Leave Service** (`/leave/v3/api-docs`)
   - **Timesheet Service** (`/timesheet/v3/api-docs`)
4. **Important**: When you select an API, the **"Servers"** dropdown below the title should display the Gateway URL (`http://localhost:<GATEWAY_PORT>`). This ensures that when you click "Try it out" and "Execute", the requests route through the Gateway instead of hitting the microservice IP directly (which would lead to CORS or connection issues). *Note: Ensure your microservices have `server.forward-headers-strategy: framework` set in their configurations so the gateway correctly forwards its host information.*

## 🗄️ Database
Since `spring.jpa.hibernate.ddl-auto=update` is used, MySQL tables are generated automatically the first time the services boot.
The expected databases you should have created in MySQL prior to startup are:
- `leavemgt_identity`
- `leavemgt_timesheet`
- `leavemgt_leave`
- `leavemgt_admin`

Ensure your Cloud Config Repo `.yml` properties point to these respective schema URLs.

Happy testing!
