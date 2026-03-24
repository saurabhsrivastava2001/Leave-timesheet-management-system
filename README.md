# Leave & Timesheet Management System

Welcome to the **Leave & Timesheet Management System**, a scalable Spring Boot microservices architecture designed to handle employee timesheets, leave balancing, authentication, and core master data management.

## 🏗️ Architecture & Component Services

The platform consists of **7 distinct Spring Boot Microservices** working together in a robust ecosystem:

1. **`config-server` (Port 8888)**: The centralized configuration repository. It pulls configuration `.yml` properties from a remote GitHub repository (`leave-management-config`), supplying them dynamically to all other microservices at boot time.
2. **`eureka-server` (Port 8761)**: The Netflix Eureka Service Registry. All independent services register their dynamic IPs and ports here, enabling seamless internal discovery.
3. **`api-gateway` (Port 8080)**: The single entry point for client applications. It uses Spring Cloud Gateway to intelligently route HTTP requests to the appropriate microservices (load-balanced by Eureka). It also features a global `JwtAuthenticationFilter` to validate tokens securely.
4. **`identity-service` (Port 8081)**: Manages Employee Identity, Signup, Login, and JWT generation processes. Connects to `leavemgt_identity` DB.
5. **`timesheet-service` (Port 8082)**: Dedicated to capturing employee daily work hours logged against projects and summarizing them as weekly timesheets. Connects to `leavemgt_timesheet` DB.
6. **`leave-service` (Port 8083)**: Manages holiday schedules, tracks employee leave balances, and handles new leave application requests by automatically validating rules (`such as ensuring enough balance`). Connects to `leavemgt_leave` DB.
7. **`admin-service` (Port 8084)**: Acts as the orchestrator for core master data (e.g., Leave Policies) and managerial approvals. It communicates directly with Timesheet and Leave services over the internal network using Feign clients to approve/reject requests. Connects to `leavemgt_admin` DB.

## 🛠️ Technology Stack
- **Languages / Frameworks**: Java 17+, Spring Boot 3.x, Spring Cloud Ecosystem (Netflix Eureka, Config, Gateway)
- **Database**: MySQL (Hibernate/JPA DDL-update managed schemas)
- **Security**: JWT (JSON Web Tokens), BCrypt password hashing
- **Interservice Communication**: OpenFeign (Synchronous), Eureka (Discovery)
- **Documentation**: Springdoc OpenAPI v3 (Swagger UI centrally aggregated via Gateway)
- **Event Bus / Messaging**: RabbitMQ dependencies available

## 🚀 Getting Started

### Database Setup
Before starting the application, ensure you have MySQL running on `localhost:3306` and create the core databases. Thanks to Hibernate `ddl-auto: update`, tables define themselves automatically on boot!
```sql
CREATE DATABASE leavemgt_identity;
CREATE DATABASE leavemgt_timesheet;
CREATE DATABASE leavemgt_leave;
CREATE DATABASE leavemgt_admin;
```

### The Boot Sequence (Critical)
Because microservices depend on centralized config and discovery, **you must start the services in this exact order**:

1. Start **`config-server`** -> Wait for it to fully initialize on port `8888`.
2. Start **`eureka-server`** -> Wait for it to map on port `8761`. (Check `http://localhost:8761` in browser)
3. Start **`api-gateway`** -> Listens on standard interface `8080`.
4. Start the feature microservices: **`identity-service`**, **`timesheet-service`**, **`leave-service`**, **`admin-service`** in any order.

### Using the APIs & Swagger
Once everything is registered in Eureka, navigate to:
**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Use the **"Select a definition"** dropdown in the top-right corner to browse between the different Swagger documentations associated with Identity, Leave, Admin, and Timesheet services dynamically routed through the Gateway.

---
*For detailed API payload definitions, refer to Swagger or the `API_DOCUMENTATION.md` file located in the root directory. To understand the operational flow and setup dummy data, see `usecase_flow.txt`!*
