-- ============================================================
--  HRMS v5 Schema Update — Role-Based Access
--  Run AFTER HRMS_Database_v4.sql
-- ============================================================
USE HRMS_DB;
GO

-- ── 1. Expenses table ─────────────────────────────────────────────────────────
IF OBJECT_ID('Expenses','U') IS NULL
BEGIN
    CREATE TABLE Expenses (
        Expense_ID    INT          IDENTITY(1,1) PRIMARY KEY,
        Category      VARCHAR(50)  NOT NULL CHECK (Category IN ('Payroll','Bonuses','Overtime','Utilities','Equipment','Travel','Training','Other')),
        Description   VARCHAR(300) NULL,
        Amount        DECIMAL(14,2) NOT NULL,
        Expense_Date  DATE          NOT NULL DEFAULT CAST(GETDATE() AS DATE),
        Recorded_By   VARCHAR(50)   NOT NULL DEFAULT 'system',
        Status        VARCHAR(20)   NOT NULL DEFAULT 'Pending' CHECK (Status IN ('Pending','Approved','Rejected','Processed')),
        Notes         VARCHAR(500)  NULL,
        Created_At    DATETIME      DEFAULT GETDATE()
    );
    PRINT 'Expenses table created.';
END
GO

-- ── 2. Team_Members table (for Team Lead scoping) ─────────────────────────────
IF OBJECT_ID('TeamAssignments','U') IS NULL
BEGIN
    CREATE TABLE TeamAssignments (
        Assignment_ID INT IDENTITY(1,1) PRIMARY KEY,
        Lead_Emp_ID   INT NOT NULL REFERENCES Employees(Emp_ID),
        Member_Emp_ID INT NOT NULL REFERENCES Employees(Emp_ID),
        Assigned_Date DATE DEFAULT CAST(GETDATE() AS DATE)
    );
    PRINT 'TeamAssignments table created.';
END
GO

-- ── 3. Update role check to include all v5 roles ─────────────────────────────
DECLARE @con NVARCHAR(200)
SELECT @con=name FROM sys.check_constraints
WHERE parent_object_id=OBJECT_ID('SystemUsers') AND name LIKE '%Role%'
IF @con IS NOT NULL EXEC('ALTER TABLE SystemUsers DROP CONSTRAINT '+@con)
ALTER TABLE SystemUsers ADD CONSTRAINT CK_Users_Role_v5
    CHECK (Role IN ('Admin','Employee','HR Manager','Team Lead','Accountant','Director'));
GO

-- ── 4. Sample expenses ────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM Expenses)
BEGIN
    INSERT INTO Expenses(Category,Description,Amount,Expense_Date,Recorded_By,Status) VALUES
    ('Payroll',   'March 2025 Salaries',          2400000.00, '2025-03-31', 'accountant', 'Processed'),
    ('Bonuses',   'Q1 2025 Performance Bonuses',    120000.00, '2025-03-31', 'accountant', 'Processed'),
    ('Overtime',  'March 2025 Overtime Pay',         45000.00, '2025-03-31', 'accountant', 'Processed'),
    ('Utilities', 'Office Electricity & Internet',   22000.00, '2025-03-28', 'admin',      'Approved'),
    ('Equipment', 'Developer Workstations x4',      185000.00, '2025-03-15', 'admin',      'Approved'),
    ('Training',  'AWS Certification Program',       35000.00, '2025-03-20', 'hrmanager',  'Pending'),
    ('Travel',    'Client Visit Lahore-Karachi',      18500.00, '2025-03-10', 'director',   'Processed');
END
GO

-- ── 5. Role-based view for Accountant ────────────────────────────────────────
CREATE OR ALTER VIEW vw_PayrollSummary AS
SELECT
    DATENAME(MONTH,Pay_Date)+' '+CAST(YEAR(Pay_Date) AS VARCHAR) AS Pay_Period,
    COUNT(*)                                                       AS Employees_Paid,
    SUM(Basic_Salary)                                              AS Total_Basic,
    SUM(Bonus)                                                     AS Total_Bonuses,
    SUM(Overtime_Pay)                                              AS Total_Overtime,
    SUM(Tax_Amount)                                                AS Total_Tax,
    SUM(Deductions)                                                AS Total_Deductions,
    SUM(Net_Salary)                                                AS Total_Net
FROM Salaries
GROUP BY DATENAME(MONTH,Pay_Date), YEAR(Pay_Date), MONTH(Pay_Date)
ORDER BY YEAR(Pay_Date) DESC, MONTH(Pay_Date) DESC
OFFSET 0 ROWS;
GO

PRINT '============================================';
PRINT 'HRMS v5 schema update complete!';
PRINT '';
PRINT 'All role credentials:';
PRINT '  Admin       -> admin      / admin123';
PRINT '  Director    -> director   / dir123';
PRINT '  HR Manager  -> hrmanager  / hr123';
PRINT '  Team Lead   -> teamlead   / tl123';
PRINT '  Accountant  -> accountant / ac123';
PRINT '  Employee    -> employee1  / emp123';
PRINT '============================================';
GO
