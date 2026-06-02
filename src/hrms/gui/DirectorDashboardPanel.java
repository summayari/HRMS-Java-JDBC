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

/** Director Dashboard — strategic overview, KPIs, approvals. */
public class DirectorDashboardPanel extends JPanel {

    public DirectorDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16,20,20,20));

        // KPI row
        int[] kpis = getKPIs();
        JPanel kpiRow = new JPanel(new GridLayout(1,4,12,0));
        kpiRow.setBackground(Theme.BG);
        kpiRow.add(Theme.statCard("Total Headcount",  String.valueOf(kpis[0]), Theme.DIRECTOR_COLOR));
        kpiRow.add(Theme.statCard("Monthly Payroll",  "₨"+kpis[1]+"K",        Theme.ACCOUNTANT_COLOR));
        kpiRow.add(Theme.statCard("Open Positions",   String.valueOf(kpis[2]), Theme.INFO));
        kpiRow.add(Theme.statCard("Attendance Rate",  kpis[3]+"%",             Theme.ACCENT));
        center.add(kpiRow, BorderLayout.NORTH);

        JPanel mid = new JPanel(new GridLayout(1,3,14,0));
        mid.setBackground(Theme.BG);
        mid.add(buildPayrollTrend());
        mid.add(buildDeptStrength());
        mid.add(buildPendingApprovals());
        center.add(mid, BorderLayout.CENTER);

        center.add(buildStrategicInsights(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(88,28,135),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,5));
                for(int x=0;x<getWidth();x+=32) for(int y=0;y<getHeight();y+=32) g2.fillOval(x,y,2,2);
                g2.setColor(Theme.DIRECTOR_COLOR); g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0,100));
        User u = SessionManager.getInstance().getUser();
        int hour = java.time.LocalTime.now().getHour();
        String greet = hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel t = new JLabel("👑 "+greet+", Director "+(u!=null?u.getUsername():""));
        t.setFont(new Font("Segoe UI",Font.BOLD,24)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Strategic Overview — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        s.setFont(Theme.FONT_SMALL); s.setForeground(new Color(192,132,252));
        JPanel left=new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false); left.add(t); left.add(s);
        hdr.add(left,BorderLayout.WEST);
        JLabel badge=new JLabel("  DIRECTOR  ");
        badge.setFont(new Font("Segoe UI",Font.BOLD,11)); badge.setForeground(Theme.DIRECTOR_COLOR);
        badge.setOpaque(true); badge.setBackground(new Color(168,85,247,25));
        badge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(168,85,247,80)),BorderFactory.createEmptyBorder(4,12,4,12)));
        hdr.add(badge,BorderLayout.EAST);
        return hdr;
    }

    private int[] getKPIs() {
        int[] k=new int[4];
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs;
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Employees WHERE Employment_Status='Active'"); if(rs.next()) k[0]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT ISNULL(SUM(Net_Salary)/1000,0) FROM Salaries s WHERE s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=s.Emp_ID)"); if(rs.next()) k[1]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM Recruitment WHERE Status IN ('Applied','Shortlisted','Interview')"); if(rs.next()) k[2]=rs.getInt(1);
            rs=c.createStatement().executeQuery("SELECT ISNULL(CAST(COUNT(*)*100/(NULLIF((SELECT COUNT(*) FROM Employees WHERE Employment_Status='Active'),0)) AS INT),0) FROM Attendance WHERE CAST(Check_In AS DATE)=CAST(GETDATE() AS DATE)"); if(rs.next()) k[3]=rs.getInt(1);
        }catch(Exception ex){}
        return k;
    }

    private JPanel buildPayrollTrend() {
        JPanel card = Theme.card("💰 Payroll by Department");
        java.util.List<String[]> data=new java.util.ArrayList<>();
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT Dept_Name,ISNULL(Total_Payroll,0) FROM vw_DeptStats ORDER BY Total_Payroll DESC");
            while(rs.next()) data.add(new String[]{rs.getString(1),rs.getString(2)});
        }catch(Exception ex){}
        JPanel chart=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                if(data.isEmpty()) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                double max=data.stream().mapToDouble(d->Double.parseDouble(d[1])).max().orElse(1);
                int pad=10,bw=Math.max(16,(getWidth()-pad*2)/data.size()-8);
                Color[] cols={Theme.DIRECTOR_COLOR,Theme.PRIMARY,Theme.INFO,Theme.ACCENT,Theme.ACCOUNTANT_COLOR};
                for(int i=0;i<data.size();i++){
                    int bh=(int)((Double.parseDouble(data.get(i)[1])/max)*(getHeight()-50));
                    int bx=pad+i*(bw+8),by=getHeight()-30-bh;
                    g2.setColor(cols[i%cols.length]);
                    g2.fillRoundRect(bx,by,bw,bh,6,6);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,8));
                    String n=data.get(i)[0]; if(n.length()>6) n=n.substring(0,6);
                    g2.drawString(n,bx,getHeight()-14);
                }
                g2.dispose();
            }
        };
        chart.setBackground(Theme.CARD_BG);
        card.add(chart,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDeptStrength() {
        JPanel card = Theme.card("🏢 Department Strength");
        String[] cols={"Department","Employees","Avg Salary"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT Dept_Name,Total_Employees,CAST(Avg_Net_Salary AS INT) FROM vw_DeptStats ORDER BY Total_Employees DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),"₨"+rs.getString(3)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPendingApprovals() {
        JPanel card = Theme.card("⏳ Pending Approvals");
        String[] cols={"Employee","Type","Days","Applied"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 e.First_Name+' '+e.Last_Name,l.Leave_Type,l.Total_Days,FORMAT(l.Applied_On,'dd MMM') FROM LeaveApplications l JOIN Employees e ON l.Emp_ID=e.Emp_ID WHERE l.Status='Pending' ORDER BY l.Applied_On");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        card.add(Theme.scrollPane(t),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStrategicInsights() {
        JPanel card = Theme.card("📈 Strategic Insights");
        card.setPreferredSize(new Dimension(0,110));
        JPanel row=new JPanel(new GridLayout(1,4,12,0)); row.setBackground(Theme.CARD_BG);
        row.add(insightTile("🎯","Recruitment","Track hiring pipeline",Theme.INFO));
        row.add(insightTile("📊","Analytics","Company-wide reports",Theme.DIRECTOR_COLOR));
        row.add(insightTile("💼","Compliance","Audit & governance",Theme.ACCOUNTANT_COLOR));
        row.add(insightTile("🌱","Growth","Headcount planning",Theme.ACCENT));
        card.add(row,BorderLayout.CENTER);
        return card;
    }

    private JPanel insightTile(String icon, String title, String desc, Color c) {
        JPanel p=new JPanel(new BorderLayout(8,2)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),20));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),60));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JLabel i=new JLabel(icon); i.setFont(new Font("Segoe UI Emoji",Font.PLAIN,20));
        JPanel info=new JPanel(new GridLayout(2,1,0,2)); info.setOpaque(false);
        JLabel t=new JLabel(title); t.setFont(new Font("Segoe UI",Font.BOLD,12)); t.setForeground(Color.WHITE);
        JLabel d=new JLabel(desc); d.setFont(Theme.FONT_SMALL); d.setForeground(Theme.TEXT_MUTED);
        info.add(t); info.add(d);
        p.add(i,BorderLayout.WEST); p.add(info,BorderLayout.CENTER);
        return p;
    }
}
