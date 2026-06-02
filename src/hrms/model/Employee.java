package hrms.model;

public class Employee {
    private int    empId;
    private String empName;
    private int    deptId;
    private String deptName;
    private String joiningDate;
    private String email;
    private String phone;
    private String position;
    private String status;
    // New fields
    private String gender;        // Male / Female / Other
    private String cnic;          // National ID
    private String address;
    private String contractType;  // Permanent / Contract / Intern
    private String roleLevel;     // Admin / HR Manager / Team Lead / Accountant / Director / Employee
    private String photoPath;     // local file path to profile photo
    private String emergencyContact;
    private double overtimeHours;
    private double taxPercent;    // e.g. 5.0 = 5%

    public Employee() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getEmpId()            { return empId; }
    public String getEmpName()          { return empName; }
    public int    getDeptId()           { return deptId; }
    public String getDeptName()         { return deptName; }
    public String getJoiningDate()      { return joiningDate; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public String getPosition()         { return position; }
    public String getStatus()           { return status; }
    public String getGender()           { return gender; }
    public String getCnic()             { return cnic; }
    public String getAddress()          { return address; }
    public String getContractType()     { return contractType; }
    public String getRoleLevel()        { return roleLevel; }
    public String getPhotoPath()        { return photoPath; }
    public String getEmergencyContact() { return emergencyContact; }
    public double getOvertimeHours()    { return overtimeHours; }
    public double getTaxPercent()       { return taxPercent; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setEmpId(int v)               { empId = v; }
    public void setEmpName(String v)          { empName = v; }
    public void setDeptId(int v)              { deptId = v; }
    public void setDeptName(String v)         { deptName = v; }
    public void setJoiningDate(String v)      { joiningDate = v; }
    public void setEmail(String v)            { email = v; }
    public void setPhone(String v)            { phone = v; }
    public void setPosition(String v)         { position = v; }
    public void setStatus(String v)           { status = v; }
    public void setGender(String v)           { gender = v; }
    public void setCnic(String v)             { cnic = v; }
    public void setAddress(String v)          { address = v; }
    public void setContractType(String v)     { contractType = v; }
    public void setRoleLevel(String v)        { roleLevel = v; }
    public void setPhotoPath(String v)        { photoPath = v; }
    public void setEmergencyContact(String v) { emergencyContact = v; }
    public void setOvertimeHours(double v)    { overtimeHours = v; }
    public void setTaxPercent(double v)       { taxPercent = v; }
}
