package hrms.model;

public class User {
    private final int    userId;
    private final String username;
    private final String role;
    private final int    empId;

    public User(int userId, String username, String role, int empId) {
        this.userId=userId; this.username=username;
        this.role=role; this.empId=empId;
    }

    public int    getUserId()   { return userId; }
    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public int    getEmpId()    { return empId; }

    /** Admin has full access; others have limited role-based access. */
    public boolean isAdmin() { return "Admin".equalsIgnoreCase(role); }

    public boolean isHRManager()  { return "HR Manager".equalsIgnoreCase(role) || isAdmin(); }
    public boolean isTeamLead()   { return "Team Lead".equalsIgnoreCase(role)  || isAdmin(); }
    public boolean isAccountant() { return "Accountant".equalsIgnoreCase(role) || isAdmin(); }
    public boolean isDirector()   { return "Director".equalsIgnoreCase(role)   || isAdmin(); }

    @Override public String toString() { return username+" ("+role+")"; }
}
