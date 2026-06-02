package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.LeaveApplication;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveDAO {

    public boolean applyLeave(LeaveApplication l) {
        String sql = "INSERT INTO LeaveApplications (Emp_ID,Leave_Type,Start_Date,End_Date,Reason) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, l.getEmpId());
            ps.setString(2, l.getLeaveType());
            ps.setString(3, l.getStartDate());
            ps.setString(4, l.getEndDate());
            ps.setString(5, l.getReason());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public List<LeaveApplication> getAll() {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT la.*,e.Emp_Name FROM LeaveApplications la "
                   + "JOIN Employees e ON la.Emp_ID=e.Emp_ID ORDER BY la.Applied_On DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public List<LeaveApplication> getByEmployee(int empId) {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT la.*,e.Emp_Name FROM LeaveApplications la "
                   + "JOIN Employees e ON la.Emp_ID=e.Emp_ID WHERE la.Emp_ID=? ORDER BY la.Applied_On DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(int leaveId, String status) {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE LeaveApplications SET Status=? WHERE Leave_ID=?")) {
            ps.setString(1, status);
            ps.setInt   (2, leaveId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    private LeaveApplication map(ResultSet rs) throws SQLException {
        LeaveApplication l = new LeaveApplication();
        l.setLeaveId  (rs.getInt   ("Leave_ID"));
        l.setEmpId    (rs.getInt   ("Emp_ID"));
        l.setEmpName  (rs.getString("Emp_Name"));
        l.setLeaveType(rs.getString("Leave_Type"));
        l.setStartDate(rs.getString("Start_Date"));
        l.setEndDate  (rs.getString("End_Date"));
        l.setReason   (rs.getString("Reason"));
        l.setStatus   (rs.getString("Status"));
        l.setAppliedOn(rs.getString("Applied_On"));
        return l;
    }
}
