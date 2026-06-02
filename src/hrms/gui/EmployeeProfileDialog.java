package hrms.gui;

import hrms.dao.*;
import hrms.model.*;
import hrms.util.DialogHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Full employee profile dialog — shows all records in tabbed panes:
 * Overview | Attendance | Salary History | Leave History | Leave Balance
 */
public class EmployeeProfileDialog extends JDialog {

    private final Employee emp;
    private final AttendanceDAO attDAO  = new AttendanceDAO();
    private final SalaryDAO     salDAO  = new SalaryDAO();
    private final LeaveDAO      leaveDAO = new LeaveDAO();
    private final LeaveBalanceDAO balDAO = new LeaveBalanceDAO();

    public EmployeeProfileDialog(Component parent, Employee emp) {
        
        this.emp = emp;
        setTitle("Employee Profile — " + emp.getEmpName());
        setModal(true);
        setSize(860, 640);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        // ── Profile header ────────────────────────────────────────────────────
        root.add(buildProfileHeader(), BorderLayout.NORTH);

        // ── Tabbed content ────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_SECONDARY);
        tabs.setForeground(Theme.TEXT_MAIN);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Style the tabbed pane
        UIManager.put("TabbedPane.selected",            Theme.BG_SECONDARY);
        UIManager.put("TabbedPane.background",          Theme.BG);
        UIManager.put("TabbedPane.foreground",          Theme.TEXT_MUTED);
        UIManager.put("TabbedPane.selectedForeground",  Theme.TEXT_MAIN);

        tabs.addTab("📋  Overview",          buildOverviewTab());
        tabs.addTab("📅  Attendance",         buildAttendanceTab());
        tabs.addTab("💰  Salary History",     buildSalaryTab());
        tabs.addTab("🏖  Leave History",      buildLeaveTab());
        tabs.addTab("⚖  Leave Balance",       buildLeaveBalanceTab());

        root.add(tabs, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JButton closeBtn = Theme.primaryBtn("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel footer = DialogHelper.buttonRow(closeBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Profile header card ───────────────────────────────────────────────────

    private JPanel buildProfileHeader() {
        JPanel hdr = new JPanel(new BorderLayout(20, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(49,46,129),
                    getWidth(), getHeight(), new Color(15,23,42));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // dot texture
                g2.setColor(new Color(255,255,255,10));
                for (int x=0;x<getWidth();x+=28) for(int y=0;y<getHeight();y+=28) g2.fillOval(x,y,2,2);
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Avatar circle with initial
        Color statusColor = "Active".equals(emp.getStatus()) ? Theme.SUCCESS
                          : "On Leave".equals(emp.getStatus()) ? Theme.WARNING : Theme.DANGER;
        JLabel avatar = new JLabel(String.valueOf(emp.getEmpName().charAt(0)).toUpperCase(), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Status ring
                g2.setColor(statusColor);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(2, 2, getWidth()-4, getHeight()-4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 32));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(72, 72));

        // Right: info grid
        JPanel info = new JPanel(new GridLayout(2, 3, 16, 4));
        info.setOpaque(false);

        info.add(infoBlock("Employee ID",  "#" + emp.getEmpId()));
        info.add(infoBlock("Department",    emp.getDeptName()));
        info.add(infoBlock("Position",      emp.getPosition() != null ? emp.getPosition() : "—"));
        info.add(infoBlock("Joined",        emp.getJoiningDate()));
        info.add(infoBlock("Email",         emp.getEmail() != null ? emp.getEmail() : "—"));
        info.add(infoBlock("Phone",         emp.getPhone() != null ? emp.getPhone() : "—"));

        // Status badge
        JLabel statusBadge = new JLabel("  " + emp.getStatus() + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(statusColor);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 30));
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 80)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameRow.setOpaque(false);
        JLabel nameLabel = new JLabel(emp.getEmpName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLabel.setForeground(Color.WHITE);
        nameRow.add(nameLabel);
        nameRow.add(statusBadge);

        JPanel leftInfo = new JPanel(new BorderLayout(0, 8));
        leftInfo.setOpaque(false);
        leftInfo.add(nameRow, BorderLayout.NORTH);
        leftInfo.add(info,    BorderLayout.CENTER);

        hdr.add(avatar,   BorderLayout.WEST);
        hdr.add(leftInfo, BorderLayout.CENTER);
        return hdr;
    }

    private JPanel infoBlock(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(100, 116, 139));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(Theme.TEXT_MAIN);
        p.add(lbl); p.add(val);
        return p;
    }

    // ── Overview tab ──────────────────────────────────────────────────────────

    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Attendance summary
        int[] attStats = getAttendanceStats();
        JPanel attCard = Theme.card();
        attCard.setLayout(new GridLayout(5, 1, 0, 8));
        attCard.add(sectionHeader("📅 Attendance (This Year)"));
        attCard.add(statRow("Present",  String.valueOf(attStats[0]), Theme.SUCCESS));
        attCard.add(statRow("Absent",   String.valueOf(attStats[1]), Theme.DANGER));
        attCard.add(statRow("On Leave", String.valueOf(attStats[2]), Theme.WARNING));
        attCard.add(statRow("Half Day", String.valueOf(attStats[3]), Theme.INFO));
        p.add(attCard);

        // Salary summary
        double[] salStats = getSalaryStats();
        JPanel salCard = Theme.card();
        salCard.setLayout(new GridLayout(5, 1, 0, 8));
        salCard.add(sectionHeader("💰 Salary Summary"));
        salCard.add(statRow("Latest Net", String.format("PKR %,.0f", salStats[0]), Theme.SUCCESS));
        salCard.add(statRow("Basic",      String.format("PKR %,.0f", salStats[1]), Theme.TEXT_MAIN));
        salCard.add(statRow("Bonus",      String.format("PKR %,.0f", salStats[2]), Theme.ACCENT));
        salCard.add(statRow("Deductions", String.format("PKR %,.0f", salStats[3]), Theme.DANGER));
        p.add(salCard);

        // Leave balance summary
        Map<String, int[]> bal = balDAO.getBalance(emp.getEmpId());
        JPanel balCard = Theme.card();
        balCard.setLayout(new GridLayout(5, 1, 0, 8));
        balCard.add(sectionHeader("🏖 Leave Balance (" + java.time.LocalDate.now().getYear() + ")"));
        for (Map.Entry<String, int[]> e : bal.entrySet()) {
            if ("Unpaid".equals(e.getKey())) continue;
            int rem = e.getValue()[2];
            Color c = rem > 5 ? Theme.SUCCESS : rem > 0 ? Theme.WARNING : Theme.DANGER;
            balCard.add(statRow(e.getKey(), rem + " days left", c));
        }
        p.add(balCard);
        return p;
    }

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.TEXT_MAIN);
        return l;
    }

    private JPanel statRow(String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL); lbl.setForeground(Theme.TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13)); val.setForeground(color);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    // ── Attendance tab ────────────────────────────────────────────────────────

    private JPanel buildAttendanceTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        String[] cols = {"Date", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Attendance> atts = attDAO.getByEmployee(emp.getEmpId());
        for (Attendance a : atts) m.addRow(new Object[]{a.getAttDate(), a.getStatus()});

        JTable t = new JTable(m);
        Theme.styleTable(t);
        t.getColumnModel().getColumn(1).setCellRenderer(statusCellRenderer());

        // Summary bar
        long present = atts.stream().filter(a -> "Present".equals(a.getStatus())).count();
        long absent  = atts.stream().filter(a -> "Absent".equals(a.getStatus())).count();
        long leave   = atts.stream().filter(a -> "Leave".equals(a.getStatus())).count();
        JLabel summary = new JLabel(String.format(
            "  Total: %d  |  ✅ Present: %d  |  ❌ Absent: %d  |  🏖 Leave: %d",
            atts.size(), present, absent, leave));
        summary.setFont(Theme.FONT_SMALL);
        summary.setForeground(Theme.TEXT_MUTED);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        p.add(Theme.scrollPane(t), BorderLayout.CENTER);
        p.add(summary, BorderLayout.SOUTH);
        return p;
    }

    // ── Salary tab ────────────────────────────────────────────────────────────

    private JPanel buildSalaryTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        String[] cols = {"Pay Month", "Basic Salary", "Bonus", "Deductions", "Net Salary", "Pay Date"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Salary> sals = salDAO.getByEmployee(emp.getEmpId());
        double totalNet = 0;
        for (Salary s : sals) {
            totalNet += s.getNetSalary();
            m.addRow(new Object[]{
                s.getPayMonth(),
                String.format("PKR %,.0f", s.getBasicSalary()),
                String.format("PKR %,.0f", s.getBonus()),
                String.format("PKR %,.0f", s.getDeductions()),
                String.format("PKR %,.0f", s.getNetSalary()),
                s.getPayDate()
            });
        }

        JTable t = new JTable(m);
        Theme.styleTable(t);
        // Highlight net column green
        t.getColumnModel().getColumn(4).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                        boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(tbl,value,sel,focus,row,col);
                    if (!sel) { setForeground(Theme.SUCCESS); setBackground(Theme.CARD_BG); }
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    return this;
                }
            });

        JLabel summary = new JLabel(String.format(
            "  %d salary records  |  Total paid: PKR %,.0f", sals.size(), totalNet));
        summary.setFont(Theme.FONT_SMALL);
        summary.setForeground(Theme.TEXT_MUTED);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        p.add(Theme.scrollPane(t), BorderLayout.CENTER);
        p.add(summary, BorderLayout.SOUTH);
        return p;
    }

    // ── Leave history tab ─────────────────────────────────────────────────────

    private JPanel buildLeaveTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        String[] cols = {"Leave ID", "Type", "Start", "End", "Days", "Reason", "Status", "Applied On"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<LeaveApplication> leaves = leaveDAO.getByEmployee(emp.getEmpId());
        for (LeaveApplication l : leaves) {
            long days = 0;
            try {
                java.time.LocalDate s = java.time.LocalDate.parse(l.getStartDate());
                java.time.LocalDate e = java.time.LocalDate.parse(l.getEndDate());
                days = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
            } catch (Exception ignored) {}
            m.addRow(new Object[]{l.getLeaveId(), l.getLeaveType(), l.getStartDate(),
                l.getEndDate(), days+"d", l.getReason(), l.getStatus(), l.getAppliedOn()});
        }

        JTable t = new JTable(m);
        Theme.styleTable(t);
        t.getColumnModel().getColumn(6).setCellRenderer(statusCellRenderer());
        t.getColumnModel().getColumn(5).setPreferredWidth(160);

        p.add(Theme.scrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── Leave balance tab ─────────────────────────────────────────────────────

    private JPanel buildLeaveBalanceTab() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Leave Balance for " + java.time.LocalDate.now().getYear());
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        p.add(title, BorderLayout.NORTH);

        Map<String, int[]> bal = balDAO.getBalance(emp.getEmpId());
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setBackground(Theme.BG);

        Color[] colors = {Theme.PRIMARY, Theme.ACCENT, Theme.WARNING, Theme.INFO};
        String[] icons = {"🌴", "🏥", "📅", "💸"};
        int i = 0;
        for (Map.Entry<String, int[]> e : bal.entrySet()) {
            int quota = e.getValue()[0];
            int used  = e.getValue()[1];
            int rem   = e.getValue()[2];
            Color c   = colors[i % colors.length];

            JPanel card = Theme.card();
            card.setLayout(new BorderLayout(0, 8));

            JLabel icon = new JLabel(icons[i % icons.length] + "  " + e.getKey());
            icon.setFont(new Font("Segoe UI", Font.BOLD, 13));
            icon.setForeground(c);

            JLabel remLbl = new JLabel(String.valueOf("Unpaid".equals(e.getKey()) ? "∞" : rem));
            remLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
            remLbl.setForeground(c);

            JLabel detail = new JLabel(
                "Unpaid".equals(e.getKey()) ? "Unlimited" :
                "Used: " + used + " / " + quota + " days");
            detail.setFont(Theme.FONT_SMALL);
            detail.setForeground(Theme.TEXT_MUTED);

            // Progress bar
            JPanel progress = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(51,65,85));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    if (quota > 0 && !e.getKey().equals("Unpaid")) {
                        int filled = (int)((double) used / quota * getWidth());
                        Color fillColor = used >= quota ? Theme.DANGER : used > quota*0.7 ? Theme.WARNING : c;
                        g2.setColor(fillColor);
                        g2.fillRoundRect(0, 0, Math.min(filled, getWidth()), getHeight(), 4, 4);
                    }
                    g2.dispose();
                }
            };
            progress.setPreferredSize(new Dimension(0, 6));
            progress.setOpaque(false);

            card.add(icon,     BorderLayout.NORTH);
            card.add(remLbl,   BorderLayout.CENTER);
            JPanel bottom = new JPanel(new BorderLayout(0,4));
            bottom.setOpaque(false);
            bottom.add(detail,   BorderLayout.NORTH);
            bottom.add(progress, BorderLayout.SOUTH);
            card.add(bottom, BorderLayout.SOUTH);
            cards.add(card);
            i++;
        }
        p.add(cards, BorderLayout.CENTER);

        // Note
        JLabel note = new JLabel("  * Balances are based on approved leaves for the current calendar year.");
        note.setFont(Theme.FONT_SMALL); note.setForeground(new Color(71,85,105));
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private javax.swing.table.TableCellRenderer statusCellRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if (!sel) {
                    switch (value == null ? "" : value.toString()) {
                        case "Present","Approved" -> { setForeground(Theme.SUCCESS); setBackground(new Color(22,101,52,70)); }
                        case "Absent","Rejected"  -> { setForeground(Theme.DANGER);  setBackground(new Color(153,27,27,70)); }
                        case "Leave","Pending"    -> { setForeground(Theme.WARNING); setBackground(new Color(120,53,15,70)); }
                        case "Half Day"           -> { setForeground(Theme.INFO);    setBackground(new Color(7,89,133,70)); }
                        default -> { setForeground(Theme.TEXT_MUTED); setBackground(Theme.CARD_BG); }
                    }
                }
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return this;
            }
        };
    }

    private int[] getAttendanceStats() {
        int[] s = new int[4];
        for (Attendance a : attDAO.getByEmployee(emp.getEmpId())) {
            switch (a.getStatus()) {
                case "Present"  -> s[0]++;
                case "Absent"   -> s[1]++;
                case "Leave"    -> s[2]++;
                case "Half Day" -> s[3]++;
            }
        }
        return s;
    }

    private double[] getSalaryStats() {
        double[] s = new double[4];
        List<Salary> sals = salDAO.getByEmployee(emp.getEmpId());
        if (!sals.isEmpty()) {
            Salary latest = sals.get(0);
            s[0] = latest.getNetSalary();
            s[1] = latest.getBasicSalary();
            s[2] = latest.getBonus();
            s[3] = latest.getDeductions();
        }
        return s;
    }
}
