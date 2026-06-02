-- ============================================================
--  HRMS v2 – Human Resource Management System
--  SQL Server Script  |  Run in SSMS
--  BCE 6A - DBMS Lab Project
--  UPDATED: Added SystemUsers table for Login
-- ============================================================

-- Step 1: Create Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'HRMS_DB')
BEGIN
    CREATE DATABASE HRMS_DB;
END
GO

USE HRMS_DB;
GO

-- Step 2: Drop tables in reverse order (safe re-run)
IF OBJECT_ID('SystemUsers',       'U') IS NOT NULL DROP TABLE SystemUsers;
IF OBJECT_ID('LeaveApplications', 'U') IS NOT NULL DROP TABLE LeaveApplications;
IF OBJECT_ID('Attendance',        'U') IS NOT NULL DROP TABLE Attendance;
IF OBJECT_ID('Salaries',          'U') IS NOT NULL DROP TABLE Salaries;
IF OBJECT_ID('Employees',         'U') IS NOT NULL DROP TABLE Employees;
IF OBJECT_ID('Departments',       'U') IS NOT NULL DROP TABLE Departments;
GO

-- ============================================================
-- TABLE 1: Departments
-- ============================================================
CREATE TABLE Departments (
    Dept_ID    INT          IDENTITY(1,1) PRIMARY KEY,
    Dept_Name  VARCHAR(100) NOT NULL UNIQUE,
    Manager    VARCHAR(100) NULL,
    Location   VARCHAR(100) NULL,
    Created_At DATETIME     DEFAULT GETDATE()
);
GO

-- ============================================================
-- TABLE 2: Employees
-- ============================================================
CREATE TABLE Employees (
    Emp_ID       INT          IDENTITY(1,1) PRIMARY KEY,
    Emp_Name     VARCHAR(100) NOT NULL,
    Dept_ID      INT          NOT NULL,
    Joining_Date DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Email        VARCHAR(150) NULL UNIQUE,
    Phone        VARCHAR(20)  NULL,
    Position     VARCHAR(100) NULL,
    Status       VARCHAR(20)  NOT NULL DEFAULT 'Active'
                              CHECK (Status IN ('Active','Inactive','On Leave')),

    CONSTRAINT FK_Emp_Dept FOREIGN KEY (Dept_ID)
        REFERENCES Departments(Dept_ID)
        ON UPDATE CASCADE
        ON DELETE NO ACTION
);
GO

