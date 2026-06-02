package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.Candidate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecruitmentDAO {

    public List<Candidate> getAll() {
        List<Candidate> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT * FROM Recruitment ORDER BY Candidate_ID DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    public List<Candidate> getByStatus(String status) {
        List<Candidate> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM Recruitment WHERE Status=? ORDER BY Candidate_ID DESC")) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    public boolean add(Candidate c) {
        String sql = "INSERT INTO Recruitment (Name,Email,Phone,Position_Applied,Department,Status,"
                   + "Applied_Date,Interview_Date,Notes) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getPosition());
            ps.setString(5, c.getDepartment());
            ps.setString(6, c.getStatus() != null ? c.getStatus() : "Applied");
            ps.setString(7, c.getAppliedDate());
            ps.setString(8, c.getInterviewDate());
            ps.setString(9, c.getNotes());
            return ps.executeUpdate() > 0;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public boolean updateStatus(int id, String status, String interviewDate, String notes) {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE Recruitment SET Status=?,Interview_Date=?,Notes=? WHERE Candidate_ID=?")) {
            ps.setString(1, status);
            ps.setString(2, interviewDate);
            ps.setString(3, notes);
            ps.setInt   (4, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("DELETE FROM Recruitment WHERE Candidate_ID=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public int[] getPipelineCounts() {
        // Returns [Applied, Shortlisted, Interview, Hired, Rejected]
        int[] counts = new int[5];
        String[] stages = {"Applied","Shortlisted","Interview","Hired","Rejected"};
        try (Connection c = DBConnection.getConnection()) {
            for (int i = 0; i < stages.length; i++) {
                PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM Recruitment WHERE Status=?");
                ps.setString(1, stages[i]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) counts[i] = rs.getInt(1);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return counts;
    }

    private Candidate map(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId  (rs.getInt   ("Candidate_ID"));
        c.setName         (rs.getString("Name"));
        c.setEmail        (rs.getString("Email"));
        c.setPhone        (rs.getString("Phone"));
        c.setPosition     (rs.getString("Position_Applied"));
        c.setDepartment   (rs.getString("Department"));
        c.setStatus       (rs.getString("Status"));
        c.setAppliedDate  (rs.getString("Applied_Date"));
        try { c.setInterviewDate(rs.getString("Interview_Date")); } catch (Exception ignored) {}
        try { c.setNotes        (rs.getString("Notes")); }          catch (Exception ignored) {}
        return c;
    }
}
