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

/** Team Lead Dashboard — team monitoring, leave approvals, attendance. */
public class TeamLeadDashboardPanel extends JPanel {

    public TeamLeadDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16,20,20,20));

        JPanel row=new JPanel(new GridLayout(1,4,12,0)); row.setBackground(Theme.BG);
        row.add(Theme.statCard("Team Size",    "8",  Theme.TEAMLEAD_COLOR));
        row.add(Theme.statCard("Present Today","6",  Theme.ACCENT));
        row.add(Theme.statCard("On Leave",     "1",  Theme.INFO));
        row.add(Theme.statCard("Pending Tasks","3",  Theme.ACCOUNTANT_COLOR));
        center.add(row,BorderLayout.NORTH);

        JPanel mid=new JPanel(new GridLayout(1,3,14,0)); mid.setBackground(Theme.BG);
        mid.add(buildTeamMembers());
        mid.add(buildLeaveRequests());
        mid.add(buildTeamActions());
        center.add(mid,BorderLayout.CENTER);
        center.add(buildTeamPerformance(),BorderLayout.SOUTH);
        add(center,BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(5,46,22),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,5));
                for(int x=0;x<getWidth();x+=30) for(int y=0;y<getHeight();y+=30) g2.fillOval(x,y,2,2);
                g2.setColor(Theme.TEAMLEAD_COLOR); g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0,100));
        User u=SessionManager.getInstance().getUser();
        int hour=java.time.LocalTime.now().getHour();
        String greet=hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel t=new JLabel("🎯 "+greet+", Team Lead "+(u!=null?u.getUsername():""));
        t.setFont(new Font("Segoe UI",Font.BOLD,24)); t.setForeground(Color.WHITE);
        JLabel s=new JLabel("Team Operations Dashboard — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        s.setFont(Theme.FONT_SMALL); s.setForeground(new Color(134,239,172));
        JPanel left=new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false); left.add(t); left.add(s);
        hdr.add(left,BorderLayout.WEST);
        JLabel badge=new JLabel("  TEAM LEAD  ");
        badge.setFont(new Font("Segoe UI",Font.BOLD,11)); badge.setForeground(Theme.TEAMLEAD_COLOR);
        badge.setOpaque(true); badge.setBackground(new Color(16,185,129,25));
        badge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(16,185,129,80)),BorderFactory.createEmptyBorder(4,12,4,12)));
        hdr.add(badge,BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildTeamMembers() {
        JPanel card=Theme.card("👥 My Team");
        String[] cols={"Name","Role","Status","Dept"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 First_Name+' '+Last_Name,Job_Title,Employment_Status,Dept_ID FROM Employees WHERE Employment_Status='Active' ORDER BY Emp_ID");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),"Dept "+rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        t.getColumnModel().getColumn(2).setCellRenderer((tbl,val,sel,foc,r,c)->{
            JLabel l=new JLabel("● "+val); l.setOpaque(true);
            l.setBackground(sel?new Color(99,102,241,60):(r%2==0?Theme.CARD_BG:new Color(28,40,62)));
            l.setForeground("Active".equals(val)?Theme.ACCENT:Theme.DANGER);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));
            return l;
        });
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLeaveRequests() {
        JPanel card=Theme.card("📋 Team Leave Requests");
        String[] cols={"Member","Type","Days","Status"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 e.First_Name+' '+e.Last_Name,l.Leave_Type,l.Total_Days,l.Status FROM LeaveApplications l JOIN Employees e ON l.Emp_ID=e.Emp_ID ORDER BY l.Applied_On DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        t.getColumnModel().getColumn(3).setCellRenderer((tbl,val,sel,foc,r,c)->{
            JLabel l=new JLabel(String.valueOf(val)); l.setOpaque(true);
            l.setBackground(sel?new Color(99,102,241,60):(r%2==0?Theme.CARD_BG:new Color(28,40,62)));
            String v=String.valueOf(val);
            l.setForeground("Approved".equals(v)?Theme.ACCENT:"Rejected".equals(v)?Theme.DANGER:Theme.ACCOUNTANT_COLOR);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));
            return l;
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); btnRow.setBackground(Theme.CARD_BG);
        btnRow.add(Theme.accentBtn("✓ Recommend"));
        btnRow.add(Theme.dangerBtn("✗ Decline"));
        JPanel wrap=new JPanel(new BorderLayout(0,6)); wrap.setBackground(Theme.CARD_BG);
        wrap.add(Theme.scrollPane(t),BorderLayout.CENTER); wrap.add(btnRow,BorderLayout.SOUTH);
        card.add(wrap,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTeamActions() {
        JPanel card=Theme.card("⚡ Team Actions");
        JPanel grid=new JPanel(new GridLayout(5,1,0,7)); grid.setBackground(Theme.CARD_BG);
        grid.add(Theme.actionCard("📊","Team Report","Generate performance summary",Theme.TEAMLEAD_COLOR,null));
        grid.add(Theme.actionCard("📅","Mark Attendance","Log team check-ins",Theme.ACCENT,null));
        grid.add(Theme.actionCard("🎯","Recruitment","Refer candidates",Theme.INFO,null));
        grid.add(Theme.actionCard("📋","Leave Summary","Team leave overview",Theme.ACCOUNTANT_COLOR,null));
        grid.add(Theme.actionCard("🔔","Send Notice","Alert team members",Theme.DANGER,null));
        card.add(grid,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTeamPerformance() {
        JPanel card=Theme.card("📈 Team Attendance This Week");
        card.setPreferredSize(new Dimension(0,110));
        JPanel row=new JPanel(new GridLayout(1,5,10,0)); row.setBackground(Theme.CARD_BG);
        String[] days={"Mon","Tue","Wed","Thu","Fri"};
        int[] present={7,6,8,5,6};
        Color[] cols={Theme.ACCENT,Theme.INFO,Theme.ACCENT,Theme.ACCOUNTANT_COLOR,Theme.TEAMLEAD_COLOR};
        for(int i=0;i<5;i++){
            final int fi=i;
            JPanel tile=new JPanel(new BorderLayout(4,2)){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(cols[fi].getRed(),cols[fi].getGreen(),cols[fi].getBlue(),20));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.dispose();
                }
            };
            tile.setOpaque(false); tile.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            JLabel v=new JLabel(String.valueOf(present[i]),SwingConstants.CENTER); v.setFont(new Font("Segoe UI",Font.BOLD,22)); v.setForeground(cols[i]);
            JLabel d=new JLabel(days[i],SwingConstants.CENTER); d.setFont(Theme.FONT_SMALL); d.setForeground(Theme.TEXT_MUTED);
            tile.add(v,BorderLayout.CENTER); tile.add(d,BorderLayout.SOUTH);
            row.add(tile);
        }
        card.add(row,BorderLayout.CENTER);
        return card;
    }
}
