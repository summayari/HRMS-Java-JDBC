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
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        // Stat cards row
        int[] stats = getStats();
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setBackground(Theme.BG);
        statsRow.add(Theme.statCard("Total Employees",  String.valueOf(stats[0]), Theme.PRIMARY));
        statsRow.add(Theme.statCard("Active Employees", String.valueOf(stats[1]), Theme.ACCENT));
        statsRow.add(Theme.statCard("Departments",      String.valueOf(stats[2]), Theme.WARNING));
        statsRow.add(Theme.statCard("Pending Leaves",   String.valueOf(stats[3]), Theme.DANGER));
        center.add(statsRow, BorderLayout.NORTH);

        // Middle: chart + recent tables
        JPanel middle = new JPanel(new GridLayout(1, 3, 14, 0));
        middle.setBackground(Theme.BG);
        middle.add(buildDeptChart());
        middle.add(buildRecentEmployees());
        middle.add(buildRecentLeaves());
        center.add(middle, BorderLayout.CENTER);

        // Bottom: activity feed
        center.add(buildActivityFeed(), BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(49,46,129),getWidth(),getHeight(),new Color(15,23,42));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,8));
                for (int x=0;x<getWidth();x+=30) for(int y=0;y<getHeight();y+=30) g2.fillOval(x,y,2,2);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        header.setPreferredSize(new Dimension(0, 100));

        User u = SessionManager.getInstance().getUser();
        String hour = String.valueOf(java.time.LocalTime.now().getHour());
        String greet = Integer.parseInt(hour) < 12 ? "Good morning" : Integer.parseInt(hour) < 17 ? "Good afternoon" : "Good evening";
        String name = (u != null) ? u.getUsername() : "User";

        JLabel title = new JLabel(greet + ", " + name + " 👋");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Here's your HRMS overview — "
            + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(new Color(165, 180, 252));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(title); left.add(sub);
        header.add(left, BorderLayout.WEST);

        if (u != null) {
            Color rc = u.isAdmin() ? Theme.PRIMARY : Theme.ACCENT;
            JLabel role = new JLabel("  " + u.getRole().toUpperCase() + "  ");
            role.setFont(new Font("Segoe UI", Font.BOLD, 11));
            role.setForeground(rc);
            role.setOpaque(true);
            role.setBackground(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 30));
            role.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 80)),
                BorderFactory.createEmptyBorder(6,14,6,14)));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false); right.add(role);
            header.add(right, BorderLayout.EAST);
        }
        return header;
    }

    // ── Dept bar chart ────────────────────────────────────────────────────────

    private JPanel buildDeptChart() {
        List<String[]> data = getDeptCounts();

        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));

        JLabel lbl = new JLabel("🏢  Employees by Department");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG); g2.fillRect(0,0,getWidth(),getHeight());

                int max = data.stream().mapToInt(r -> Integer.parseInt(r[1])).max().orElse(1);
                int n   = data.size();
                int pad = 12;
                int barW= Math.max(20, (getWidth() - pad*(n+1)) / n);
                int chartH = getHeight() - 44;

                Color[] palette = {Theme.PRIMARY, Theme.ACCENT, Theme.WARNING, Theme.DANGER, Theme.INFO};
                for (int i = 0; i < n; i++) {
                    int count = Integer.parseInt(data.get(i)[1]);
                    int h = (int)((double)count / max * chartH);
                    int x = pad + i * (barW + pad);
                    int y = chartH - h + 4;
                    Color c = palette[i % palette.length];

                    // Shadow
                    g2.setColor(new Color(0,0,0,30));
                    g2.fillRoundRect(x+2, y+2, barW, h, 6, 6);

                    // Gradient bar
                    GradientPaint gp = new GradientPaint(x, y, c, x, y+h, new Color(c.getRed(),c.getGreen(),c.getBlue(),80));
                    g2.setPaint(gp);
                    g2.fillRoundRect(x, y, barW, h, 6, 6);

                    // Count on top
                    g2.setColor(c);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String cnt = String.valueOf(count);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(cnt, x + (barW - fm.stringWidth(cnt))/2, y-4);

                    // Dept name at bottom
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String dept = data.get(i)[0].length() > 8 ? data.get(i)[0].substring(0,8)+"…" : data.get(i)[0];
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(dept, x + (barW - fm2.stringWidth(dept))/2, getHeight()-6);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        wrapper.add(chart, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Recent employees ──────────────────────────────────────────────────────

    private JPanel buildRecentEmployees() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("👥  Recent Employees");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Department", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT TOP 8 e.Emp_ID,e.Emp_Name,d.Dept_Name,e.Status "
             + "FROM Employees e JOIN Departments d ON e.Dept_ID=d.Dept_ID ORDER BY e.Emp_ID DESC")) {
            while (rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        } catch (SQLException ex) { ex.printStackTrace(); }

        JTable t = new JTable(m);
        Theme.styleTable(t);
        t.setPreferredScrollableViewportSize(new Dimension(0, 180));
        wrapper.add(Theme.scrollPane(t), BorderLayout.CENTER);
        return wrapper;
    }

    // ── Recent leaves ─────────────────────────────────────────────────────────

    private JPanel buildRecentLeaves() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("📋  Recent Leaves");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Employee", "Type", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT TOP 8 e.Emp_Name,la.Leave_Type,la.Status "
             + "FROM LeaveApplications la JOIN Employees e ON la.Emp_ID=e.Emp_ID ORDER BY la.Leave_ID DESC")) {
            while (rs.next()) m.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3)});
        } catch (SQLException ex) { ex.printStackTrace(); }

        JTable t = new JTable(m);
        Theme.styleTable(t);
        t.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl,value,sel,focus,row,col);
                if (!sel) {
                    switch (value == null ? "" : value.toString()) {
                        case "Approved" -> setForeground(Theme.SUCCESS);
                        case "Rejected" -> setForeground(Theme.DANGER);
                        default         -> setForeground(Theme.WARNING);
                    }
                }
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                return this;
            }
        });
        t.setPreferredScrollableViewportSize(new Dimension(0, 180));
        wrapper.add(Theme.scrollPane(t), BorderLayout.CENTER);
        return wrapper;
    }

    // ── Activity feed ─────────────────────────────────────────────────────────

    private JPanel buildActivityFeed() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 8));
        wrapper.setPreferredSize(new Dimension(0, 90));

        JLabel lbl = new JLabel("⚡  Quick Stats");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 20, 0));
        stats.setOpaque(false);

        // Today's attendance, total payroll, active depts, pending leaves
        String[] todayAtt = getTodayAttendance();
        Object[] payroll  = getTotalPayroll();
        String[][] items = {
            {"📅 Today's Present", todayAtt[0], Theme.SUCCESS.toString()},
            {"❌ Today's Absent",  todayAtt[1], Theme.DANGER.toString()},
            {"💰 Total Payroll",   payroll[0].toString(), Theme.ACCENT.toString()},
            {"🏖 Pending Leaves",  payroll[1].toString(), Theme.WARNING.toString()},
        };
        Color[] colors = {Theme.SUCCESS, Theme.DANGER, Theme.ACCENT, Theme.WARNING};

        for (int i = 0; i < items.length; i++) {
            JPanel card = new JPanel(new GridLayout(2,1,0,2));
            card.setBackground(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 15));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 40)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            JLabel k = new JLabel(items[i][0]);
            k.setFont(new Font("Segoe UI", Font.PLAIN, 10)); k.setForeground(Theme.TEXT_MUTED);
            JLabel v = new JLabel(items[i][1]);
            v.setFont(new Font("Segoe UI", Font.BOLD, 18)); v.setForeground(colors[i]);
            card.add(k); card.add(v);
            stats.add(card);
        }
        wrapper.add(stats, BorderLayout.CENTER);
        return wrapper;
    }

    // ── DB queries ────────────────────────────────────────────────────────────

    private int[] getStats() {
        int[] s = new int[4];
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            ResultSet rs;
            rs = st.executeQuery("SELECT COUNT(*) FROM Employees"); if(rs.next()) s[0]=rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(*) FROM Employees WHERE Status='Active'"); if(rs.next()) s[1]=rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(*) FROM Departments"); if(rs.next()) s[2]=rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'"); if(rs.next()) s[3]=rs.getInt(1);
        } catch (SQLException ex) { ex.printStackTrace(); }
        return s;
    }

    private List<String[]> getDeptCounts() {
        List<String[]> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
               "SELECT d.Dept_Name, COUNT(e.Emp_ID) AS Cnt "
             + "FROM Departments d LEFT JOIN Employees e ON d.Dept_ID=e.Dept_ID "
             + "GROUP BY d.Dept_Name ORDER BY Cnt DESC")) {
            while (rs.next()) list.add(new String[]{rs.getString(1), String.valueOf(rs.getInt(2))});
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private String[] getTodayAttendance() {
        String[] r = {"0","0"};
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
               "SELECT Status, COUNT(*) FROM Attendance WHERE Att_Date=CAST(GETDATE() AS DATE) GROUP BY Status")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if ("Present".equals(rs.getString(1))) r[0] = String.valueOf(rs.getInt(2));
                if ("Absent".equals(rs.getString(1)))  r[1] = String.valueOf(rs.getInt(2));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return r;
    }

    private Object[] getTotalPayroll() {
        Object[] r = {"PKR 0", "0"};
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT ISNULL(SUM(Net_Salary),0) FROM Salaries");
            if (rs.next()) r[0] = String.format("PKR %,.0f", rs.getDouble(1));
            rs = st.executeQuery("SELECT COUNT(*) FROM LeaveApplications WHERE Status='Pending'");
            if (rs.next()) r[1] = String.valueOf(rs.getInt(1));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return r;
    }
}