-- ============================================================
-- TABLE 3: Salaries
-- ============================================================
CREATE TABLE Salaries (
    Salary_ID    INT           IDENTITY(1,1) PRIMARY KEY,
    Emp_ID       INT           NOT NULL,
    Basic_Salary DECIMAL(12,2) NOT NULL CHECK (Basic_Salary >= 0),
    Bonus        DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (Bonus >= 0),
    Deductions   DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (Deductions >= 0),
    Net_Salary   AS (Basic_Salary + Bonus - Deductions) PERSISTED,
    Pay_Month    VARCHAR(20)   NOT NULL,
    Pay_Date     DATE          DEFAULT CAST(GETDATE() AS DATE),

    CONSTRAINT FK_Salary_Emp FOREIGN KEY (Emp_ID)
        REFERENCES Employees(Emp_ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
GO

-- ============================================================
-- TABLE 4: Attendance
-- ============================================================
CREATE TABLE Attendance (
    Att_ID   INT         IDENTITY(1,1) PRIMARY KEY,
    Emp_ID   INT         NOT NULL,
    Att_Date DATE        NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Status   VARCHAR(10) NOT NULL
             CHECK (Status IN ('Present','Absent','Leave','Half Day')),

    CONSTRAINT UQ_Att_EmpDate UNIQUE (Emp_ID, Att_Date),

    CONSTRAINT FK_Att_Emp FOREIGN KEY (Emp_ID)
        REFERENCES Employees(Emp_ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
GO

-- ============================================================
-- TABLE 5: LeaveApplications
-- ============================================================
CREATE TABLE LeaveApplications (
    Leave_ID   INT          IDENTITY(1,1) PRIMARY KEY,
    Emp_ID     INT          NOT NULL,
    Leave_Type VARCHAR(20)  NOT NULL
               CHECK (Leave_Type IN ('Casual','Medical','Annual','Unpaid')),
    Start_Date DATE         NOT NULL,
    End_Date   DATE         NOT NULL,
    Reason     VARCHAR(500) NULL,
    Status     VARCHAR(20)  NOT NULL DEFAULT 'Pending'
               CHECK (Status IN ('Pending','Approved','Rejected')),
    Applied_On DATETIME     DEFAULT GETDATE(),

    CONSTRAINT CHK_Leave_Dates CHECK (End_Date >= Start_Date),

    CONSTRAINT FK_Leave_Emp FOREIGN KEY (Emp_ID)
        REFERENCES Employees(Emp_ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
GO

-- ============================================================
-- TABLE 6: SystemUsers  (NEW - for Login)
-- ============================================================
CREATE TABLE SystemUsers (
    User_ID       INT          IDENTITY(1,1) PRIMARY KEY,
    Username      VARCHAR(50)  NOT NULL UNIQUE,
    Password_Hash VARCHAR(255) NOT NULL,   -- store hashed in production!
    Role          VARCHAR(20)  NOT NULL CHECK (Role IN ('Admin','Employee')),
    Emp_ID        INT          NULL,       -- NULL for admin-only accounts
    Is_Active     BIT          NOT NULL DEFAULT 1,
    Created_At    DATETIME     DEFAULT GETDATE(),
    Last_Login    DATETIME     NULL,

    CONSTRAINT FK_User_Emp FOREIGN KEY (Emp_ID)
        REFERENCES Employees(Emp_ID)
        ON DELETE SET NULL
);
GO

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO Departments (Dept_Name, Manager, Location) VALUES
('Human Resources', 'Sara Khan',    'Floor 1'),
('Engineering',     'Ali Raza',     'Floor 2'),
('Finance',         'Hamza Malik',  'Floor 3'),
('Marketing',       'Nadia Shah',   'Floor 1'),
('IT Support',      'Usman Ahmed',  'Floor 2');

INSERT INTO Employees (Emp_Name, Dept_ID, Joining_Date, Email, Phone, Position) VALUES
('Ahmed Siddiqui', 2, '2022-03-15', 'ahmed.s@hrms.com',  '0311-1234567', 'Senior Developer'),
('Fatima Noor',    1, '2021-07-01', 'fatima.n@hrms.com', '0322-2345678', 'HR Manager'),
('Bilal Qureshi',  3, '2023-01-10', 'bilal.q@hrms.com',  '0333-3456789', 'Accountant'),
('Zara Hussain',   4, '2022-11-20', 'zara.h@hrms.com',   '0344-4567890', 'Marketing Lead'),
('Omar Sheikh',    2, '2023-06-01', 'omar.s@hrms.com',   '0355-5678901', 'Junior Developer'),
('Aisha Malik',    5, '2021-09-15', 'aisha.m@hrms.com',  '0366-6789012', 'IT Specialist');

INSERT INTO Salaries (Emp_ID, Basic_Salary, Bonus, Deductions, Pay_Month) VALUES
(1, 120000, 15000, 5000,  'April-2025'),
(2,  90000, 10000, 4000,  'April-2025'),
(3,  75000,  8000, 3500,  'April-2025'),
(4,  85000, 12000, 4000,  'April-2025'),
(5,  60000,  5000, 2500,  'April-2025'),
(6,  70000,  7000, 3000,  'April-2025');

INSERT INTO Attendance (Emp_ID, Att_Date, Status) VALUES
(1, CAST(DATEADD(DAY,-2,GETDATE()) AS DATE), 'Present'),
(2, CAST(DATEADD(DAY,-2,GETDATE()) AS DATE), 'Present'),
(3, CAST(DATEADD(DAY,-2,GETDATE()) AS DATE), 'Absent'),
(1, CAST(DATEADD(DAY,-1,GETDATE()) AS DATE), 'Present'),
(2, CAST(DATEADD(DAY,-1,GETDATE()) AS DATE), 'Leave'),
(3, CAST(DATEADD(DAY,-1,GETDATE()) AS DATE), 'Present');

INSERT INTO LeaveApplications (Emp_ID, Leave_Type, Start_Date, End_Date, Reason, Status) VALUES
(2, 'Annual',  '2025-05-10', '2025-05-12', 'Family trip',        'Approved'),
(3, 'Medical', '2025-05-15', '2025-05-16', 'Doctor appointment', 'Pending');

-- ── SystemUsers seed ──────────────────────────────────────────────────────────
-- Admin account (no Emp_ID)
INSERT INTO SystemUsers (Username, Password_Hash, Role, Emp_ID) VALUES
('admin', 'admin123', 'Admin', NULL);

-- Employee accounts linked to Employees table
INSERT INTO SystemUsers (Username, Password_Hash, Role, Emp_ID) VALUES
('ahmed.s',  'emp123', 'Employee', 1),
('fatima.n', 'emp123', 'Employee', 2),
('bilal.q',  'emp123', 'Employee', 3),
('zara.h',   'emp123', 'Employee', 4),
('omar.s',   'emp123', 'Employee', 5),
('aisha.m',  'emp123', 'Employee', 6);

-- Also add shorthand aliases
INSERT INTO SystemUsers (Username, Password_Hash, Role, Emp_ID) VALUES
('emp001', 'emp123', 'Employee', 1),
('emp002', 'emp123', 'Employee', 2);
GO

-- ============================================================
-- VIEWS
-- ============================================================
CREATE OR ALTER VIEW vw_EmployeeDetails AS
SELECT e.Emp_ID, e.Emp_Name, d.Dept_Name, e.Position,
       e.Joining_Date, e.Email, e.Phone, e.Status
FROM Employees e
INNER JOIN Departments d ON e.Dept_ID = d.Dept_ID;
GO

CREATE OR ALTER VIEW vw_LatestSalary AS
SELECT e.Emp_ID, e.Emp_Name, d.Dept_Name,
       s.Basic_Salary, s.Bonus, s.Deductions, s.Net_Salary, s.Pay_Month
FROM Employees e
INNER JOIN Departments d ON e.Dept_ID = d.Dept_ID
INNER JOIN Salaries    s ON e.Emp_ID  = s.Emp_ID
WHERE s.Salary_ID = (SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID = e.Emp_ID);
GO

CREATE OR ALTER VIEW vw_DeptStats AS
SELECT
    d.Dept_Name,
    COUNT(e.Emp_ID)              AS Total_Employees,
    ISNULL(AVG(s.Net_Salary), 0) AS Avg_Net_Salary,
    ISNULL(MAX(s.Net_Salary), 0) AS Max_Salary,
    ISNULL(MIN(s.Net_Salary), 0) AS Min_Salary,
    ISNULL(SUM(s.Net_Salary), 0) AS Total_Payroll
FROM Departments d
LEFT JOIN Employees e ON d.Dept_ID = e.Dept_ID
LEFT JOIN Salaries  s ON e.Emp_ID  = s.Emp_ID
    AND s.Salary_ID = (SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID = e.Emp_ID)
GROUP BY d.Dept_Name;
GO

PRINT 'HRMS_DB v2 created successfully! Login credentials:';
PRINT '  Admin    -> username: admin    | password: admin123';
PRINT '  Employee -> username: emp001   | password: emp123';
PRINT '  Employee -> username: ahmed.s  | password: emp123';
GO

-- ============================================================
-- v3 ADDITIONS: AuditLog table
-- ============================================================
IF OBJECT_ID('SystemAuditLog', 'U') IS NULL
BEGIN
    CREATE TABLE SystemAuditLog (
        Log_ID    INT          IDENTITY(1,1) PRIMARY KEY,
        Username  VARCHAR(50)  NOT NULL DEFAULT 'system',
        Module    VARCHAR(50)  NOT NULL,
        Action    VARCHAR(100) NOT NULL,
        Details   VARCHAR(500) NULL,
        LogTime   DATETIME     NOT NULL DEFAULT GETDATE()
    );
    PRINT 'SystemAuditLog table created.';
END
GO

-- Add Last_Login to SystemUsers if missing
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('SystemUsers') AND name='Last_Login')
    ALTER TABLE SystemUsers ADD Last_Login DATETIME NULL;
GO

PRINT 'v3 schema additions complete.';
PRINT 'Run this script (or just the v3 section) against your existing HRMS_DB.';
GO
