package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.Salary;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaryDAO {

    private static final String SELECT_BASE =
        "SELECT s.Salary_ID,s.Emp_ID,e.Emp_Name,d.Dept_Name,"
      + "s.Basic_Salary,s.Bonus,"
      + "ISNULL(s.Overtime_Pay,0) AS Overtime_Pay,"
      + "ISNULL(s.Tax_Amount,0) AS Tax_Amount,"
      + "s.Deductions,s.Net_Salary,s.Pay_Month,"
      + "ISNULL(CONVERT(varchar,s.Pay_Date,23),'') AS Pay_Date "
      + "FROM Salaries s "
      + "JOIN Employees e ON s.Emp_ID=e.Emp_ID "
      + "JOIN Departments d ON e.Dept_ID=d.Dept_ID ";

    public List<Salary> getAllSalaries() {
        List<Salary> list=new ArrayList<>();
        try (Statement st=DBConnection.getConnection().createStatement();
             ResultSet rs=st.executeQuery(SELECT_BASE+"ORDER BY s.Salary_ID DESC")) {
            while(rs.next()) list.add(map(rs));
        } catch(Exception ex){ ex.printStackTrace(); }
        return list;
    }

    public List<Salary> getByEmployee(int empId) {
        List<Salary> list=new ArrayList<>();
        try (PreparedStatement ps=DBConnection.getConnection()
                .prepareStatement(SELECT_BASE+"WHERE s.Emp_ID=? ORDER BY s.Salary_ID DESC")) {
            ps.setInt(1,empId);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) list.add(map(rs)); }
        } catch(Exception ex){ ex.printStackTrace(); }
        return list;
    }

    /** Simple manual add (no OT/Tax fields). */
    public boolean addSalary(Salary s) {
        String sql="INSERT INTO Salaries(Emp_ID,Basic_Salary,Bonus,Deductions,Pay_Month) VALUES(?,?,?,?,?)";
        try(PreparedStatement ps=DBConnection.getConnection().prepareStatement(sql)){
            ps.setInt(1,s.getEmpId()); ps.setDouble(2,s.getBasicSalary());
            ps.setDouble(3,s.getBonus()); ps.setDouble(4,s.getDeductions());
            ps.setString(5,s.getPayMonth());
            return ps.executeUpdate()>0;
        } catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    /** Full add with OT/Tax (auto-calc). */
    public boolean addSalaryFull(Salary s) {
        String sql="INSERT INTO Salaries(Emp_ID,Basic_Salary,Bonus,Overtime_Pay,Tax_Amount,Deductions,Net_Salary,Pay_Month) "
                 + "VALUES(?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=DBConnection.getConnection().prepareStatement(sql)){
            ps.setInt(1,s.getEmpId()); ps.setDouble(2,s.getBasicSalary());
            ps.setDouble(3,s.getBonus()); ps.setDouble(4,s.getOvertimePay());
            ps.setDouble(5,s.getTaxAmount()); ps.setDouble(6,s.getDeductions());
            ps.setDouble(7,s.getNetSalary()); ps.setString(8,s.getPayMonth());
            return ps.executeUpdate()>0;
        } catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    public boolean updateSalary(Salary s) {
        String sql="UPDATE Salaries SET Basic_Salary=?,Bonus=?,Deductions=?,Pay_Month=? WHERE Salary_ID=?";
        try(PreparedStatement ps=DBConnection.getConnection().prepareStatement(sql)){
            ps.setDouble(1,s.getBasicSalary()); ps.setDouble(2,s.getBonus());
            ps.setDouble(3,s.getDeductions()); ps.setString(4,s.getPayMonth());
            ps.setInt(5,s.getSalaryId());
            return ps.executeUpdate()>0;
        } catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    public boolean deleteSalary(int id) {
        try(PreparedStatement ps=DBConnection.getConnection()
                .prepareStatement("DELETE FROM Salaries WHERE Salary_ID=?")){
            ps.setInt(1,id); return ps.executeUpdate()>0;
        } catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    public Object[][] getDeptStats() {
        List<Object[]> rows=new ArrayList<>();
        try(Statement st=DBConnection.getConnection().createStatement();
            ResultSet rs=st.executeQuery("SELECT * FROM vw_DeptStats")){
            while(rs.next()) rows.add(new Object[]{
                rs.getString("Dept_Name"),rs.getInt("Total_Employees"),
                String.format("PKR %,.0f",rs.getDouble("Avg_Net_Salary")),
                String.format("PKR %,.0f",rs.getDouble("Max_Salary")),
                String.format("PKR %,.0f",rs.getDouble("Min_Salary")),
                String.format("PKR %,.0f",rs.getDouble("Total_Payroll"))
            });
        } catch(Exception ex){ ex.printStackTrace(); }
        return rows.toArray(new Object[0][]);
    }

    private Salary map(ResultSet rs) throws SQLException {
        Salary s=new Salary();
        s.setSalaryId  (rs.getInt   ("Salary_ID"));
        s.setEmpId     (rs.getInt   ("Emp_ID"));
        s.setEmpName   (rs.getString("Emp_Name"));
        s.setDeptName  (rs.getString("Dept_Name"));
        s.setBasicSalary(rs.getDouble("Basic_Salary"));
        s.setBonus     (rs.getDouble("Bonus"));
        s.setOvertimePay(rs.getDouble("Overtime_Pay"));
        s.setTaxAmount (rs.getDouble("Tax_Amount"));
        s.setDeductions(rs.getDouble("Deductions"));
        s.setNetSalary (rs.getDouble("Net_Salary"));
        s.setPayMonth  (rs.getString("Pay_Month"));
        s.setPayDate   (rs.getString("Pay_Date"));
        return s;
    }
}
