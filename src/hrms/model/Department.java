package hrms.model;

public class Department {
    private int    deptId;
    private String deptName;
    private String manager;
    private String location;

    public Department() {}
    public Department(String deptName, String manager, String location) {
        this.deptName = deptName; this.manager = manager; this.location = location;
    }

    public int    getDeptId()   { return deptId; }
    public String getDeptName() { return deptName; }
    public String getManager()  { return manager; }
    public String getLocation() { return location; }

    public void setDeptId(int v)      { deptId = v; }
    public void setDeptName(String v) { deptName = v; }
    public void setManager(String v)  { manager = v; }
    public void setLocation(String v) { location = v; }

    @Override public String toString() { return deptId + " – " + deptName; }
}
