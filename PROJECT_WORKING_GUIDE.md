# Leave Management Project Working Guide

This guide explains how the project works end to end based on the current codebase. It is meant to help you demonstrate the system, onboard someone new, or run a full local walkthrough without guessing which parts are finished and which parts still require manual setup.

## 1. What This Project Does

This is a microservices-based leave and timesheet platform with:

- employee signup and login
- leave balance tracking
- leave request submission
- weekly timesheet submission
- manager/admin approval actions
- leave policy management
- holiday master data management
- centralized routing through an API gateway

The system is split into these services:

| Service | Default Port | Purpose |
| --- | --- | --- |
| `config-server` | `8888` | Reads shared configuration from the external config repo |
| `eureka-server` | `8761` | Service discovery |
| `api-gateway` | `8080` | Single entry point for frontend and APIs |
| `identity-service` | `8081` | Signup, login, JWT generation |
| `timesheet-service` | `8082` | Timesheet draft/save/submit/approval status |
| `leave-service` | `8083` | Leave balance, leave requests, holidays |
| `admin-service` | `8084` | Leave policy management and approval orchestration |

## 2. How the Request Flow Works

The normal request path is:

`Frontend -> API Gateway -> Target Microservice`

How authentication works:

1. A user signs up or logs in through `identity-service`.
2. Login returns a JWT token.
3. All protected requests go through `api-gateway`.
4. The gateway validates the JWT.
5. The gateway extracts the employee code from the token and forwards it as `X-Employee-Code`.
6. `leave-service` and `timesheet-service` use that header to identify the logged-in employee.

## 3. Local Setup and Startup Order

### Prerequisites

- Java 17+
- Maven
- MySQL running on `localhost:3306`
- RabbitMQ
- optional: Docker Desktop if you want to use `docker-compose.yml`

### Databases used

The services create their own schemas automatically if MySQL is available:

- `auth_db`
- `timesheet_db`
- `leave_db`
- `admin_db`

### Important config dependency

`config-server` pulls properties from this GitHub config repo:

- `https://github.com/saurabhsrivastava2001/leave-management-config`

So the app depends on that external config source unless you replace it with your own config.

### Recommended boot order

Start the services in this order:

1. `config-server`
2. `eureka-server`
3. `identity-service`
4. `timesheet-service`
5. `leave-service`
6. `admin-service`
7. `api-gateway`
8. `frontend`

### Fastest way to start backend infra

If you want the containerized stack:

```bash
docker compose up --build
```

That compose file also starts:

- RabbitMQ management UI: `http://localhost:15672`
- Grafana: `http://localhost:3000`
- Loki: `http://localhost:3100`

### Frontend start

From the `frontend` folder:

```bash
npm install
npm run dev
```

Frontend default Vite URL is usually:

- `http://localhost:5173`

### Swagger

Once the gateway is up:

- `http://localhost:8080/swagger-ui.html`

## 4. Main Functional Modules

### Identity

Handled by `identity-service`.

Current capabilities:

- employee signup
- employee login
- JWT token generation

Current API endpoints:

- `POST /api/auth/signup`
- `POST /api/auth/login`

### Leave Management

Handled by `leave-service`.

Current capabilities:

- get leave balances
- create leave requests
- get own leave history
- get pending leave approvals
- approve or reject leave
- get team leave calendar
- create and view holidays

Current API endpoints:

- `GET /api/leave/balance/{employeeCode}`
- `POST /api/leave/requests`
- `GET /api/leave/history`
- `GET /api/leave/team-calendar`
- `GET /api/leave/pending-approvals`
- `PUT /api/leave/{id}/status`
- `GET /api/holidays`
- `POST /api/holidays`

### Timesheet Management

Handled by `timesheet-service`.

Current capabilities:

- save or update weekly timesheet draft
- submit weekly timesheet
- get weekly timesheet
- get pending approvals
- approve or reject timesheet

Current API endpoints:

- `GET /api/timesheet/weeks/{weekStart}`
- `POST /api/timesheet/entries`
- `POST /api/timesheet/weeks/{weekStart}/submit`
- `GET /api/timesheet/pending-approvals`
- `PUT /api/timesheet/{id}/status`

### Admin

Handled by `admin-service`.

Current capabilities:

- create, read, update, delete leave policies
- fetch pending leave approvals
- fetch pending timesheet approvals
- approve or reject leave asynchronously
- approve or reject timesheets asynchronously

Current API endpoints:

- `GET /api/admin/master/policies`
- `GET /api/admin/master/policies/{code}`
- `POST /api/admin/master/policies`
- `DELETE /api/admin/master/policies/{code}`
- `GET /api/admin/approvals/leaves`
- `POST /api/admin/approvals/leaves/{id}/approve`
- `POST /api/admin/approvals/leaves/{id}/reject`
- `GET /api/admin/approvals/timesheets`
- `POST /api/admin/approvals/timesheets/{id}/approve`
- `POST /api/admin/approvals/timesheets/{id}/reject`

