package hrms.gui;

import hrms.dao.AttendanceDAO;
import hrms.dao.EmployeeDAO;
import hrms.dao.SalaryDAO;
import hrms.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/** Advanced Analytics Dashboard — pure Java2D charts, no external libraries. */
public class AnalyticsDashboard extends JPanel {

    private final EmployeeDAO  empDAO = new EmployeeDAO();
    private final SalaryDAO    salDAO = new SalaryDAO();

    public AnalyticsDashboard() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(buildContent());
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(7,89,133),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(18,22,14,22));
        hdr.setPreferredSize(new Dimension(0, 72));
        JLabel title = new JLabel("📊  Advanced Analytics");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        JLabel sub = new JLabel("Real-time workforce intelligence & trends");
        sub.setFont(Theme.FONT_SMALL); sub.setForeground(new Color(125,211,252));
        JPanel left = new JPanel(new GridLayout(2,1,0,3)); left.setOpaque(false);
        left.add(title); left.add(sub);
        hdr.add(left, BorderLayout.WEST);
        JButton refresh = Theme.ghostBtn("↺ Refresh");
        refresh.addActionListener(e -> { removeAll(); build(); revalidate(); repaint(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,16));
        right.setOpaque(false); right.add(refresh);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(16,20,20,20));

        // Row 1: KPI cards
        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(16));

        // Row 2: Dept headcount bar + Gender pie (side by side)
        JPanel row2 = new JPanel(new GridLayout(1,2,14,0));
        row2.setBackground(Theme.BG); row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        row2.add(buildDeptBarChart());
        row2.add(buildGenderPieChart());
        content.add(row2);
        content.add(Box.createVerticalStrut(16));

        // Row 3: Monthly salary trend + Attendance %
        JPanel row3 = new JPanel(new GridLayout(1,2,14,0));
        row3.setBackground(Theme.BG); row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        row3.add(buildMonthlySalaryChart());
        row3.add(buildAttendancePieChart());
        content.add(row3);
        content.add(Box.createVerticalStrut(16));

        // Row 4: Leave trend bar + Role level donut
        JPanel row4 = new JPanel(new GridLayout(1,2,14,0));
        row4.setBackground(Theme.BG); row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        row4.add(buildLeaveTrendChart());
        row4.add(buildRoleLevelChart());
        content.add(row4);

        return content;
    }

    // ── KPI Row ───────────────────────────────────────────────────────────────

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1,5,10,0));
        row.setBackground(Theme.BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        int[] stats = getBasicStats();
        double[] salStats = getSalaryStats();
        int presentToday = getTodayPresent();

        row.add(kpiCard("👥","Total Staff",     String.valueOf(stats[0]),  Theme.PRIMARY));
        row.add(kpiCard("✅","Active",           String.valueOf(stats[1]),  Theme.ACCENT));
        row.add(kpiCard("📅","Present Today",    String.valueOf(presentToday),Theme.SUCCESS));
        row.add(kpiCard("💰","Avg Net Salary",   String.format("PKR %,.0f",salStats[0]), Theme.WARNING));
        row.add(kpiCard("📋","Open Leaves",      String.valueOf(stats[2]),  Theme.DANGER));
        return row;
    }

    private JPanel kpiCard(String icon, String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0,4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                GradientPaint gp = new GradientPaint(0,0,accent,getWidth(),0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),0));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.fillRect(0,2,getWidth(),3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14,16,12,16));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); top.setOpaque(false);
        JLabel ic = new JLabel(icon); ic.setFont(new Font("Segoe UI",Font.PLAIN,18));
        JLabel lb = new JLabel(label); lb.setFont(new Font("Segoe UI",Font.BOLD,9)); lb.setForeground(Theme.TEXT_MUTED);
        top.add(ic); top.add(lb);
        JLabel val = new JLabel(value); val.setFont(new Font("Segoe UI",Font.BOLD,20)); val.setForeground(accent);
        card.add(top,  BorderLayout.NORTH);
        card.add(val,  BorderLayout.CENTER);
        return card;
    }

    // ── Dept bar chart ────────────────────────────────────────────────────────

    private JPanel buildDeptBarChart() {
        List<Object[]> data = getDeptCounts(); // [[name, count], ...]
        return chartCard("🏢  Employees per Department", new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());

                int max = data.stream().mapToInt(r->((Number)r[1]).intValue()).max().orElse(1);
                int n = data.size(), pad = 10;
                int barW = Math.max(18, (getWidth()-pad*(n+1))/n);
                int chartH = getHeight()-46;
                Color[] palette = {Theme.PRIMARY,Theme.ACCENT,Theme.WARNING,Theme.DANGER,Theme.INFO,new Color(167,139,250)};

                for (int i = 0; i < n; i++) {
                    int count = ((Number)data.get(i)[1]).intValue();
                    int h = (int)((double)count/max*chartH);
                    int x = pad + i*(barW+pad), y = chartH-h+4;
                    Color c = palette[i%palette.length];
                    // Shadow
                    g2.setColor(new Color(0,0,0,25)); g2.fillRoundRect(x+2,y+2,barW,h,6,6);
                    // Bar gradient
                    g2.setPaint(new GradientPaint(x,y,c,x,y+h,new Color(c.getRed(),c.getGreen(),c.getBlue(),70)));
                    g2.fillRoundRect(x,y,barW,h,6,6);
                    // Count label
                    g2.setColor(c); g2.setFont(new Font("Segoe UI",Font.BOLD,10));
                    FontMetrics fm = g2.getFontMetrics();
                    String s = String.valueOf(count);
                    g2.drawString(s, x+(barW-fm.stringWidth(s))/2, y-3);
                    // Dept label
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,8));
                    String dept = data.get(i)[0].toString();
                    if (dept.length()>8) dept = dept.substring(0,8)+"…";
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(dept, x+(barW-fm2.stringWidth(dept))/2, getHeight()-6);
                }
                g2.dispose();
            }
        });
    }

    // ── Gender pie chart ──────────────────────────────────────────────────────

    private JPanel buildGenderPieChart() {
        int[] g = empDAO.getGenderDistribution();
        String[] labels = {"Male","Female","Other"};
        Color[]  colors = {Theme.INFO, new Color(236,72,153), Theme.TEXT_MUTED};
        int total = g[0]+g[1]+g[2];
        return chartCard("⚥  Gender Distribution", new JPanel() {
            @Override protected void paintComponent(Graphics g2d) {
                super.paintComponent(g2d);
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());
                if (total==0) { g2.setColor(Theme.TEXT_MUTED); g2.drawString("No data",20,40); g2.dispose(); return; }
                int sz=Math.min(getWidth()-120,getHeight()-20);
                int ox=(getWidth()-sz)/4, oy=(getHeight()-sz)/2;
                double start=0;
                for (int i=0;i<3;i++) {
                    if(g[i]==0) continue;
                    double arc=(double)g[i]/total*360;
                    g2.setColor(colors[i]);
                    g2.fillArc(ox,oy,sz,sz,(int)start,(int)arc);
                    g2.setColor(Theme.BG); g2.setStroke(new BasicStroke(2));
                    g2.drawArc(ox,oy,sz,sz,(int)start,(int)arc);
                    start+=arc;
                }
                // Centre hole
                int hole=sz/3, hox=ox+sz/2-hole/2, hoy=oy+sz/2-hole/2;
                g2.setColor(Theme.CARD_BG); g2.fillOval(hox,hoy,hole,hole);
                // Legend
                int lx=getWidth()-90, ly=oy+10;
                for (int i=0;i<3;i++){
                    g2.setColor(colors[i]); g2.fillRoundRect(lx,ly+i*22,12,12,3,3);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                    g2.drawString(labels[i]+": "+g[i],lx+16,ly+i*22+10);
                }
                // Centre label
                g2.setColor(Theme.TEXT_MAIN); g2.setFont(new Font("Segoe UI",Font.BOLD,16));
                String tot=String.valueOf(total);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(tot,ox+sz/2-fm.stringWidth(tot)/2,oy+sz/2+6);
                g2.dispose();
            }
        });
    }

    // ── Monthly salary trend (line chart) ─────────────────────────────────────

    private JPanel buildMonthlySalaryChart() {
        List<Object[]> data = getMonthlySalaryTrend(); // [[month, totalPayroll], ...]
        return chartCard("💰  Monthly Payroll Trend", new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());
                if (data.size() < 2) {
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
                    g2.drawString("Not enough data for trend", 20, getHeight()/2);
                    g2.dispose(); return;
                }
                int pad=40, chartW=getWidth()-pad*2, chartH=getHeight()-50;
                double max = data.stream().mapToDouble(r->((Number)r[1]).doubleValue()).max().orElse(1);
                int n = data.size();

                // Grid lines
                g2.setColor(new Color(51,65,85)); g2.setStroke(new BasicStroke(1f));
                for (int i=0;i<=4;i++) {
                    int y=pad+(int)(chartH*i/4.0);
                    g2.drawLine(pad,y,pad+chartW,y);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,8));
                    g2.drawString(String.format("%.0fK",max*(4-i)/4/1000), 2, y+4);
                    g2.setColor(new Color(51,65,85));
                }

                // Area fill
                int[] xs=new int[n+2], ys=new int[n+2];
                for (int i=0;i<n;i++){
                    xs[i]=pad+(int)((double)i/(n-1)*chartW);
                    ys[i]=pad+(int)((1-((Number)data.get(i)[1]).doubleValue()/max)*chartH);
                }
                xs[n]=xs[n-1]; ys[n]=pad+chartH;
                xs[n+1]=xs[0]; ys[n+1]=pad+chartH;
                g2.setColor(new Color(99,102,241,30)); g2.fillPolygon(xs,ys,n+2);

                // Line
                g2.setColor(Theme.PRIMARY); g2.setStroke(new BasicStroke(2.5f));
                for (int i=0;i<n-1;i++)
                    g2.drawLine(xs[i],ys[i],xs[i+1],ys[i+1]);

                // Dots + labels
                for (int i=0;i<n;i++){
                    g2.setColor(Theme.BG); g2.fillOval(xs[i]-5,ys[i]-5,10,10);
                    g2.setColor(Theme.PRIMARY); g2.fillOval(xs[i]-3,ys[i]-3,7,7);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,7));
                    String lbl=data.get(i)[0].toString(); if(lbl.length()>7) lbl=lbl.substring(0,7);
                    g2.drawString(lbl,xs[i]-10,pad+chartH+14);
                }
                g2.dispose();
            }
        });
    }

    // ── Attendance pie chart ──────────────────────────────────────────────────

    private JPanel buildAttendancePieChart() {
        int[] att = getAttendanceStats(); // [Present,Absent,Leave,HalfDay]
        String[] labels = {"Present","Absent","Leave","Half Day"};
        Color[]  colors = {Theme.SUCCESS,Theme.DANGER,Theme.WARNING,Theme.INFO};
        int total = att[0]+att[1]+att[2]+att[3];
        return chartCard("📅  Attendance Overview (All Time)", new JPanel() {
            @Override protected void paintComponent(Graphics g2d) {
                super.paintComponent(g2d);
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());
                if (total==0) { g2.setColor(Theme.TEXT_MUTED); g2.drawString("No data",20,40); g2.dispose(); return; }
                int sz=Math.min(getWidth()-130,getHeight()-20);
                int ox=(getWidth()-sz)/4, oy=(getHeight()-sz)/2;
                double start=-90;
                for (int i=0;i<4;i++){
                    if(att[i]==0) continue;
                    double arc=(double)att[i]/total*360;
                    g2.setColor(colors[i]); g2.fillArc(ox,oy,sz,sz,(int)start,(int)arc);
                    g2.setColor(Theme.BG); g2.setStroke(new BasicStroke(2));
                    g2.drawArc(ox,oy,sz,sz,(int)start,(int)arc);
                    start+=arc;
                }
                // Donut hole
                int hole=sz/3;
                g2.setColor(Theme.CARD_BG);
                g2.fillOval(ox+sz/2-hole/2,oy+sz/2-hole/2,hole,hole);
                // Centre %
                int pct = total>0 ? (int)((double)att[0]/total*100) : 0;
                g2.setColor(Theme.SUCCESS); g2.setFont(new Font("Segoe UI",Font.BOLD,15));
                String pctStr=pct+"%";
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(pctStr,ox+sz/2-fm.stringWidth(pctStr)/2,oy+sz/2+5);
                g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,7));
                g2.drawString("present",ox+sz/2-14,oy+sz/2+16);
                // Legend
                int lx=getWidth()-105, ly=oy+5;
                for (int i=0;i<4;i++){
                    int pctI=total>0?(int)((double)att[i]/total*100):0;
                    g2.setColor(colors[i]); g2.fillRoundRect(lx,ly+i*22,12,12,3,3);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                    g2.drawString(labels[i]+": "+pctI+"%",lx+16,ly+i*22+10);
                }
                g2.dispose();
            }
        });
    }

    // ── Leave trend bar chart ─────────────────────────────────────────────────

    private JPanel buildLeaveTrendChart() {
        List<Object[]> data = getLeaveTrend(); // [[month, count], ...]
        return chartCard("🏖  Leave Applications per Month", new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());
                if (data.isEmpty()) {
                    g2.setColor(Theme.TEXT_MUTED); g2.drawString("No leave data",20,40);
                    g2.dispose(); return;
                }
                int max=data.stream().mapToInt(r->((Number)r[1]).intValue()).max().orElse(1);
                int n=data.size(), pad=10;
                int barW=Math.max(14,(getWidth()-pad*(n+1))/n);
                int chartH=getHeight()-44;
                for (int i=0;i<n;i++){
                    int count=((Number)data.get(i)[1]).intValue();
                    int h=(int)((double)count/max*chartH);
                    int x=pad+i*(barW+pad), y=chartH-h+4;
                    g2.setPaint(new GradientPaint(x,y,Theme.WARNING,x,y+h,new Color(245,158,11,60)));
                    g2.fillRoundRect(x,y,barW,h,4,4);
                    g2.setColor(Theme.WARNING); g2.setFont(new Font("Segoe UI",Font.BOLD,9));
                    FontMetrics fm=g2.getFontMetrics();
                    String s=String.valueOf(count);
                    g2.drawString(s,x+(barW-fm.stringWidth(s))/2,y-2);
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,7));
                    String lbl=data.get(i)[0].toString(); if(lbl.length()>6) lbl=lbl.substring(0,6);
                    g2.drawString(lbl,x+(barW-g2.getFontMetrics().stringWidth(lbl))/2,getHeight()-5);
                }
                g2.dispose();
            }
        });
    }

    // ── Role level horizontal bar ─────────────────────────────────────────────

    private JPanel buildRoleLevelChart() {
        List<Object[]> data = empDAO.getRoleLevelDistribution();
        Color[] palette = {Theme.PRIMARY,Theme.ACCENT,Theme.WARNING,Theme.DANGER,Theme.INFO,new Color(167,139,250)};
        return chartCard("🎖  Staff by Role Level", new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());
                if (data.isEmpty()) { g2.setColor(Theme.TEXT_MUTED); g2.drawString("No data",20,40); g2.dispose(); return; }
                int max=data.stream().mapToInt(r->((Number)r[1]).intValue()).max().orElse(1);
                int n=data.size(), rowH=(getHeight()-20)/Math.max(n,1), labelW=90;
                for (int i=0;i<n;i++){
                    int count=((Number)data.get(i)[1]).intValue();
                    int barMaxW=getWidth()-labelW-50;
                    int barW=(int)((double)count/max*barMaxW);
                    int y=10+i*rowH, barY=y+(rowH-16)/2;
                    Color c=palette[i%palette.length];
                    // Label
                    g2.setColor(Theme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                    String lbl=data.get(i)[0].toString(); if(lbl.length()>12) lbl=lbl.substring(0,12);
                    g2.drawString(lbl,4,barY+12);
                    // Track
                    g2.setColor(new Color(51,65,85)); g2.fillRoundRect(labelW,barY,barMaxW,14,7,7);
                    // Bar
                    g2.setPaint(new GradientPaint(labelW,barY,c,labelW+barW,barY,new Color(c.getRed(),c.getGreen(),c.getBlue(),80)));
                    g2.fillRoundRect(labelW,barY,Math.max(barW,6),14,7,7);
                    // Count
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,9));
                    g2.drawString(String.valueOf(count),labelW+barW+4,barY+10);
                }
                g2.dispose();
            }
        });
    }

    // ── Chart card wrapper ────────────────────────────────────────────────────

    private JPanel chartCard(String title, JPanel chart) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        card.add(lbl, BorderLayout.NORTH);
        chart.setBackground(Theme.CARD_BG);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── DB queries ────────────────────────────────────────────────────────────

    private int[] getBasicStats() {
        int[] s = new int[3];
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement()) {
            ResultSet rs;
            rs=st.executeQuery("SELECT COUNT(*) FROM Employees"); if(rs.next()) s[0]=rs.getInt(1);
            rs=st.executeQuery("SELECT COUNT(*) FROM Employees WHERE Status='Active'"); if(rs.next()) s[1]=rs.getInt(1);
            rs=st.executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'"); if(rs.next()) s[2]=rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return s;
    }

    private double[] getSalaryStats() {
        double[] s = new double[1];
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement();
             ResultSet rs=st.executeQuery("SELECT ISNULL(AVG(Net_Salary),0) FROM Salaries")) {
            if(rs.next()) s[0]=rs.getDouble(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return s;
    }

    private int getTodayPresent() {
        try (Connection c=DBConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(
               "SELECT COUNT(*) FROM Attendance WHERE Status='Present' AND Att_Date=CAST(GETDATE() AS DATE)")) {
            ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private List<Object[]> getDeptCounts() {
        List<Object[]> list=new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement();
             ResultSet rs=st.executeQuery(
               "SELECT d.Dept_Name,COUNT(e.Emp_ID) AS C FROM Departments d "
             + "LEFT JOIN Employees e ON d.Dept_ID=e.Dept_ID GROUP BY d.Dept_Name ORDER BY C DESC")) {
            while(rs.next()) list.add(new Object[]{rs.getString(1),rs.getInt(2)});
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    private List<Object[]> getMonthlySalaryTrend() {
        List<Object[]> list=new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement();
             ResultSet rs=st.executeQuery(
               "SELECT TOP 8 Pay_Month, SUM(Net_Salary) AS Total "
             + "FROM Salaries GROUP BY Pay_Month ORDER BY MIN(Pay_Date) ASC")) {
            while(rs.next()) list.add(new Object[]{rs.getString(1),rs.getDouble(2)});
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    private int[] getAttendanceStats() {
        int[] s=new int[4];
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement();
             ResultSet rs=st.executeQuery(
               "SELECT Status,COUNT(*) AS C FROM Attendance GROUP BY Status")) {
            while(rs.next()) switch(rs.getString(1)){
                case "Present"  -> s[0]=rs.getInt(2);
                case "Absent"   -> s[1]=rs.getInt(2);
                case "Leave"    -> s[2]=rs.getInt(2);
                case "Half Day" -> s[3]=rs.getInt(2);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return s;
    }

    private List<Object[]> getLeaveTrend() {
        List<Object[]> list=new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement();
             ResultSet rs=st.executeQuery(
               "SELECT TOP 8 FORMAT(Applied_On,'MMM-yy') AS Mo, COUNT(*) AS C "
             + "FROM LeaveApplications GROUP BY FORMAT(Applied_On,'MMM-yy'), "
             + "YEAR(Applied_On)*100+MONTH(Applied_On) ORDER BY YEAR(Applied_On)*100+MONTH(Applied_On)")) {
            while(rs.next()) list.add(new Object[]{rs.getString(1),rs.getInt(2)});
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }
}
