package hrms.gui;

import hrms.db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Team Members view for Team Lead — shows team roster with status. */
public class TeamMembersPanel extends JPanel {
    public TeamMembersPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(Theme.pageHeader("👥","My Team","Manage and monitor your team members",Theme.TEAMLEAD_COLOR),BorderLayout.NORTH);

        String[] cols={"Emp ID","Name","Job Title","Department","Contract","Status","Join Date"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT e.Emp_ID,e.First_Name+' '+e.Last_Name,e.Job_Title,d.Dept_Name,e.Contract_Type,e.Employment_Status,FORMAT(e.Hire_Date,'dd MMM yyyy') FROM Employees e LEFT JOIN Departments d ON e.Dept_ID=d.Dept_ID ORDER BY e.Emp_ID");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)});
        }catch(Exception ex){ ex.printStackTrace(); }

        JTable t=new JTable(m); Theme.styleTable(t);
        t.getColumnModel().getColumn(5).setCellRenderer((tbl,val,sel,foc,r,c)->{
            JLabel l=new JLabel("● "+val); l.setOpaque(true);
            l.setBackground(sel?new Color(99,102,241,60):(r%2==0?Theme.CARD_BG:new Color(28,40,62)));
            l.setForeground("Active".equals(val)?Theme.ACCENT:Theme.DANGER);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));
            return l;
        });

        JPanel center=new JPanel(new BorderLayout(0,10));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14,20,20,20));

        JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); searchRow.setBackground(Theme.BG);
        JTextField sf=Theme.field(20); sf.putClientProperty("JTextField.placeholderText","🔍 Search members...");
        searchRow.add(sf);
        searchRow.add(Theme.accentBtn("🔍 Search"));
        searchRow.add(Theme.infoBtn("📤 Export CSV"));

        JPanel tableCard=Theme.card("");
        tableCard.add(Theme.scrollPane(t),BorderLayout.CENTER);

        center.add(searchRow,BorderLayout.NORTH);
        center.add(tableCard,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
    }
}
