# Project Demo Data and Full Usage Guide

This file is the practical checklist for making the app usable end to end after the services are running. Use it when you want a clean demo with login, leave balances, holidays, timesheets, and approvals.

## 1. Start Everything

Start the backend in this order:

1. `config-server`
2. `eureka-server`
3. `identity-service`
4. `timesheet-service`
5. `leave-service`
6. `admin-service`
7. `api-gateway`
8. `frontend`

Frontend:

```powershell
cd D:\Desktop\Leave-management\frontend
npm run dev
```

Open:

```text
http://localhost:5173
```

Gateway Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## 2. Create Demo Users

Create users through the frontend signup page, or call signup through the gateway.

Employee:

```json
{
  "employeeCode": "EMP001",
  "name": "Jane Employee",
  "email": "employee@company.com",
  "password": "Employee123"
}
```

Manager/Admin:

```json
{
  "employeeCode": "ADM001",
  "name": "Admin User",
  "email": "admin@company.com",
  "password": "Admin123"
}
```

Manager:

```json
{
  "employeeCode": "MGR001",
  "name": "Manager User",
  "email": "manager@company.com",
  "password": "Manager123"
}
```

Login accepts either `employeeCode` or `email`.

Employee login:

```text
EMP001 / Employee123
```

Admin login:

```text
ADM001 / Admin123
```

Manager login:

```text
MGR001 / Manager123
```

## 3. Promote Admin User

After signing up `ADM001`, run this in MySQL.

```sql
USE auth_db;

DELETE FROM user_roles
WHERE user_id = (
  SELECT id FROM users WHERE employee_code = 'ADM001'
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN'
FROM users
WHERE employee_code = 'ADM001';
```

Then log out and log in again as `ADM001` so the new role is included in the JWT.

Promote `MGR001` to manager:

```sql
USE auth_db;

DELETE FROM user_roles
WHERE user_id = (
  SELECT id FROM users WHERE employee_code = 'MGR001'
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_MANAGER'
FROM users
WHERE employee_code = 'MGR001';
```

Manager approval rule:

- Employee leaves can be approved by `ROLE_ADMIN` or `ROLE_MANAGER`.
- Admin leaves must be approved by `ROLE_MANAGER`.
- Nobody can approve or reject their own leave request.

## 4. Seed Timesheet Projects

The timesheet screen needs active projects. Run this in MySQL.

```sql
USE timesheet_db;

INSERT INTO project (project_code, name, description, active, created_on)
VALUES
  ('PRJ-ALPHA', 'Alpha Web App', 'Internal employee self-service web application', 1, NOW()),
  ('PRJ-BETA', 'Beta Mobile App', 'Mobile leave and attendance companion app', 1, NOW()),
  ('PRJ-GAMMA', 'Gamma Platform', 'Shared platform and integration engineering', 1, NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  active = VALUES(active);
```

## 5. Seed Leave Policies

Policies are managed by `admin-service`. They define the master rules, but they do not automatically create employee balances yet.

Run this in MySQL:

```sql
USE admin_db;

INSERT INTO leave_policy
  (policy_code, leave_type, annual_allocation, carry_forward_allowed, max_carry_forward_days)
VALUES
  ('EARNED_2026', 'EARNED', 20.0, 1, 5),
  ('SICK_2026', 'SICK', 10.0, 0, 0),
  ('CASUAL_2026', 'CASUAL', 8.0, 0, 0)
ON DUPLICATE KEY UPDATE
  leave_type = VALUES(leave_type),
  annual_allocation = VALUES(annual_allocation),
  carry_forward_allowed = VALUES(carry_forward_allowed),
  max_carry_forward_days = VALUES(max_carry_forward_days);
```

## 6. Seed Employee Leave Balances

Leave apply will fail until the logged-in employee has leave balance rows.

Run this in MySQL:

```sql
USE leave_db;

INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES
  ('EMP001', 'EARNED', 20.0, 0.0),
  ('EMP001', 'SICK', 10.0, 0.0),
  ('EMP001', 'CASUAL', 8.0, 0.0)
ON DUPLICATE KEY UPDATE
  allocated = VALUES(allocated),
  consumed = VALUES(consumed);
```

If your database does not have a unique key on `employee_code + leave_type`, use this safer manual version instead:

```sql
USE leave_db;

DELETE FROM leave_balance WHERE employee_code = 'EMP001';

INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES
  ('EMP001', 'EARNED', 20.0, 0.0),
  ('EMP001', 'SICK', 10.0, 0.0),
  ('EMP001', 'CASUAL', 8.0, 0.0);
```

