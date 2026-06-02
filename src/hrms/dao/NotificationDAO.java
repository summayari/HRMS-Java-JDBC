package hrms.dao;

import hrms.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Generates live notifications by querying key business rules. */
public class NotificationDAO {

    public static class Notification {
        public final String icon, title, detail, type; // type: warn/info/danger/success
        public Notification(String icon, String title, String detail, String type) {
            this.icon = icon; this.title = title; this.detail = detail; this.type = type;
        }
    }

    public List<Notification> getAdminNotifications() {
        List<Notification> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'");
            if (rs.next() && rs.getInt(1) > 0)
                list.add(new Notification("📋", "Pending Leave Approvals",
                    rs.getInt(1) + " leave application(s) waiting for your decision", "warn"));

            rs = st.executeQuery(
                "SELECT COUNT(*) FROM Employees e WHERE NOT EXISTS "
              + "(SELECT 1 FROM Attendance a WHERE a.Emp_ID=e.Emp_ID AND a.Att_Date=CAST(GETDATE() AS DATE))");
            if (rs.next() && rs.getInt(1) > 0)
                list.add(new Notification("📅", "Attendance Not Marked",
                    rs.getInt(1) + " employee(s) have no attendance record for today", "danger"));

            rs = st.executeQuery(
                "SELECT COUNT(DISTINCT e.Emp_ID) FROM Employees e "
              + "WHERE NOT EXISTS (SELECT 1 FROM Salaries s WHERE s.Emp_ID=e.Emp_ID "
              + "AND s.Pay_Month LIKE '%" + java.time.LocalDate.now().getYear() + "%')");
            if (rs.next() && rs.getInt(1) > 0)
                list.add(new Notification("💰", "Salary Records Missing",
                    rs.getInt(1) + " employee(s) have no salary record this year", "warn"));

            rs = st.executeQuery("SELECT COUNT(*) FROM Employees WHERE Status='Active'");
            if (rs.next())
                list.add(new Notification("👥", "Workforce Summary",
                    rs.getInt(1) + " active employee(s) currently in the system", "info"));

            rs = st.executeQuery(
                "SELECT COUNT(*) FROM Attendance WHERE Status='Present' AND Att_Date=CAST(GETDATE() AS DATE)");
            if (rs.next())
                list.add(new Notification("✅", "Today's Attendance",
                    rs.getInt(1) + " employee(s) marked present today", "success"));

        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    public List<Notification> getEmployeeNotifications(int empId) {
        List<Notification> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps;
            ResultSet rs;

            ps = c.prepareStatement(
                "SELECT COUNT(*) FROM LeaveApplications WHERE Emp_ID=? AND Status='Pending'");
            ps.setInt(1, empId); rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                list.add(new Notification("⏳", "Leave Pending",
                    rs.getInt(1) + " of your leave applications are pending approval", "warn"));

            ps = c.prepareStatement(
                "SELECT COUNT(*) FROM LeaveApplications WHERE Emp_ID=? AND Status='Approved' "
              + "AND Start_Date >= CAST(GETDATE() AS DATE)");
            ps.setInt(1, empId); rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                list.add(new Notification("✅", "Leave Approved",
                    rs.getInt(1) + " upcoming approved leave(s)", "success"));

            ps = c.prepareStatement(
                "SELECT TOP 1 Status FROM Attendance WHERE Emp_ID=? AND Att_Date=CAST(GETDATE() AS DATE)");
            ps.setInt(1, empId); rs = ps.executeQuery();
            if (rs.next())
                list.add(new Notification("📅", "Today's Attendance",
                    "You are marked: " + rs.getString(1) + " today", "info"));
            else
                list.add(new Notification("⚠", "Attendance Missing",
                    "Your attendance has not been marked today", "danger"));

            ps = c.prepareStatement(
                "SELECT TOP 1 Net_Salary, Pay_Month FROM Salaries WHERE Emp_ID=? ORDER BY Salary_ID DESC");
            ps.setInt(1, empId); rs = ps.executeQuery();
            if (rs.next())
                list.add(new Notification("💰", "Latest Salary",
                    String.format("PKR %,.0f for %s", rs.getDouble(1), rs.getString(2)), "success"));

        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }
}
