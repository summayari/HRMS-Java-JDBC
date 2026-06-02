package hrms.gui;

import hrms.auth.SessionManager;
import hrms.dao.*;
import hrms.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Dashboard shown to Employees — personal stats, own records, leave balance. */
public class EmployeeDashboardPanel extends JPanel {

    private final int empId;
    private final AttendanceDAO  attDAO  = new AttendanceDAO();
    private final LeaveDAO       leaveDAO = new LeaveDAO();
    private final SalaryDAO      salDAO  = new SalaryDAO();
    private final LeaveBalanceDAO balDAO = new LeaveBalanceDAO();

    public EmployeeDashboardPanel() {
        User u = SessionManager.getInstance().getUser();
        this.empId = (u != null) ? u.getEmpId() : 0;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        // Top stat cards
        JPanel statsRow = buildStatCards();
        center.add(statsRow, BorderLayout.NORTH);

        // Middle: leave balance + recent attendance + recent salary
        JPanel middle = new JPanel(new GridLayout(1, 3, 14, 0));
        middle.setBackground(Theme.BG);
        middle.add(buildLeaveBalanceCard());
        middle.add(buildRecentAttendance());
        middle.add(buildRecentSalary());
        center.add(middle, BorderLayout.CENTER);

        // Bottom: quick actions
        center.add(buildQuickActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(5,46,22),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,6));
                for (int x=0;x<getWidth();x+=28) for(int y=0;y<getHeight();y+=28) g2.fillOval(x,y,2,2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
        hdr.setPreferredSize(new Dimension(0, 100));

        User u = SessionManager.getInstance().getUser();
        int hour = java.time.LocalTime.now().getHour();
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

        JLabel title = new JLabel(greet + ", " + (u != null ? u.getUsername() : "User") + " 👋");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Your personal dashboard — " +
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        sub.setFont(Theme.FONT_BODY); sub.setForeground(new Color(134, 239, 172));

        JPanel left = new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false);
        left.add(title); left.add(sub);
        hdr.add(left, BorderLayout.WEST);