## 7. Seed Holidays

Holidays can be added from the Admin page under `Master Data`, or seeded directly.

```sql
USE leave_db;

INSERT INTO holiday (date, description)
VALUES
  ('2026-01-26', 'Republic Day'),
  ('2026-03-04', 'Holi'),
  ('2026-08-15', 'Independence Day'),
  ('2026-10-20', 'Diwali'),
  ('2026-12-25', 'Christmas Day')
ON DUPLICATE KEY UPDATE
  description = VALUES(description);
```

## 8. Employee Workflow

Use `EMP001`.

1. Log in with `EMP001 / Employee123`.
2. Open Dashboard and confirm leave balance is visible.
3. Open Leaves.
4. Apply for leave:

```text
Leave Type: SICK
Start Date: 2026-05-04
End Date: 2026-05-05
Reason: Sick leave
```

5. Open Timesheets.
6. Pick week starting `2026-05-04`.
7. Add hours using seeded project codes:

```text
PRJ-ALPHA - 8 hours - Login and API integration
PRJ-BETA  - 7 hours - Mobile sync testing
PRJ-GAMMA - 6 hours - Platform cleanup
```

8. Click `Save Draft`.
9. Click `Submit Week`.

Important timesheet rules:

- Week start should be Monday.
- Total weekly hours must be `60` or less.
- Every filled row needs project and hours.
- Project code must exist in `timesheet_db.project`.
- Submitted or approved sheets cannot be edited.

## 9. Admin Workflow

Use `ADM001`.

1. Log in with `ADM001 / Admin123`.
2. Open Admin.
3. In `Leaves`, approve or reject pending leave requests.
4. In `Timesheets`, approve or reject submitted timesheets.
5. In `Master Data`, add more holidays.

For admin leave approval, use `MGR001` instead of `ADM001`. If `ADM001` applies for leave, log in as `MGR001` and approve it from the Admin Portal.

Approval behavior:

- Admin approval actions are sent through RabbitMQ.
- The status may update shortly after the click, once the target service consumes the event.
- Approved leave deducts balance from `leave_balance.consumed`.

## 10. Useful Direct API Routes

Your live gateway config uses these prefixes:

```text
Auth:       /auth/api/auth/...
Leave:      /leave/api/leave/...
Holidays:   /leave/api/holidays
Timesheet:  /timesheet/api/timesheet/...
Admin:      /admin/api/admin/...
```

Examples:

```text
POST /auth/api/auth/login
GET  /leave/api/leave/balance/EMP001
POST /leave/api/leave/requests
GET  /timesheet/api/timesheet/projects
PUT  /timesheet/api/timesheet/weeks/2026-05-04
POST /timesheet/api/timesheet/weeks/2026-05-04/submit
GET  /admin/api/admin/approvals/leaves
GET  /admin/api/admin/approvals/timesheets
```

## 11. Common Problems and Fixes

`No leave balance record found for type: SICK`

Fix: add a `leave_balance` row for the logged-in employee and selected leave type.

`Insufficient balance for leave type`

Fix: increase `allocated`, reduce `consumed`, or choose a leave type with available balance.

`Date range overlaps with existing leave`

Fix: choose dates that do not overlap an existing submitted or approved leave request.

`Project not found`

Fix: insert the project into `timesheet_db.project`, and make sure `active = 1`.

`Work date must fall within the selected week`

Fix: select a Monday week start and use dates between that Monday and Sunday.

`Total hours for the week exceed allowed limit (60)`

Fix: reduce weekly hours to 60 or less.

`Admin page shows no employee code`

Fix: restart/rebuild `leave-service` after the DTO update that includes `employeeCode`.

## 12. Clean Demo Reset

Use this if you want to repeat the demo from scratch for `EMP001`.

```sql
USE leave_db;
DELETE FROM leave_request WHERE employee_code = 'EMP001';
DELETE FROM leave_balance WHERE employee_code = 'EMP001';
INSERT INTO leave_balance (employee_code, leave_type, allocated, consumed)
VALUES
  ('EMP001', 'EARNED', 20.0, 0.0),
  ('EMP001', 'SICK', 10.0, 0.0),
  ('EMP001', 'CASUAL', 8.0, 0.0);

USE timesheet_db;
DELETE FROM timesheet_entry
WHERE timesheet_id IN (
  SELECT id FROM timesheet WHERE employee_code = 'EMP001'
);
DELETE FROM timesheet WHERE employee_code = 'EMP001';
```

After reset, log in again and run the employee workflow.
