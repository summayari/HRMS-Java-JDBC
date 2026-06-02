package hrms.model;

public class Candidate {
    private int    candidateId;
    private String name;
    private String email;
    private String phone;
    private String position;     // position applied for
    private String department;
    private String status;       // Applied / Shortlisted / Interview / Hired / Rejected
    private String appliedDate;
    private String interviewDate;
    private String notes;
    private int    postedBy;     // Emp_ID of HR who added

    public Candidate() {}

    public int    getCandidateId()  { return candidateId; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getPosition()      { return position; }
    public String getDepartment()    { return department; }
    public String getStatus()        { return status; }
    public String getAppliedDate()   { return appliedDate; }
    public String getInterviewDate() { return interviewDate; }
    public String getNotes()         { return notes; }
    public int    getPostedBy()      { return postedBy; }

    public void setCandidateId(int v)    { candidateId = v; }
    public void setName(String v)         { name = v; }
    public void setEmail(String v)        { email = v; }
    public void setPhone(String v)        { phone = v; }
    public void setPosition(String v)     { position = v; }
    public void setDepartment(String v)   { department = v; }
    public void setStatus(String v)       { status = v; }
    public void setAppliedDate(String v)  { appliedDate = v; }
    public void setInterviewDate(String v){ interviewDate = v; }
    public void setNotes(String v)        { notes = v; }
    public void setPostedBy(int v)        { postedBy = v; }
}
