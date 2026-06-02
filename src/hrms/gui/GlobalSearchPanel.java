package hrms.gui;

import hrms.db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GlobalSearchPanel extends JPanel {

    private final String query;

    public GlobalSearchPanel(String query) {
        this.query = query;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_SECONDARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel("🔍  Search Results for: \"" + query + "\"");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        if (query.isEmpty()) {
            JLabel hint = new JLabel("Type a name, department, or keyword in the top search bar and press Enter.", SwingConstants.CENTER);
            hint.setForeground(Theme.TEXT_MUTED); hint.setFont(Theme.FONT_BODY);
            add(hint, BorderLayout.CENTER);
            return;
        }

        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(Theme.BG);
        results.setBorder(BorderFactory.createEmptyBorder(14, 20, 20, 20));

        results.add(buildSection("👥 Employees", searchEmployees()));
        results.add(Box.createVerticalStrut(16));
        results.add(buildSection("📋 Leave Applications", searchLeaves()));
        results.add(Box.createVerticalStrut(16));
        results.add(buildSection("💰 Salary Records", searchSalaries()));

        JScrollPane sp = new JScrollPane(results);
        sp.setBorder(null); sp.getViewport().setBackground(Theme.BG);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildSection(String title, DefaultTableModel model) {
        JPanel section = Theme.card();
        section.setLayout(new BorderLayout(0, 8));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel lbl = new JLabel(title + "  (" + model.getRowCount() + " found)");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        section.add(lbl, BorderLayout.NORTH);

        if (model.getRowCount() == 0) {
            JLabel none = new JLabel("  No results found.");
            none.setFont(Theme.FONT_SMALL); none.setForeground(Theme.TEXT_MUTED);
            section.add(none, BorderLayout.CENTER);
        } else {
            JTable t = new JTable(model);
            Theme.styleTable(t);
            t.setPreferredScrollableViewportSize(new Dimension(0, 130));
            section.add(Theme.scrollPane(t), BorderLayout.CENTER);
        }
        return section;
    }

    private DefaultTableModel searchEmployees() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Name","Department","Position","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT e.Emp_ID,e.Emp_Name,d.Dept_Name,e.Position,e.Status "
                   + "FROM Employees e JOIN Departments d ON e.Dept_ID=d.Dept_ID "
                   + "WHERE e.Emp_Name LIKE ? OR d.Dept_Name LIKE ? OR e.Position LIKE ? OR e.Email LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + query + "%";
            ps.setString(1,p); ps.setString(2,p); ps.setString(3,p); ps.setString(4,p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)});
        } catch (Exception ex) { ex.printStackTrace(); }
        return m;
    }

    private DefaultTableModel searchLeaves() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Employee","Type","Dates","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT la.Leave_ID,e.Emp_Name,la.Leave_Type,"
                   + "CONVERT(varchar,la.Start_Date,23)+' to '+CONVERT(varchar,la.End_Date,23),la.Status "
                   + "FROM LeaveApplications la JOIN Employees e ON la.Emp_ID=e.Emp_ID "
                   + "WHERE e.Emp_Name LIKE ? OR la.Leave_Type LIKE ? OR la.Reason LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + query + "%";
            ps.setString(1,p); ps.setString(2,p); ps.setString(3,p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)});
        } catch (Exception ex) { ex.printStackTrace(); }
        return m;
    }

    private DefaultTableModel searchSalaries() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Employee","Pay Month","Net Salary"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT s.Salary_ID,e.Emp_Name,s.Pay_Month,"
                   + "CAST(s.Net_Salary AS varchar) "
                   + "FROM Salaries s JOIN Employees e ON s.Emp_ID=e.Emp_ID "
                   + "WHERE e.Emp_Name LIKE ? OR s.Pay_Month LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + query + "%";
            ps.setString(1,p); ps.setString(2,p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),
                String.format("PKR %,.0f", rs.getDouble(4))});
        } catch (Exception ex) { ex.printStackTrace(); }
        return m;
    }
}
