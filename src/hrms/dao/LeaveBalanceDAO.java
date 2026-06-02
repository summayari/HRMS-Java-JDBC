package hrms.dao;

import hrms.db.DBConnection;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeaveBalanceDAO {

    /** Returns Map of LeaveType -> {quota, used, remaining} for an employee in current year. */
    public Map<String, int[]> getBalance(int empId) {
        Map<String, int[]> map = new LinkedHashMap<>();
        // Default quotas
        map.put("Casual",  new int[]{15, 0, 15});
        map.put("Medical", new int[]{10, 0, 10});
        map.put("Annual",  new int[]{21, 0, 21});
        map.put("Unpaid",  new int[]{999, 0, 999});

        String sql = "SELECT Leave_Type, "
                   + "SUM(DATEDIFF(DAY, Start_Date, End_Date)+1) AS Days_Used "
                   + "FROM LeaveApplications "
                   + "WHERE Emp_ID=? AND Status='Approved' "
                   + "AND YEAR(Start_Date)=YEAR(GETDATE()) "
                   + "GROUP BY Leave_Type";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString(1);
                int used = rs.getInt(2);
                if (map.containsKey(type)) {
                    int quota = map.get(type)[0];
                    map.put(type, new int[]{quota, used, Math.max(0, quota - used)});
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return map;
    }

    /** Total approved leave days this year for an employee. */
    public int getTotalUsed(int empId) {
        String sql = "SELECT ISNULL(SUM(DATEDIFF(DAY,Start_Date,End_Date)+1),0) "
                   + "FROM LeaveApplications WHERE Emp_ID=? AND Status='Approved' "
                   + "AND YEAR(Start_Date)=YEAR(GETDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }
}