## 5. Leave Schemes in This Project

The project supports leave types like:

- `EARNED`
- `SICK`
- `CASUAL`

There are two related concepts:

### Leave Policy

Stored in `admin_db`.

A leave policy defines the master rule for a leave type:

- `policyCode`
- `leaveType`
- `annualAllocation`
- `carryForwardAllowed`
- `maxCarryForwardDays`

Example:

```json
{
  "policyCode": "EARNED_2026",
  "leaveType": "EARNED",
  "annualAllocation": 20.0,
  "carryForwardAllowed": true,
  "maxCarryForwardDays": 5
}
```

Important current behavior:

- policies are stored and managed by admin-service
- leave-service does not automatically allocate balances from policies
- employee balances still need their own records in `leave_balance`

### Leave Balance

Stored in `leave_db.leave_balance`.

This is the actual employee-specific available leave record:

- `employee_code`
- `leave_type`
- `allocated`
- `consumed`

The system checks this table when an employee applies for leave.

If there is no balance row, leave application fails.

## 6. Roles and Admin Behavior

The intended roles in the code are:

- `ROLE_EMPLOYEE`
- `ROLE_MANAGER`
- `ROLE_ADMIN`

### What really happens today

Current implementation details are important:

1. Signup always creates users as `ROLE_EMPLOYEE`.
2. There is no API or frontend screen to create a manager or admin user.
3. There is no dedicated employee-management module yet.
4. Backend role-based authorization is not fully enforced.
5. The gateway checks authentication, but not role permissions.
6. The frontend has an Admin page and currently does not hard-block it at route level.

So if you want a real admin or manager account today, you must create the user first and then update the role directly in the identity database.

### How to add an admin or manager today

Step 1: Sign up the user normally through UI or `POST /api/auth/signup`

Example signup payload:

```json
{
  "employeeCode": "ADM001",
  "name": "Admin User",
  "email": "admin@company.com",
  "password": "Admin123"
}
```

Step 2: Update the role in `auth_db`

The `users` table stores the user record and the `user_roles` table stores roles.

Example SQL for admin:

```sql
DELETE FROM user_roles WHERE user_id = (
  SELECT id FROM users WHERE employee_code = 'ADM001'
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN'
FROM users
WHERE employee_code = 'ADM001';
```

Example SQL for manager:

```sql
DELETE FROM user_roles WHERE user_id = (
  SELECT id FROM users WHERE employee_code = 'MGR001'
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_MANAGER'
FROM users
WHERE employee_code = 'MGR001';
```

After that, log in again so the JWT contains the updated role.

## 7. How to Add Employees

Employee creation is supported directly.

### Through frontend

1. Open the signup page.
2. Enter employee code, name, email, and password.
3. Submit.
4. Log in using employee code or email plus password.

### Through API

`POST /api/auth/signup`

```json
{
  "employeeCode": "EMP001",
  "name": "Jane Developer",
  "email": "jane@company.com",
  "password": "SecurePassword123"
}
```

Current result:

- user is created in `auth_db.users`
- role is `ROLE_EMPLOYEE`
- user can log in immediately

## 8. How to Add a Project

This is one of the most important current limitations.

There is a `Project` entity in `timesheet-service`, but there is:

- no project controller
- no project service API
- no frontend screen to create projects

That means project creation is manual right now.

### Add a project through MySQL

Use `timesheet_db`:

```sql
INSERT INTO project (project_code, name, description, active, created_on)
VALUES ('PRJ-ALPHA', 'Alpha Web App', 'Internal web application project', 1, NOW());
```

You can add more:

```sql
INSERT INTO project (project_code, name, description, active, created_on)
VALUES ('PRJ-BETA', 'Beta Mobile App', 'Mobile product workstream', 1, NOW());

INSERT INTO project (project_code, name, description, active, created_on)
VALUES ('PRJ-GAMMA', 'Gamma Platform', 'Platform engineering initiative', 1, NOW());
```

Important note:

- the frontend timesheet page already shows `PRJ-ALPHA`, `PRJ-BETA`, and `PRJ-GAMMA` in its dropdown
- if those projects do not exist in the database, timesheet submission will fail

## 9. Full End-to-End Walkthrough

This is the cleanest demo flow.

### Step A: Start the system

Make sure all services, MySQL, RabbitMQ, and frontend are running.

### Step B: Seed project data

In `timesheet_db`:

```sql
INSERT INTO project (project_code, name, description, active, created_on)
VALUES ('PRJ-ALPHA', 'Alpha Web App', 'Internal web application project', 1, NOW());
```

### Step C: Create leave policy

Call:

`POST /api/admin/master/policies`

```json
{
  "policyCode": "EARNED_2026",
  "leaveType": "EARNED",
  "annualAllocation": 20.0,
  "carryForwardAllowed": true,
  "maxCarryForwardDays": 5
}
```

