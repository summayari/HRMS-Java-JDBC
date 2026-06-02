package hrms.gui;

import hrms.auth.SessionManager;
import hrms.dao.SalaryDAO;
import hrms.db.DBConnection;
import hrms.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Dashboard for Admin role — full system overview. */
public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        int[] stats = getStats();
        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setBackground(Theme.BG);
        statsRow.add(Theme.statCard("Total Employees",  String.valueOf(stats[0]), Theme.PRIMARY));
        statsRow.add(Theme.statCard("Active Today",     String.valueOf(stats[1]), Theme.ACCENT));
        statsRow.add(Theme.statCard("Departments",      String.valueOf(stats[2]), Theme.ACCOUNTANT_COLOR));
        statsRow.add(Theme.statCard("Pending Leaves",   String.valueOf(stats[3]), Theme.DANGER));
        statsRow.add(Theme.statCard("Total Payroll",    "₨"+stats[4]+"K",       Theme.INFO));
        center.add(statsRow, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(1, 3, 14, 0));
        middle.setBackground(Theme.BG);
        middle.add(buildDeptChart());
        middle.add(buildRecentEmployees());
        middle.add(buildQuickActions());
        center.add(middle, BorderLayout.CENTER);

        center.add(buildActivityFeed(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        return buildRoleHeader("⚙️", "Admin Control Center", "Full system access — manage all modules", Theme.ADMIN_COLOR);
    }

    private JPanel buildRoleHeader(String icon, String title, String sub, Color accent) {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),70),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,5));
                for(int x=0;x<getWidth();x+=28) for(int y=0;y<getHeight();y+=28) g2.fillOval(x,y,2,2);
                g2.setColor(accent); g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0,100));
        User u = SessionManager.getInstance().getUser();
        int hour = java.time.LocalTime.now().getHour();
        String greet = hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel t = new JLabel(icon+" "+greet+", "+(u!=null?u.getUsername():"User")+" 👋");
        t.setFont(new Font("Segoe UI",Font.BOLD,24)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sub+" — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        s.setFont(Theme.FONT_SMALL); s.setForeground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),220));
        JPanel left = new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false);
        left.add(t); left.add(s);
        hdr.add(left,BorderLayout.WEST);
        // Role badge
        JLabel badge = new JLabel("  "+u.getRole().toUpperCase()+"  ");
        badge.setFont(new Font("Segoe UI",Font.BOLD,11)); badge.setForeground(accent);
        badge.setOpaque(true); badge.setBackground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80)),
            BorderFactory.createEmptyBorder(4,12,4,12)));
        hdr.add(badge,BorderLayout.EAST);
        return hdr;
    }

    private int[] getStats() {
        int[] s = new int[5];
        try (Connection c = DBConnection.getConnection()) {
            ResultSet rs;
            rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM Employees"); if(rs.next()) s[0]=rs.getInt(1);
            rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM Attendance WHERE CAST(Check_In AS DATE)=CAST(GETDATE() AS DATE)"); if(rs.next()) s[1]=rs.getInt(1);
            rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM Departments"); if(rs.next()) s[2]=rs.getInt(1);
            rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'"); if(rs.next()) s[3]=rs.getInt(1);
            rs = c.createStatement().executeQuery("SELECT ISNULL(SUM(Net_Salary)/1000,0) FROM Salaries s WHERE s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=s.Emp_ID)"); if(rs.next()) s[4]=rs.getInt(1);
        } catch(Exception ex){ ex.printStackTrace(); }
        return s;
    }

    private JPanel buildDeptChart() {
        JPanel card = Theme.card("📊 Department Overview");
        card.setPreferredSize(new Dimension(0,240));
        java.util.List<String[]> data = new java.util.ArrayList<>();
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT Dept_Name,COUNT(Emp_ID) FROM Departments d LEFT JOIN Employees e ON d.Dept_ID=e.Dept_ID GROUP BY Dept_Name");
            while(rs.next()) data.add(new String[]{rs.getString(1),rs.getString(2)});
        }catch(Exception ex){}
        Color[] colors={Theme.PRIMARY,Theme.ACCENT,Theme.ACCOUNTANT_COLOR,Theme.INFO,Theme.DANGER,Theme.DIRECTOR_COLOR};
        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int total=data.stream().mapToInt(d->Integer.parseInt(d[1])).sum();
                if(total==0){g2.dispose();return;}
                int cx=getWidth()/2,cy=getHeight()/2-10,r=Math.min(cx,cy)-20;
                double angle=0;
                for(int i=0;i<data.size();i++){
                    double sweep=360.0*Integer.parseInt(data.get(i)[1])/total;
                    g2.setColor(colors[i%colors.length]);
                    g2.fillArc(cx-r,cy-r,r*2,r*2,(int)angle,(int)sweep);
                    g2.setColor(Theme.BG_SECONDARY); g2.setStroke(new BasicStroke(2f));
                    g2.drawArc(cx-r,cy-r,r*2,r*2,(int)angle,(int)sweep);
                    angle+=sweep;
                }
                // center hole
                g2.setColor(Theme.CARD_BG); g2.fillOval(cx-r/2,cy-r/2,r,r);
                g2.setColor(Theme.TEXT_MAIN); g2.setFont(new Font("Segoe UI",Font.BOLD,14));
                FontMetrics fm=g2.getFontMetrics(); String tot=String.valueOf(total);
                g2.drawString(tot,cx-fm.stringWidth(tot)/2,cy+fm.getAscent()/2);
                // legend
                int ly=getHeight()-data.size()*18-4;
                for(int i=0;i<data.size();i++){
                    g2.setColor(colors[i%colors.length]); g2.fillRoundRect(8,ly+i*18,10,10,4,4);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
                    g2.drawString(data.get(i)[0]+" ("+data.get(i)[1]+")",24,ly+i*18+10);
                }
                g2.dispose();
            }
        };
        chart.setBackground(Theme.CARD_BG);
        card.add(chart,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentEmployees() {
        JPanel card = Theme.card("👥 Recent Employees");
        String[] cols={"Name","Dept","Role","Status"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 7 e.First_Name+' '+e.Last_Name,d.Dept_Name,e.Job_Title,e.Employment_Status FROM Employees e LEFT JOIN Departments d ON e.Dept_ID=d.Dept_ID ORDER BY e.Emp_ID DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t);
        t.getColumnModel().getColumn(3).setCellRenderer((table,val,sel,foc,r,c)->{
            JLabel l=new JLabel(String.valueOf(val)); l.setOpaque(true);
            l.setBackground(sel?new Color(99,102,241,60):(r%2==0?Theme.CARD_BG:new Color(28,40,62)));
            l.setForeground("Active".equals(val)?Theme.ACCENT:Theme.DANGER);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));
            return l;
        });
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel card = Theme.card("⚡ Quick Actions");
        JPanel grid = new JPanel(new GridLayout(4,1,0,8)); grid.setBackground(Theme.CARD_BG);
        grid.add(Theme.actionCard("👤","Add Employee","Register new staff",Theme.PRIMARY,null));
        grid.add(Theme.actionCard("💰","Run Payroll","Process monthly salaries",Theme.ACCOUNTANT_COLOR,null));
        grid.add(Theme.actionCard("📋","Review Leaves","Pending approvals: check now",Theme.DANGER,null));
        grid.add(Theme.actionCard("📊","View Reports","Analytics & insights",Theme.INFO,null));
        JScrollPane sp=new JScrollPane(grid); sp.setBorder(null); sp.getViewport().setBackground(Theme.CARD_BG);
        card.add(sp,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActivityFeed() {
        JPanel card = Theme.card("📰 System Activity");
        card.setPreferredSize(new Dimension(0,130));
        String[] cols={"Time","User","Module","Action"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 6 FORMAT(LogTime,'hh:mm a'),Username,Module,Action FROM SystemAuditLog ORDER BY Log_ID DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(28);
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }
}