        JLabel badge = new JLabel("  EMPLOYEE  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11)); badge.setForeground(Theme.ACCENT);
        badge.setOpaque(true); badge.setBackground(new Color(16,185,129,30));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(16,185,129,80)),
            BorderFactory.createEmptyBorder(6,14,6,14)));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false); right.add(badge);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setBackground(Theme.BG);

        // Today attendance
        String todayAtt = "Not Marked";
        try {
            PreparedStatement ps = hrms.db.DBConnection.getConnection()
                .prepareStatement("SELECT Status FROM Attendance WHERE Emp_ID=? AND Att_Date=CAST(GETDATE() AS DATE)");
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) todayAtt = rs.getString(1);
        } catch (Exception e) { e.printStackTrace(); }

        // Pending leaves
        long pending = leaveDAO.getByEmployee(empId).stream()
            .filter(l -> "Pending".equals(l.getStatus())).count();

        // Leave days used this year
        int usedLeave = balDAO.getTotalUsed(empId);

        // Latest salary
        List<Salary> sals = salDAO.getByEmployee(empId);
        String latestSal = sals.isEmpty() ? "N/A" : String.format("PKR %,.0f", sals.get(0).getNetSalary());

        Color attColor = "Present".equals(todayAtt) ? Theme.SUCCESS
                       : "Absent".equals(todayAtt) ? Theme.DANGER : Theme.WARNING;

        row.add(Theme.statCard("Today's Status",  todayAtt,    attColor));
        row.add(Theme.statCard("Pending Leaves",  String.valueOf(pending), Theme.WARNING));
        row.add(Theme.statCard("Leave Days Used", String.valueOf(usedLeave), Theme.INFO));
        row.add(Theme.statCard("Latest Salary",   latestSal,   Theme.ACCENT));
        return row;
    }

    private JPanel buildLeaveBalanceCard() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("⚖  Leave Balance");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel balRows = new JPanel(new GridLayout(4, 1, 0, 8));
        balRows.setOpaque(false);

        Map<String, int[]> bal = balDAO.getBalance(empId);
        Color[] colors = {Theme.PRIMARY, Theme.ACCENT, Theme.WARNING, Theme.INFO};
        String[] icons = {"🌴","🏥","📅","💸"};
        int i = 0;
        for (Map.Entry<String, int[]> e : bal.entrySet()) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            String rem = "Unpaid".equals(e.getKey()) ? "∞" : String.valueOf(e.getValue()[2]);
            String used = "Unpaid".equals(e.getKey()) ? "" : " (" + e.getValue()[1] + " used)";
            Color c = colors[i % colors.length];

            JLabel nameLbl = new JLabel(icons[i%icons.length] + " " + e.getKey() + used);
            nameLbl.setFont(Theme.FONT_SMALL); nameLbl.setForeground(Theme.TEXT_MUTED);
            JLabel remLbl = new JLabel(rem + (e.getKey().equals("Unpaid") ? "" : " days"));
            remLbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); remLbl.setForeground(c);

            // Mini progress
            int quota = e.getValue()[0], usedN = e.getValue()[1];
            JPanel prog = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(51,65,85)); g2.fillRoundRect(0,0,getWidth(),getHeight(),3,3);
                    if (quota > 0 && !e.getKey().equals("Unpaid")) {
                        int w = (int)((double)usedN/quota*getWidth());
                        g2.setColor(usedN>=quota ? Theme.DANGER : usedN>quota*0.7 ? Theme.WARNING : c);
                        g2.fillRoundRect(0,0,Math.min(w,getWidth()),getHeight(),3,3);
                    }
                    g2.dispose();
                }
            };
            prog.setPreferredSize(new Dimension(0, 4)); prog.setOpaque(false);

            JPanel left = new JPanel(new BorderLayout(0,2)); left.setOpaque(false);
            left.add(nameLbl, BorderLayout.NORTH); left.add(prog, BorderLayout.SOUTH);
            row.add(left, BorderLayout.CENTER); row.add(remLbl, BorderLayout.EAST);
            balRows.add(row); i++;
        }
        wrapper.add(balRows, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRecentAttendance() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("📅  Recent Attendance");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Date", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Attendance> atts = attDAO.getByEmployee(empId);
        for (int i = 0; i < Math.min(8, atts.size()); i++)
            m.addRow(new Object[]{atts.get(i).getAttDate(), atts.get(i).getStatus()});

        JTable t = new JTable(m); Theme.styleTable(t);
        t.getColumnModel().getColumn(1).setCellRenderer(statusRenderer());
        t.setPreferredScrollableViewportSize(new Dimension(0, 180));
        wrapper.add(Theme.scrollPane(t), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRecentSalary() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("💰  Salary History");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Month", "Net Salary"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Salary> sals = salDAO.getByEmployee(empId);
        for (int i = 0; i < Math.min(8, sals.size()); i++)
            m.addRow(new Object[]{sals.get(i).getPayMonth(),
                String.format("PKR %,.0f", sals.get(i).getNetSalary())});

        JTable t = new JTable(m); Theme.styleTable(t);
        t.getColumnModel().getColumn(1).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                        boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(tbl,value,sel,focus,row,col);
                    if (!sel) { setForeground(Theme.SUCCESS); setBackground(Theme.CARD_BG); }
                    setFont(new Font("Segoe UI", Font.BOLD, 12)); return this;
                }
            });
        t.setPreferredScrollableViewportSize(new Dimension(0, 180));
        wrapper.add(Theme.scrollPane(t), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildQuickActions() {
        JPanel wrapper = Theme.card();
        wrapper.setLayout(new BorderLayout(0, 10));
        wrapper.setPreferredSize(new Dimension(0, 72));
        JLabel lbl = new JLabel("⚡  Quick Actions");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        JButton btnLeave = Theme.primaryBtn("📋 Apply for Leave");
        JButton btnPw    = Theme.warningBtn("🔒 Change Password");

        btnLeave.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Go to Leave Management from the sidebar to apply.");
        });
        btnPw.addActionListener(e ->
            new ChangePasswordDialog(this).setVisible(true));

        btns.add(btnLeave); btns.add(btnPw);
        wrapper.add(btns, BorderLayout.CENTER);
        return wrapper;
    }

    private javax.swing.table.TableCellRenderer statusRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if (!sel) switch (value == null ? "" : value.toString()) {
                    case "Present"  -> { setForeground(Theme.SUCCESS); setBackground(new Color(22,101,52,70)); }
                    case "Absent"   -> { setForeground(Theme.DANGER);  setBackground(new Color(153,27,27,70)); }
                    case "Leave"    -> { setForeground(Theme.WARNING); setBackground(new Color(120,53,15,70)); }
                    case "Half Day" -> { setForeground(Theme.INFO);    setBackground(new Color(7,89,133,70)); }
                    default -> { setForeground(Theme.TEXT_MUTED); setBackground(Theme.CARD_BG); }
                }
                setFont(new Font("Segoe UI", Font.BOLD, 11)); setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); return this;
            }
        };
    }
}
