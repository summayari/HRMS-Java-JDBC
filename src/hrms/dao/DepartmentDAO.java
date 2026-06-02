package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Departments ORDER BY Dept_ID")) {
            while (rs.next()) {
                Department d = new Department();
                d.setDeptId(rs.getInt("Dept_ID"));
                d.setDeptName(rs.getString("Dept_Name"));
                d.setManager(rs.getString("Manager"));
                d.setLocation(rs.getString("Location"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns a map of Emp_ID -> Emp_Name for all active employees.
     * Used to populate the "Select Existing Employee" dropdown in the
     * Add/Edit Department dialog.
     */
    public Map<Integer, String> getEmployeeNames() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT Emp_ID, Emp_Name FROM Employees " +
                     "WHERE Status = 'Active' ORDER BY Emp_Name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("Emp_ID"), rs.getString("Emp_Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean addDepartment(Department d) {
        String sql = "INSERT INTO Departments (Dept_Name, Manager, Location) VALUES(?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setString(2, d.getManager());
            ps.setString(3, d.getLocation());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDepartment(Department d) {
        String sql = "UPDATE Departments SET Dept_Name=?, Manager=?, Location=? WHERE Dept_ID=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setString(2, d.getManager());
            ps.setString(3, d.getLocation());
            ps.setInt(4, d.getDeptId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM Departments WHERE Dept_ID=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
