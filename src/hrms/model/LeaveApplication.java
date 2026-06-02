package hrms.model;

public class LeaveApplication {
    private int    leaveId;
    private int    empId;
    private String empName;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String status;
    private String appliedOn;

    public LeaveApplication() {}
    public LeaveApplication(int empId, String leaveType, String startDate, String endDate, String reason) {
        this.empId = empId; this.leaveType = leaveType;
        this.startDate = startDate; this.endDate = endDate; this.reason = reason;
    }

    public int    getLeaveId()  { return leaveId; }
    public int    getEmpId()    { return empId; }
    public String getEmpName()  { return empName; }
    public String getLeaveType(){ return leaveType; }
    public String getStartDate(){ return startDate; }
    public String getEndDate()  { return endDate; }
    public String getReason()   { return reason; }
    public String getStatus()   { return status; }
    public String getAppliedOn(){ return appliedOn; }

    public void setLeaveId(int v)     { leaveId = v; }
    public void setEmpId(int v)       { empId = v; }
    public void setEmpName(String v)  { empName = v; }
    public void setLeaveType(String v){ leaveType = v; }
    public void setStartDate(String v){ startDate = v; }
    public void setEndDate(String v)  { endDate = v; }
    public void setReason(String v)   { reason = v; }
    public void setStatus(String v)   { status = v; }
    public void setAppliedOn(String v){ appliedOn = v; }
}