### Step D: Create employee

Call:

`POST /api/auth/signup`

```json
{
  "employeeCode": "EMP001",
  "name": "Jane Developer",
  "email": "jane@company.com",
  "password": "SecurePassword123"
}
```

### Step E: Seed employee leave balance

In `leave_db`:

```sql
INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES ('EMP001', 'EARNED', 20.0, 0.0);
```

Optional:

```sql
INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES ('EMP001', 'SICK', 10.0, 0.0);

INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES ('EMP001', 'CASUAL', 8.0, 0.0);
```

### Step F: Login

Call:

`POST /api/auth/login`

```json
{
  "usernameOrEmail": "EMP001",
  "password": "SecurePassword123"
}
```

Use the returned JWT for further requests:

`Authorization: Bearer <token>`

### Step G: Log and submit a timesheet

Call:

`POST /api/timesheet/entries`

```json
{
  "weekStartDate": "2026-05-04",
  "entries": [
    {
      "projectCode": "PRJ-ALPHA",
      "workDate": "2026-05-04",
      "hours": 8.0,
      "taskSummary": "Worked on login and API integration"
    },
    {
      "projectCode": "PRJ-ALPHA",
      "workDate": "2026-05-05",
      "hours": 7.5,
      "taskSummary": "Bug fixing and test coverage"
    }
  ]
}
```

Then submit:

`POST /api/timesheet/weeks/2026-05-04/submit`

### Step H: Apply for leave

Call:

`POST /api/leave/requests`

```json
{
  "leaveType": "EARNED",
  "startDate": "2026-05-20",
  "endDate": "2026-05-22",
  "reason": "Family event"
}
```

### Step I: Manager/Admin reviews pending items

Pending leaves:

- `GET /api/admin/approvals/leaves`

Pending timesheets:

- `GET /api/admin/approvals/timesheets`

Approve leave:

- `POST /api/admin/approvals/leaves/{id}/approve?comments=Approved`

Reject leave:

- `POST /api/admin/approvals/leaves/{id}/reject?comments=Need%20better%20planning`

Approve timesheet:

- `POST /api/admin/approvals/timesheets/{id}/approve?comments=Looks%20good`

Reject timesheet:

- `POST /api/admin/approvals/timesheets/{id}/reject?comments=Please%20correct%20hours`

### Step J: What happens after approval

Timesheet approval:

- admin-service publishes an event to RabbitMQ
- timesheet-service consumes the event
- timesheet status changes in the timesheet database

Leave approval:

- admin-service publishes an event to RabbitMQ
- leave-service consumes the event
- leave request status changes
- approved leave days are deducted from `leave_balance.consumed`

## 10. Frontend Walkthrough

### Login Page

Purpose:

- authenticate an existing user

Inputs:

- employee code or email
- password

### Signup Page

Purpose:

- self-register a new employee

Inputs:

- employee code
- full name
- email
- password

### Dashboard

Shows:

- current leave balance summary
- recent leave activity
- link to leave request page
- link to timesheet page
- pending approvals card for manager/admin-style users

### Leaves Page

User can:

- see leave balances
- submit leave request
- view leave history

### Timesheets Page

User can:

- choose project
- enter work date
- enter hours
- enter task summary
- submit directly

Current frontend behavior:

- it submits the timesheet immediately after saving
- it assumes the chosen project code already exists in the database

### Admin Page

User can:

- view pending leave requests
- approve or reject leaves
- view pending timesheets
- approve or reject timesheets
- add public holidays

Current limitation:

- leave policies are supported by API but not exposed in the current frontend admin page

## 11. Practical Limitations to Mention During Demo

These are worth calling out clearly if you present the project:

1. Project creation is manual through the database today.
2. Admin and manager user creation is manual after signup.
3. Leave policies do not auto-create employee balances.
4. Employee management is not a full CRUD module yet.
5. Role-based authorization is only partially implemented.
6. Admin approval writes are asynchronous through RabbitMQ, so status changes may not appear instantly if the consumer is delayed.

## 12. Best Demo Script

If you want the smoothest demonstration, use this order:

1. Start backend, MySQL, RabbitMQ, and frontend.
2. Insert project rows into `timesheet_db.project`.
3. Create one leave policy through admin API or Swagger.
4. Sign up one employee.
5. Insert leave balance rows for that employee.
6. Sign up one manager/admin account.
7. Change that second account role in `auth_db.user_roles`.
8. Log in as employee and submit one leave request plus one timesheet.
9. Log in as admin/manager and approve both.
10. Show the balance deduction and updated statuses.

## 13. Suggested Future Improvements

If you want this project to feel complete for real users, the next best additions are:

1. project management API and UI
2. employee management and role assignment UI
3. strict backend role authorization
4. automatic balance allocation from leave policies
5. manager-to-employee mapping
6. approval audit history
7. richer dashboard reporting
