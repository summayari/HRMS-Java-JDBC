package hrms.model;

public class Salary {
    private int    salaryId;
    private int    empId;
    private String empName;
    private String deptName;
    private double basicSalary;
    private double bonus;
    private double overtimePay;   // calculated
    private double taxAmount;     // calculated
    private double deductions;
    private double netSalary;
    private String payMonth;
    private String payDate;

    public Salary() {}

    public Salary(int empId, double basicSalary, double bonus, double deductions, String payMonth) {
        this.empId=empId; this.basicSalary=basicSalary;
        this.bonus=bonus; this.deductions=deductions; this.payMonth=payMonth;
    }

    public Salary(int salaryId, int empId, double basicSalary, double bonus, double deductions, String payMonth) {
        this.salaryId=salaryId; this.empId=empId; this.basicSalary=basicSalary;
        this.bonus=bonus; this.deductions=deductions; this.payMonth=payMonth;
    }

    public int    getSalaryId()    { return salaryId; }
    public int    getEmpId()       { return empId; }
    public String getEmpName()     { return empName; }
    public String getDeptName()    { return deptName; }
    public double getBasicSalary() { return basicSalary; }
    public double getBonus()       { return bonus; }
    public double getOvertimePay() { return overtimePay; }
    public double getTaxAmount()   { return taxAmount; }
    public double getDeductions()  { return deductions; }
    public double getNetSalary()   { return netSalary; }
    public String getPayMonth()    { return payMonth; }
    public String getPayDate()     { return payDate; }

    public void setSalaryId(int v)       { salaryId=v; }
    public void setEmpId(int v)          { empId=v; }
    public void setEmpName(String v)     { empName=v; }
    public void setDeptName(String v)    { deptName=v; }
    public void setBasicSalary(double v) { basicSalary=v; }
    public void setBonus(double v)       { bonus=v; }
    public void setOvertimePay(double v) { overtimePay=v; }
    public void setTaxAmount(double v)   { taxAmount=v; }
    public void setDeductions(double v)  { deductions=v; }
    public void setNetSalary(double v)   { netSalary=v; }
    public void setPayMonth(String v)    { payMonth=v; }
    public void setPayDate(String v)     { payDate=v; }
}
