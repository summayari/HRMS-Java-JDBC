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

/** Accountant Dashboard — salaries, payroll, expenses, financial reports. */
public class AccountantDashboardPanel extends JPanel {

    public AccountantDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16,20,20,20));

        double[] fin=getFinancials();
        JPanel row=new JPanel(new GridLayout(1,4,12,0)); row.setBackground(Theme.BG);
        row.add(Theme.statCard("Monthly Payroll",  "₨"+String.format("%.0f",fin[0])+"K", Theme.ACCOUNTANT_COLOR));
        row.add(Theme.statCard("Total Bonuses",    "₨"+String.format("%.0f",fin[1])+"K", Theme.ACCENT));
        row.add(Theme.statCard("Tax Collected",    "₨"+String.format("%.0f",fin[2])+"K", Theme.DANGER));
        row.add(Theme.statCard("Net Disbursement", "₨"+String.format("%.0f",fin[3])+"K", Theme.INFO));
        center.add(row,BorderLayout.NORTH);

        JPanel mid=new JPanel(new GridLayout(1,3,14,0)); mid.setBackground(Theme.BG);
        mid.add(buildSalarySummary());
        mid.add(buildPayrollChart());
        mid.add(buildFinancialActions());
        center.add(mid,BorderLayout.CENTER);

        center.add(buildExpenseOverview(),BorderLayout.SOUTH);
        add(center,BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(78,52,0),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,5));
                for(int x=0;x<getWidth();x+=28) for(int y=0;y<getHeight();y+=28) g2.fillOval(x,y,2,2);
                g2.setColor(Theme.ACCOUNTANT_COLOR); g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0,100));
        User u=SessionManager.getInstance().getUser();
        int hour=java.time.LocalTime.now().getHour();
        String greet=hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel t=new JLabel("💰 "+greet+", "+(u!=null?u.getUsername():"Accountant"));
        t.setFont(new Font("Segoe UI",Font.BOLD,24)); t.setForeground(Color.WHITE);
        JLabel s=new JLabel("Finance & Payroll Center — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        s.setFont(Theme.FONT_SMALL); s.setForeground(new Color(253,230,138));
        JPanel left=new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false); left.add(t); left.add(s);
        hdr.add(left,BorderLayout.WEST);
        JLabel badge=new JLabel("  ACCOUNTANT  ");
        badge.setFont(new Font("Segoe UI",Font.BOLD,11)); badge.setForeground(Theme.ACCOUNTANT_COLOR);
        badge.setOpaque(true); badge.setBackground(new Color(245,158,11,25));
        badge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(245,158,11,80)),BorderFactory.createEmptyBorder(4,12,4,12)));
        hdr.add(badge,BorderLayout.EAST);
        return hdr;
    }

    private double[] getFinancials() {
        double[] f=new double[4];
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT ISNULL(SUM(Basic_Salary+Bonus+Overtime_Pay),0)/1000, ISNULL(SUM(Bonus),0)/1000, ISNULL(SUM(Tax_Amount),0)/1000, ISNULL(SUM(Net_Salary),0)/1000 FROM Salaries s WHERE s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=s.Emp_ID)");
            if(rs.next()){f[0]=rs.getDouble(1);f[1]=rs.getDouble(2);f[2]=rs.getDouble(3);f[3]=rs.getDouble(4);}
        }catch(Exception ex){}
        return f;
    }

    private JPanel buildSalarySummary() {
        JPanel card=Theme.card("💼 Salary Overview");
        String[] cols={"Employee","Basic","Bonus","Net"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT TOP 8 e.First_Name+' '+e.Last_Name,s.Basic_Salary,s.Bonus,s.Net_Salary FROM Salaries s JOIN Employees e ON s.Emp_ID=e.Emp_ID WHERE s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=s.Emp_ID) ORDER BY s.Net_Salary DESC");
            while(rs.next()) m.addRow(new Object[]{rs.getString(1),"₨"+rs.getInt(2),"₨"+rs.getInt(3),"₨"+rs.getInt(4)});
        }catch(Exception ex){}
        JTable t=new JTable(m); Theme.styleTable(t); t.setRowHeight(30);
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); btnRow.setBackground(Theme.CARD_BG);
        btnRow.add(Theme.accentBtn("💳 Run Payroll"));
        btnRow.add(Theme.infoBtn("📄 Export"));
        JPanel wrap=new JPanel(new BorderLayout(0,6)); wrap.setBackground(Theme.CARD_BG);
        wrap.add(Theme.scrollPane(t),BorderLayout.CENTER); wrap.add(btnRow,BorderLayout.SOUTH);
        card.add(wrap,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPayrollChart() {
        JPanel card=Theme.card("📊 Dept Payroll Distribution");
        java.util.List<String[]> data=new java.util.ArrayList<>();
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT Dept_Name,CAST(Total_Payroll AS BIGINT) FROM vw_DeptStats ORDER BY Total_Payroll DESC");
            while(rs.next()) data.add(new String[]{rs.getString(1),rs.getString(2)});
        }catch(Exception ex){}
        JPanel chart=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                if(data.isEmpty()) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                long total=data.stream().mapToLong(d->{try{return Long.parseLong(d[1]);}catch(Exception e){return 0;}}).sum();
                if(total==0){g2.dispose();return;}
                Color[] cols={Theme.ACCOUNTANT_COLOR,Theme.ACCENT,Theme.INFO,Theme.DIRECTOR_COLOR,Theme.PRIMARY};
                int barH=Math.min(28,(getHeight()-40)/Math.max(1,data.size())-6);
                int maxW=getWidth()-100;
                for(int i=0;i<data.size();i++){
                    long v=Long.parseLong(data.get(i)[1]);
                    int w=(int)(v*maxW/total);
                    int y=20+i*(barH+8);
                    g2.setColor(cols[i%cols.length]);
                    g2.fillRoundRect(0,y,w,barH,6,6);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
                    String n=data.get(i)[0]; if(n.length()>8) n=n.substring(0,8);
                    g2.drawString(n,w+6,y+barH-4);
                }
                g2.dispose();
            }
        };
        chart.setBackground(Theme.CARD_BG);
        card.add(chart,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFinancialActions() {
        JPanel card=Theme.card("⚡ Finance Actions");
        JPanel grid=new JPanel(new GridLayout(5,1,0,7)); grid.setBackground(Theme.CARD_BG);
        grid.add(Theme.actionCard("💰","Add Salary","Record employee salary",Theme.ACCOUNTANT_COLOR,null));
        grid.add(Theme.actionCard("💳","Run Payroll","Process all salaries",Theme.ACCENT,null));
        grid.add(Theme.actionCard("🧾","Expense Report","Track company expenses",Theme.INFO,null));
        grid.add(Theme.actionCard("📊","Financial Report","P&L, payroll summary",Theme.DIRECTOR_COLOR,null));
        grid.add(Theme.actionCard("📤","Export CSV","Download payroll data",Theme.PRIMARY,null));
        card.add(grid,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildExpenseOverview() {
        JPanel card=Theme.card("🧾 Expense & Cost Summary");
        card.setPreferredSize(new Dimension(0,110));
        JPanel row=new JPanel(new GridLayout(1,5,10,0)); row.setBackground(Theme.CARD_BG);
        String[] labels={"Basic Salaries","Bonuses","Overtime","Tax Deducted","Net Paid"};
        double[] vals=new double[5];
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery(
                "SELECT ISNULL(SUM(Basic_Salary),0)/1000, ISNULL(SUM(Bonus),0)/1000, ISNULL(SUM(Overtime_Pay),0)/1000, ISNULL(SUM(Tax_Amount),0)/1000, ISNULL(SUM(Net_Salary),0)/1000 FROM Salaries s WHERE s.Salary_ID=(SELECT MAX(Salary_ID) FROM Salaries WHERE Emp_ID=s.Emp_ID)");
            if(rs.next()){for(int i=0;i<5;i++) vals[i]=rs.getDouble(i+1);}
        }catch(Exception ex){}
        Color[] cols={Theme.PRIMARY,Theme.ACCENT,Theme.INFO,Theme.DANGER,Theme.ACCOUNTANT_COLOR};
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
            JLabel v=new JLabel("₨"+String.format("%.1f",vals[i])+"K",SwingConstants.CENTER); v.setFont(new Font("Segoe UI",Font.BOLD,14)); v.setForeground(cols[i]);
            JLabel l=new JLabel(labels[i],SwingConstants.CENTER); l.setFont(new Font("Segoe UI",Font.PLAIN,9)); l.setForeground(Theme.TEXT_MUTED);
            tile.add(v,BorderLayout.CENTER); tile.add(l,BorderLayout.SOUTH);
            row.add(tile);
        }
        card.add(row,BorderLayout.CENTER);
        return card;
    }
}
