-- ============================================================
--  HRMS v4 Schema Update
--  Run this script against your existing HRMS_DB
--  OR run HRMS_Database_v2.sql first for a fresh install
-- ============================================================
USE HRMS_DB;
GO

-- ── 1. Employee table new columns ────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Gender')
    ALTER TABLE Employees ADD Gender VARCHAR(10) NULL CHECK (Gender IN ('Male','Female','Other'));
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='CNIC')
    ALTER TABLE Employees ADD CNIC VARCHAR(20) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Address')
    ALTER TABLE Employees ADD Address VARCHAR(300) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Contract_Type')
    ALTER TABLE Employees ADD Contract_Type VARCHAR(20) NULL DEFAULT 'Permanent'
        CHECK (Contract_Type IN ('Permanent','Contract','Intern'));
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Role_Level')
    ALTER TABLE Employees ADD Role_Level VARCHAR(30) NULL DEFAULT 'Employee'
        CHECK (Role_Level IN ('Employee','Team Lead','HR Manager','Accountant','Director','Admin'));
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Photo_Path')
    ALTER TABLE Employees ADD Photo_Path VARCHAR(500) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Emergency_Contact')
    ALTER TABLE Employees ADD Emergency_Contact VARCHAR(200) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Overtime_Hours')
    ALTER TABLE Employees ADD Overtime_Hours DECIMAL(8,2) NOT NULL DEFAULT 0;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Employees') AND name='Tax_Percent')
    ALTER TABLE Employees ADD Tax_Percent DECIMAL(5,2) NOT NULL DEFAULT 0;
GO

-- ── 2. Salary table new columns (OT, Tax) ────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Salaries') AND name='Overtime_Pay')
    ALTER TABLE Salaries ADD Overtime_Pay DECIMAL(12,2) NOT NULL DEFAULT 0;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Salaries') AND name='Tax_Amount')
    ALTER TABLE Salaries ADD Tax_Amount DECIMAL(12,2) NOT NULL DEFAULT 0;
-- Recreate Net_Salary as persisted computed column (drop old one first if needed)
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Salaries') AND name='Net_Salary' AND is_computed=1)
BEGIN
    ALTER TABLE Salaries DROP COLUMN Net_Salary;
    ALTER TABLE Salaries ADD Net_Salary AS
        (Basic_Salary + Bonus + Overtime_Pay - Tax_Amount - Deductions) PERSISTED;
END
ELSE IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('Salaries') AND name='Net_Salary')
    ALTER TABLE Salaries ADD Net_Salary AS
        (Basic_Salary + Bonus + Overtime_Pay - Tax_Amount - Deductions) PERSISTED;
GO

-- ── 3. Recruitment table ──────────────────────────────────────────────────────
IF OBJECT_ID('Recruitment','U') IS NULL
BEGIN
    CREATE TABLE Recruitment (
        Candidate_ID   INT          IDENTITY(1,1) PRIMARY KEY,
        Name           VARCHAR(100) NOT NULL,
        Email          VARCHAR(150) NULL,
        Phone          VARCHAR(20)  NULL,
        Position_Applied VARCHAR(100) NULL,
        Department     VARCHAR(100) NULL,
        Status         VARCHAR(20)  NOT NULL DEFAULT 'Applied'
                       CHECK (Status IN ('Applied','Shortlisted','Interview','Hired','Rejected')),
        Applied_Date   DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
        Interview_Date DATE         NULL,
        Notes          VARCHAR(500) NULL,
        Created_At     DATETIME     DEFAULT GETDATE()
    );
    PRINT 'Recruitment table created.';
END
GO

-- ── 4. SystemAuditLog (if missing from v3) ────────────────────────────────────
IF OBJECT_ID('SystemAuditLog','U') IS NULL
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

-- ── 5. Update SystemUsers Role check to include new roles ─────────────────────
-- Drop old check constraint and add new one
DECLARE @con NVARCHAR(200)
SELECT @con=name FROM sys.check_constraints
WHERE parent_object_id=OBJECT_ID('SystemUsers') AND name LIKE '%Role%'
IF @con IS NOT NULL EXEC('ALTER TABLE SystemUsers DROP CONSTRAINT '+@con)
ALTER TABLE SystemUsers ADD CONSTRAINT CK_Users_Role
    CHECK (Role IN ('Admin','Employee','HR Manager','Team Lead','Accountant','Director'));
