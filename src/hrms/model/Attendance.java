package hrms.model;

public class Attendance {
    private int    attId;
    private int    empId;
    private String empName;
    private String attDate;
    private String status;

    public Attendance() {}
    public Attendance(int empId, String attDate, String status) {
        this.empId = empId; this.attDate = attDate; this.status = status;
    }

    public int    getAttId()   { return attId; }
    public int    getEmpId()   { return empId; }
    public String getEmpName() { return empName; }
    public String getAttDate() { return attDate; }
    public String getStatus()  { return status; }

    public void setAttId(int v)     { attId = v; }
    public void setEmpId(int v)     { empId = v; }
    public void setEmpName(String v){ empName = v; }
    public void setAttDate(String v){ attDate = v; }
    public void setStatus(String v) { status = v; }
}
