package hrms.gui;

import hrms.db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Financial Reports panel for Accountant role. */
public class FinancialReportsPanel extends JPanel {
    public FinancialReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(Theme.pageHeader("📊","Financial Reports","Payroll summaries, department cost analysis",Theme.ACCOUNTANT_COLOR),BorderLayout.NORTH);

        JPanel center=new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14,20,20,20));

        // Dept stats table
        String[] cols={"Department","Employees","Avg Salary","Max Salary","Min Salary","Total Payroll"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT Dept_Name,Total_Employees,CAST(Avg_Net_Salary AS INT),CAST(Max_Salary AS INT),CAST(Min_Salary AS INT),CAST(Total_Payroll AS INT) FROM vw_DeptStats ORDER BY Total_Payroll DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),"₨"+rs.getString(3),"₨"+rs.getString(4),"₨"+rs.getString(5),"₨"+rs.getString(6)});
        }catch(Exception ex){ ex.printStackTrace(); }

        JTable t=new JTable(m); Theme.styleTable(t);
        JPanel card=Theme.card("📋 Department-wise Payroll Report");
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)); btnRow.setBackground(Theme.BG);
        btnRow.add(Theme.accentBtn("📤 Export CSV"));
        btnRow.add(Theme.primaryBtn("🖨️ Print Report"));
        btnRow.add(Theme.infoBtn("📧 Email Report"));

        center.add(btnRow,BorderLayout.NORTH);
        center.add(card,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
    }
}
