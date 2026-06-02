package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.Employee;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    private static final String SELECT_BASE =
        "SELECT e.Emp_ID,e.Emp_Name,e.Dept_ID,d.Dept_Name,e.Joining_Date,"
      + "e.Email,e.Phone,e.Position,e.Status,"
      + "ISNULL(e.Gender,'') AS Gender,"
      + "ISNULL(e.CNIC,'') AS CNIC,"
      + "ISNULL(e.Address,'') AS Address,"
      + "ISNULL(e.Contract_Type,'Permanent') AS Contract_Type,"
      + "ISNULL(e.Role_Level,'Employee') AS Role_Level,"
      + "ISNULL(e.Photo_Path,'') AS Photo_Path,"
      + "ISNULL(e.Emergency_Contact,'') AS Emergency_Contact,"
      + "ISNULL(e.Overtime_Hours,0) AS Overtime_Hours,"
      + "ISNULL(e.Tax_Percent,0) AS Tax_Percent "
      + "FROM Employees e JOIN Departments d ON e.Dept_ID=d.Dept_ID ";

    // ── Supported date formats (tries each in order) ──────────────────────────
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    };

    /**
     * Converts a date string in any common format to java.sql.Date.
     * Falls back to today's date if parsing fails.
     */
    private java.sql.Date parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return java.sql.Date.valueOf(LocalDate.now());
        }
        dateStr = dateStr.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return java.sql.Date.valueOf(LocalDate.parse(dateStr, fmt));
            } catch (DateTimeParseException ignored) {}
        }
        // Last resort: try direct valueOf (requires "yyyy-MM-dd")
        try {
            return java.sql.Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            System.err.println("[EmployeeDAO] Could not parse date: '" + dateStr
                + "'. Using today's date as fallback.");
            return java.sql.Date.valueOf(LocalDate.now());
        }
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY e.Emp_ID")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] getAllEmployees failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return list;
    }

    public Employee getById(int id) {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(SELECT_BASE + "WHERE e.Emp_ID=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] getById failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public List<Employee> searchByName(String kw) {
        List<Employee> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(SELECT_BASE + "WHERE e.Emp_Name LIKE ?")) {
            ps.setString(1, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] searchByName failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Inserts a new employee record.
     * FIX: Joining_Date is now set using setDate() instead of setString()
     *      to avoid SQL Server date format mismatch errors.
     */
    public boolean addEmployee(Employee e) {
        String sql = "INSERT INTO Employees "
            + "(Emp_Name, Dept_ID, Joining_Date, Email, Phone, Position, Status, "
            + " Gender, CNIC, Address, Contract_Type, Role_Level, Photo_Path, "
            + " Emergency_Contact, Overtime_Hours, Tax_Percent) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1,  nullSafe(e.getEmpName()));
            ps.setInt   (2,  e.getDeptId());
            ps.setDate  (3,  parseDateSafe(e.getJoiningDate()));          // FIXED
            ps.setString(4,  nullSafe(e.getEmail()));
            ps.setString(5,  nullSafe(e.getPhone()));
            ps.setString(6,  nullSafe(e.getPosition()));
            ps.setString(7,  e.getStatus() != null ? e.getStatus() : "Active");
            ps.setString(8,  nullSafe(e.getGender()));
            ps.setString(9,  nullSafe(e.getCnic()));
            ps.setString(10, nullSafe(e.getAddress()));
            ps.setString(11, e.getContractType()     != null ? e.getContractType()     : "Permanent");
            ps.setString(12, e.getRoleLevel()         != null ? e.getRoleLevel()         : "Employee");
            ps.setString(13, e.getPhotoPath()         != null ? e.getPhotoPath()         : "");
            ps.setString(14, e.getEmergencyContact()  != null ? e.getEmergencyContact()  : "");
            ps.setDouble(15, e.getOvertimeHours());
            ps.setDouble(16, e.getTaxPercent());

            int rows = ps.executeUpdate();
            System.out.println("[EmployeeDAO] addEmployee: " + rows + " row(s) inserted.");
            return rows > 0;

        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] addEmployee FAILED: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployee(Employee e) {
        String sql = "UPDATE Employees SET "
            + "Emp_Name=?, Dept_ID=?, Email=?, Phone=?, Position=?, Status=?, "
            + "Gender=?, CNIC=?, Address=?, Contract_Type=?, Role_Level=?, "
            + "Photo_Path=?, Emergency_Contact=?, Overtime_Hours=?, Tax_Percent=? "
            + "WHERE Emp_ID=?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1,  nullSafe(e.getEmpName()));
            ps.setInt   (2,  e.getDeptId());
            ps.setString(3,  nullSafe(e.getEmail()));
            ps.setString(4,  nullSafe(e.getPhone()));
            ps.setString(5,  nullSafe(e.getPosition()));
            ps.setString(6,  e.getStatus() != null ? e.getStatus() : "Active");
            ps.setString(7,  nullSafe(e.getGender()));
            ps.setString(8,  nullSafe(e.getCnic()));
            ps.setString(9,  nullSafe(e.getAddress()));
            ps.setString(10, e.getContractType()    != null ? e.getContractType()    : "Permanent");
            ps.setString(11, e.getRoleLevel()        != null ? e.getRoleLevel()        : "Employee");
            ps.setString(12, e.getPhotoPath()        != null ? e.getPhotoPath()        : "");
            ps.setString(13, e.getEmergencyContact() != null ? e.getEmergencyContact() : "");
            ps.setDouble(14, e.getOvertimeHours());
            ps.setDouble(15, e.getTaxPercent());
            ps.setInt   (16, e.getEmpId());

            int rows = ps.executeUpdate();
            System.out.println("[EmployeeDAO] updateEmployee: " + rows + " row(s) updated.");
            return rows > 0;

        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] updateEmployee FAILED: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updatePhotoPath(int empId, String path) {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE Employees SET Photo_Path=? WHERE Emp_ID=?")) {
            ps.setString(1, path != null ? path : "");
            ps.setInt   (2, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] updatePhotoPath FAILED: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployee(int id) {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("DELETE FROM Employees WHERE Emp_ID=?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[EmployeeDAO] deleteEmployee: " + rows + " row(s) deleted.");
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("[EmployeeDAO] deleteEmployee FAILED: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // ── Analytics queries ─────────────────────────────────────────────────────

    /** Returns [Male, Female, Other] counts */
    public int[] getGenderDistribution() {
        int[] g = {0, 0, 0};
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT ISNULL(Gender,'Other') AS G, COUNT(*) AS C "
             + "FROM Employees GROUP BY Gender")) {
            while (rs.next()) {
                switch (rs.getString("G")) {
                    case "Male"   -> g[0] = rs.getInt("C");
                    case "Female" -> g[1] = rs.getInt("C");
                    default       -> g[2] += rs.getInt("C");
                }
            }
        } catch (Exception ex) {
            System.err.println("[EmployeeDAO] getGenderDistribution failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return g;
    }

    /** Returns role level distribution: [[roleLevel, count], ...] */
    public List<Object[]> getRoleLevelDistribution() {
        List<Object[]> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT ISNULL(Role_Level,'Employee') AS R, COUNT(*) AS C "
             + "FROM Employees GROUP BY Role_Level ORDER BY C DESC")) {
            while (rs.next())
                list.add(new Object[]{rs.getString("R"), rs.getInt("C")});
        } catch (Exception ex) {
            System.err.println("[EmployeeDAO] getRoleLevelDistribution failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return list;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Returns empty string instead of null to avoid NullPointerException on setString */
    private String nullSafe(String val) {
        return val != null ? val : "";
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmpId       (rs.getInt   ("Emp_ID"));
        e.setEmpName     (rs.getString("Emp_Name"));
        e.setDeptId      (rs.getInt   ("Dept_ID"));
        e.setDeptName    (rs.getString("Dept_Name"));
        e.setJoiningDate (rs.getString("Joining_Date"));
        e.setEmail       (rs.getString("Email"));
        e.setPhone       (rs.getString("Phone"));
        e.setPosition    (rs.getString("Position"));
        e.setStatus      (rs.getString("Status"));
        try { e.setGender           (rs.getString("Gender"));            } catch (Exception ignored) {}
        try { e.setCnic             (rs.getString("CNIC"));              } catch (Exception ignored) {}
        try { e.setAddress          (rs.getString("Address"));           } catch (Exception ignored) {}
        try { e.setContractType     (rs.getString("Contract_Type"));     } catch (Exception ignored) {}
        try { e.setRoleLevel        (rs.getString("Role_Level"));        } catch (Exception ignored) {}
        try { e.setPhotoPath        (rs.getString("Photo_Path"));        } catch (Exception ignored) {}
        try { e.setEmergencyContact (rs.getString("Emergency_Contact")); } catch (Exception ignored) {}
        try { e.setOvertimeHours    (rs.getDouble("Overtime_Hours"));    } catch (Exception ignored) {}
        try { e.setTaxPercent       (rs.getDouble("Tax_Percent"));       } catch (Exception ignored) {}
        return e;
    }
}