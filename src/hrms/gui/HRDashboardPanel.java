package hrms.gui;

import hrms.auth.SessionManager;
import hrms.db.DBConnection;
import hrms.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** HR Manager Dashboard — people ops, recruitment, leave management. */
public class HRDashboardPanel extends JPanel {

    public HRDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16,20,20,20));

        int[] stats = getStats();
        JPanel row=new JPanel(new GridLayout(1,4,12,0)); row.setBackground(Theme.BG);
        row.add(Theme.statCard("Total Employees",   String.valueOf(stats[0]), Theme.HR_COLOR));
        row.add(Theme.statCard("Leave Pending",     String.valueOf(stats[1]), Theme.DANGER));
        row.add(Theme.statCard("New Recruits",      String.valueOf(stats[2]), Theme.ACCENT));
        row.add(Theme.statCard("Absent Today",      String.valueOf(stats[3]), Theme.ACCOUNTANT_COLOR));
        center.add(row, BorderLayout.NORTH);

        JPanel mid=new JPanel(new GridLayout(1,3,14,0)); mid.setBackground(Theme.BG);
        mid.add(buildPendingLeaves());
        mid.add(buildRecruitmentPipeline());
        mid.add(buildHRActions());
        center.add(mid, BorderLayout.CENTER);

        center.add(buildTodayAttendanceSummary(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(29,78,216),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,5));
                for(int x=0;x<getWidth();x+=30) for(int y=0;y<getHeight();y+=30) g2.fillOval(x,y,2,2);
                g2.setColor(Theme.HR_COLOR); g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0,100));
        User u=SessionManager.getInstance().getUser();
        int hour=java.time.LocalTime.now().getHour();
        String greet=hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel t=new JLabel("🧑‍💼 "+greet+", "+(u!=null?u.getUsername():"HR Manager"));
        t.setFont(new Font("Segoe UI",Font.BOLD,24)); t.setForeground(Color.WHITE);
        JLabel s=new JLabel("HR Operations Center — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        s.setFont(Theme.FONT_SMALL); s.setForeground(new Color(147,197,253));
        JPanel left=new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false); left.add(t); left.add(s);
        hdr.add(left,BorderLayout.WEST);
        JLabel badge=new JLabel("  HR MANAGER  ");
        badge.setFont(new Font("Segoe UI",Font.BOLD,11)); badge.setForeground(Theme.HR_COLOR);
        badge.setOpaque(true); badge.setBackground(new Color(59,130,246,25));
        badge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(59,130,246,80)),BorderFactory.createEmptyBorder(4,12,4,12)));
        hdr.add(badge,BorderLayout.EAST);
        return hdr;
    }

    private int[] getStats() {
        int[] s=new int[4];
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs;
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Employees WHERE Employment_Status='Active'"); if(rs.next()) s[0]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'"); if(rs.next()) s[1]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Recruitment WHERE Status='Hired' AND MONTH(Applied_Date)=MONTH(GETDATE())"); if(rs.next()) s[2]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Employees WHERE Employment_Status='Active' AND Emp_ID NOT IN (SELECT DISTINCT Emp_ID FROM Attendance WHERE CAST(Check_In AS DATE)=CAST(GETDATE() AS DATE))"); if(rs.next()) s[3]=rs.getInt(1);
        }catch(Exception ex){}
        return s;
    }

    private JPanel buildPendingLeaves() {
        JPanel card=Theme.card("📋 Pending Leave Requests");
        String[] cols={"Employee","Type","Days","From"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 e.First_Name+' '+e.Last_Name,l.Leave_Type,l.Total_Days,FORMAT(l.Start_Date,'dd MMM') FROM LeaveApplications l JOIN Employees e ON l.Emp_ID=e.Emp_ID WHERE l.Status='Pending' ORDER BY l.Applied_On");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); btnRow.setBackground(Theme.CARD_BG);
        JButton approve=Theme.accentBtn("✓ Approve"); JButton reject=Theme.dangerBtn("✗ Reject");
        approve.setFont(new Font("Segoe UI",Font.BOLD,11)); reject.setFont(new Font("Segoe UI",Font.BOLD,11));
        btnRow.add(approve); btnRow.add(reject);
        JPanel wrap=new JPanel(new BorderLayout(0,6)); wrap.setBackground(Theme.CARD_BG);
        wrap.add(Theme.scrollPane(t),BorderLayout.CENTER); wrap.add(btnRow,BorderLayout.SOUTH);
        card.add(wrap,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecruitmentPipeline() {
        JPanel card=Theme.card("🎯 Recruitment Pipeline");
        String[] cols={"Candidate","Position","Status"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 Name,Position_Applied,Status FROM Recruitment WHERE Status!='Hired' ORDER BY Created_At DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        t.getColumnModel().getColumn(2).setCellRenderer((tbl,val,sel,foc,r,c)->{
            JLabel l=new JLabel(String.valueOf(val)); l.setOpaque(true);
            l.setBackground(sel?new Color(99,102,241,60):(r%2==0?Theme.CARD_BG:new Color(28,40,62)));
            String v=String.valueOf(val);
            l.setForeground("Hired".equals(v)?Theme.ACCENT:"Rejected".equals(v)?Theme.DANGER:"Interview".equals(v)?Theme.ACCOUNTANT_COLOR:Theme.INFO);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));
            return l;
        });
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHRActions() {
        JPanel card=Theme.card("⚡ HR Quick Actions");
        JPanel grid=new JPanel(new GridLayout(5,1,0,7)); grid.setBackground(Theme.CARD_BG);
        grid.add(Theme.actionCard("👤","Add Employee","Onboard new team member",Theme.HR_COLOR,null));
        grid.add(Theme.actionCard("📋","Process Leave","Review & approve requests",Theme.DANGER,null));
        grid.add(Theme.actionCard("🎯","Post Job","Add recruitment listing",Theme.INFO,null));
        grid.add(Theme.actionCard("📅","Attendance","View daily attendance",Theme.ACCENT,null));
        grid.add(Theme.actionCard("🏢","Departments","Manage org structure",Theme.ACCOUNTANT_COLOR,null));
        card.add(grid,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTodayAttendanceSummary() {
        JPanel card=Theme.card("📅 Today's Attendance Overview");
        card.setPreferredSize(new Dimension(0,110));
        JPanel row=new JPanel(new GridLayout(1,4,12,0)); row.setBackground(Theme.CARD_BG);
        int[] att=new int[4];
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs;
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Attendance WHERE CAST(Check_In AS DATE)=CAST(GETDATE() AS DATE)"); if(rs.next()) att[0]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Attendance WHERE CAST(Check_In AS DATE)=CAST(GETDATE() AS DATE) AND Check_In>CAST(CAST(GETDATE() AS DATE) AS DATETIME)+0.375"); if(rs.next()) att[1]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Approved' AND CAST(GETDATE() AS DATE) BETWEEN Start_Date AND End_Date"); if(rs.next()) att[2]=rs.getInt(1);
        }catch(Exception ex){}
        row.add(summaryTile("✅","Present",String.valueOf(att[0]),Theme.ACCENT));
        row.add(summaryTile("⏰","Late Arrivals",String.valueOf(att[1]),Theme.ACCOUNTANT_COLOR));
        row.add(summaryTile("🏖️","On Leave",String.valueOf(att[2]),Theme.INFO));
        row.add(summaryTile("❌","Absent",String.valueOf(att[3]),Theme.DANGER));
        card.add(row,BorderLayout.CENTER);
        return card;
    }

    private JPanel summaryTile(String icon, String label, String val, Color c) {
        JPanel p=new JPanel(new BorderLayout(8,2)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),20));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JLabel ico=new JLabel(icon); ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22));
        JPanel info=new JPanel(new GridLayout(2,1,0,2)); info.setOpaque(false);
        JLabel v=new JLabel(val); v.setFont(new Font("Segoe UI",Font.BOLD,20)); v.setForeground(c);
        JLabel l=new JLabel(label); l.setFont(Theme.FONT_SMALL); l.setForeground(Theme.TEXT_MUTED);
        info.add(v); info.add(l);
        p.add(ico,BorderLayout.WEST); p.add(info,BorderLayout.CENTER);
        return p;
    }
}
