package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.Attendance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean markAttendance(Attendance a) {
        String sql = "MERGE Attendance AS t "
                   + "USING (VALUES(?,?,?)) AS s(Emp_ID,Att_Date,Status) "
                   + "ON t.Emp_ID=s.Emp_ID AND t.Att_Date=s.Att_Date "
                   + "WHEN MATCHED THEN UPDATE SET t.Status=s.Status "
                   + "WHEN NOT MATCHED THEN INSERT(Emp_ID,Att_Date,Status) VALUES(s.Emp_ID,s.Att_Date,s.Status);";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, a.getEmpId());
            ps.setString(2, a.getAttDate());
            ps.setString(3, a.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public List<Attendance> getByEmployee(int empId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*,e.Emp_Name FROM Attendance a "
                   + "JOIN Employees e ON a.Emp_ID=e.Emp_ID WHERE a.Emp_ID=? ORDER BY a.Att_Date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public List<Attendance> getByDate(String date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*,e.Emp_Name FROM Attendance a "
                   + "JOIN Employees e ON a.Emp_ID=e.Emp_ID WHERE a.Att_Date=? ORDER BY e.Emp_Name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public List<Attendance> getAll() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*,e.Emp_Name FROM Attendance a "
                   + "JOIN Employees e ON a.Emp_ID=e.Emp_ID ORDER BY a.Att_Date DESC, e.Emp_Name";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttId  (rs.getInt   ("Att_ID"));
        a.setEmpId  (rs.getInt   ("Emp_ID"));
        a.setEmpName(rs.getString("Emp_Name"));
        a.setAttDate(rs.getString("Att_Date"));
        a.setStatus (rs.getString("Status"));
        return a;
    }
}