GO

-- ── 6. Seed data for new roles ────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM SystemUsers WHERE Username='hrmanager')
    INSERT INTO SystemUsers(Username,Password_Hash,Role,Emp_ID) VALUES('hrmanager','hr123','HR Manager',2);
IF NOT EXISTS (SELECT 1 FROM SystemUsers WHERE Username='teamlead')
    INSERT INTO SystemUsers(Username,Password_Hash,Role,Emp_ID) VALUES('teamlead','tl123','Team Lead',1);
IF NOT EXISTS (SELECT 1 FROM SystemUsers WHERE Username='accountant')
    INSERT INTO SystemUsers(Username,Password_Hash,Role,Emp_ID) VALUES('accountant','ac123','Accountant',3);
IF NOT EXISTS (SELECT 1 FROM SystemUsers WHERE Username='director')
    INSERT INTO SystemUsers(Username,Password_Hash,Role,Emp_ID) VALUES('director','dir123','Director',4);
GO

-- ── 7. Updated vw_DeptStats view ─────────────────────────────────────────────
CREATE OR ALTER VIEW vw_DeptStats AS
SELECT
    d.Dept_Name,
    COUNT(e.Emp_ID)              AS Total_Employees,
    ISNULL(AVG(s.Net_Salary),0)  AS Avg_Net_Salary,
    ISNULL(MAX(s.Net_Salary),0)  AS Max_Salary,
    ISNULL(MIN(s.Net_Salary),0)  AS Min_Salary,
    ISNULL(SUM(s.Net_Salary),0)  AS Total_Payroll
FROM Departments d
LEFT JOIN Employees e  ON d.Dept_ID=e.Dept_ID
LEFT JOIN Salaries  s  ON e.Emp_ID=s.Emp_ID
    AND s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=e.Emp_ID)
GROUP BY d.Dept_Name;
GO

-- ── 8. Sample recruitment data ────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM Recruitment)
BEGIN
    INSERT INTO Recruitment(Name,Email,Phone,Position_Applied,Department,Status,Notes) VALUES
    ('Ali Hassan',   'ali.h@gmail.com',   '0311-1111111','Senior Developer','Engineering',  'Shortlisted','Strong Java background'),
    ('Sara Malik',   'sara.m@gmail.com',  '0322-2222222','HR Coordinator',  'Human Resources','Interview', 'Interview on 2025-06-10'),
    ('Usman Khan',   'usman.k@gmail.com', '0333-3333333','Accountant',      'Finance',       'Applied',   'Fresh graduate'),
    ('Nadia Iqbal',  'nadia.i@gmail.com', '0344-4444444','Marketing Exec',  'Marketing',     'Hired',     'Joining 2025-06-15'),
    ('Kamran Ali',   'kamran.a@gmail.com','0355-5555555','IT Support',      'IT Support',    'Rejected',  'Did not pass technical round');
END
GO

-- ── 9. Update sample employees with new fields ───────────────────────────────
UPDATE Employees SET Gender='Male',   Contract_Type='Permanent', Role_Level='Team Lead'   WHERE Emp_ID=1;
UPDATE Employees SET Gender='Female', Contract_Type='Permanent', Role_Level='HR Manager'  WHERE Emp_ID=2;
UPDATE Employees SET Gender='Male',   Contract_Type='Contract',  Role_Level='Accountant'  WHERE Emp_ID=3;
UPDATE Employees SET Gender='Female', Contract_Type='Permanent', Role_Level='Director'    WHERE Emp_ID=4;
UPDATE Employees SET Gender='Male',   Contract_Type='Intern',    Role_Level='Employee'    WHERE Emp_ID=5;
UPDATE Employees SET Gender='Female', Contract_Type='Permanent', Role_Level='Employee'    WHERE Emp_ID=6;
UPDATE Employees SET Tax_Percent=5.0 WHERE Emp_ID IN (1,2,4);
UPDATE Employees SET Tax_Percent=2.5 WHERE Emp_ID IN (3,5,6);
GO

PRINT '============================================';
PRINT 'HRMS v4 schema update complete!';
PRINT '';
PRINT 'New login credentials:';
PRINT '  HR Manager  -> hrmanager / hr123';
PRINT '  Team Lead   -> teamlead  / tl123';
PRINT '  Accountant  -> accountant / ac123';
PRINT '  Director    -> director  / dir123';
PRINT '============================================';
GO
