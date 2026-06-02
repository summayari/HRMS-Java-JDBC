package hrms.gui;

import hrms.auth.SessionManager;
import hrms.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainWindow extends JFrame {

    private final JPanel      contentArea;
    private final ButtonGroup navGroup = new ButtonGroup();
    private       JLabel      clockLabel;

    public MainWindow() {
        super("HRMS v5 – Role-Based Human Resource Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1380, 840);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildTopBar(),   BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG);
        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);

        showRoleDefaultPanel();
        startClock();
    }

    private void showRoleDefaultPanel() {
        User u = SessionManager.getInstance().getUser();
        if (u == null) { showPanel(new EmployeeDashboardPanel()); return; }
        switch (u.getRole()) {
            case "Admin"       -> showPanel(new AdminDashboardPanel());
            case "Director"    -> showPanel(new DirectorDashboardPanel());
            case "HR Manager"  -> showPanel(new HRDashboardPanel());
            case "Team Lead"   -> showPanel(new TeamLeadDashboardPanel());
            case "Accountant"  -> showPanel(new AccountantDashboardPanel());
            default            -> showPanel(new EmployeeDashboardPanel());
        }
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SECONDARY);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(51,65,85));
                g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0,52));
        bar.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));

        JTextField gs = Theme.field(26);
        gs.putClientProperty("JTextField.placeholderText","🔍  Search employees, leave, salary…");
        gs.addActionListener(e -> showPanel(new GlobalSearchPanel(gs.getText().trim())));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,10));
        left.setOpaque(false); left.add(gs);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI",Font.PLAIN,11));
        clockLabel.setForeground(Theme.TEXT_MUTED);
        right.add(clockLabel);

        User u = SessionManager.getInstance().getUser();
        if (u != null) {
            Color rc = Theme.getRoleColor(u.getRole());
            JLabel badge = new JLabel("  "+Theme.getRoleIcon(u.getRole())+" "+u.getUsername()+"  ");
            badge.setFont(new Font("Segoe UI",Font.BOLD,12));
            badge.setForeground(rc);
            badge.setOpaque(true);
            badge.setBackground(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),25));
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),80)),
                BorderFactory.createEmptyBorder(4,10,4,10)));
            right.add(badge);
            JLabel roleLbl = new JLabel(u.getRole());
            roleLbl.setFont(new Font("Segoe UI",Font.PLAIN,10));
            roleLbl.setForeground(Theme.TEXT_MUTED);
            right.add(roleLbl);
        }

        JButton btnPw = Theme.ghostBtn("🔒");
        btnPw.setToolTipText("Change Password");
        btnPw.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        btnPw.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        right.add(btnPw);

        JButton logoutBtn = Theme.dangerBtn("⏻  Logout");
        logoutBtn.setFont(new Font("Segoe UI",Font.BOLD,12));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?","Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                SessionManager.getInstance().logout();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginDialog login = new LoginDialog();
                    login.setVisible(true);
                    if (login.isLoggedIn()) new MainWindow().setVisible(true);
                });
            }
        });
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.SIDEBAR_BG);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(40,55,85));
                g2.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
                g2.dispose();
            }
        };
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(240,0));

        // Logo
        JPanel logo = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(30,20,80),getWidth(),getHeight(),new Color(6,10,20));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(240,72));
        logo.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));
        JLabel li = new JLabel("◈"); li.setFont(new Font("Segoe UI",Font.BOLD,28)); li.setForeground(Theme.PRIMARY);
        JLabel lt = new JLabel("<html><b style='font-size:14px;color:white'>HRMS</b><br><span style='font-size:9px;color:#94a3b8'>v5.0 — Role-Based System</span></html>");
        lt.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        logo.add(li,BorderLayout.WEST); logo.add(lt,BorderLayout.CENTER);
        sb.add(logo);

        // User card with role color
        User u = SessionManager.getInstance().getUser();
        if (u != null) {
            Color rc = Theme.getRoleColor(u.getRole());
            JPanel uc = new JPanel(new BorderLayout(10,0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setColor(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),18));
                    g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(rc);
                    g2.fillRect(0,0,3,getHeight());
                    g2.dispose();
                }
            };
            uc.setOpaque(false);
            uc.setMaximumSize(new Dimension(240,66));
            uc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),40)),
                BorderFactory.createEmptyBorder(12,18,12,18)));
            JLabel av = new JLabel(Theme.getRoleIcon(u.getRole()),SwingConstants.CENTER);
            av.setFont(new Font("Segoe UI Emoji",Font.PLAIN,20));
            av.setPreferredSize(new Dimension(36,36));
            uc.add(av,BorderLayout.WEST);
            JPanel ui = new JPanel(new GridLayout(2,1)); ui.setOpaque(false);
            JLabel un = new JLabel(u.getUsername()); un.setFont(new Font("Segoe UI",Font.BOLD,12)); un.setForeground(Theme.TEXT_MAIN);
            JLabel ur = new JLabel(u.getRole());     ur.setFont(new Font("Segoe UI",Font.PLAIN,10)); ur.setForeground(rc);
            ui.add(un); ui.add(ur);
            uc.add(ui,BorderLayout.CENTER);
            sb.add(uc);
        }

        sb.add(Box.createVerticalStrut(8));

        // Build role-specific navigation
        if (u != null) buildRoleNav(sb, u);

        sb.add(Box.createVerticalStrut(6));
        sb.add(sectionLabel("ACCOUNT"));
        addNav(sb,"🔒","Change Password",false,Theme.TEXT_MUTED);

        sb.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("<html><center>HRMS v5 • Role-Based Access</center></html>",SwingConstants.CENTER);
        footer.setFont(Theme.FONT_SMALL); footer.setForeground(new Color(50,65,90));
        footer.setAlignmentX(CENTER_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(10,8,16,8));
        sb.add(footer);
        return sb;
    }

    private void buildRoleNav(JPanel sb, User u) {
        switch (u.getRole()) {
            case "Admin" -> {
                Color c = Theme.ADMIN_COLOR;
                sb.add(sectionLabel("⚙️  ADMIN PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"👥","Employees",false,c);
                addNav(sb,"🏢","Departments",false,c);
                addNav(sb,"💰","Salaries",false,c);
                addNav(sb,"📅","Attendance",false,c);
                addNav(sb,"📋","Leave Mgmt",false,c);
                addNav(sb,"🔔","Notifications",false,c);
                sb.add(Box.createVerticalStrut(4));
                sb.add(sectionLabel("TOOLS"));
                addNav(sb,"📊","Analytics",false,c);
                addNav(sb,"🎯","Recruitment",false,c);
                addNav(sb,"💳","Payroll Run",false,c);
                addNav(sb,"📜","Audit Log",false,c);
            }
            case "Director" -> {
                Color c = Theme.DIRECTOR_COLOR;
                sb.add(sectionLabel("👑  DIRECTOR PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"📊","Company Analytics",false,c);
                addNav(sb,"👥","All Employees",false,c);
                addNav(sb,"🏢","Departments",false,c);
                addNav(sb,"💰","Payroll Overview",false,c);
                addNav(sb,"🎯","Recruitment",false,c);
                addNav(sb,"📋","Leave Approvals",false,c);
                addNav(sb,"📜","Audit Log",false,c);
                addNav(sb,"🔔","Notifications",false,c);
            }
            case "HR Manager" -> {
                Color c = Theme.HR_COLOR;
                sb.add(sectionLabel("🧑‍💼  HR PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"👥","Employee Management",false,c);
                addNav(sb,"📋","Leave Management",false,c);
                addNav(sb,"📅","Attendance Tracking",false,c);
                addNav(sb,"🎯","Recruitment",false,c);
                addNav(sb,"🏢","Departments",false,c);
                addNav(sb,"🔔","Notifications",false,c);
            }
            case "Team Lead" -> {
                Color c = Theme.TEAMLEAD_COLOR;
                sb.add(sectionLabel("🎯  TEAM LEAD PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"👥","My Team",false,c);
                addNav(sb,"📅","Team Attendance",false,c);
                addNav(sb,"📋","Team Leave Requests",false,c);
                addNav(sb,"🎯","Recruitment",false,c);
                addNav(sb,"🔔","Notifications",false,c);
            }
            case "Accountant" -> {
                Color c = Theme.ACCOUNTANT_COLOR;
                sb.add(sectionLabel("💰  ACCOUNTS PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"💰","Salary Management",false,c);
                addNav(sb,"💳","Payroll Run",false,c);
                addNav(sb,"📊","Financial Reports",false,c);
                addNav(sb,"🧾","Expense Tracking",false,c);
                addNav(sb,"🔔","Notifications",false,c);
            }
            default -> {
                Color c = Theme.EMPLOYEE_COLOR;
                sb.add(sectionLabel("👤  MY PANEL"));
                addNav(sb,"🏠","Dashboard",true,c);
                addNav(sb,"📅","My Attendance",false,c);
                addNav(sb,"📋","My Leave",false,c);
                addNav(sb,"💰","My Salary",false,c);
                addNav(sb,"🔔","Notifications",false,c);
            }
        }
    }

    private JLabel sectionLabel(String t) {
        JLabel l = new JLabel("  "+t);
        l.setFont(new Font("Segoe UI",Font.BOLD,9));
        l.setForeground(new Color(71,85,105));
        l.setMaximumSize(new Dimension(240,22));
        l.setBorder(BorderFactory.createEmptyBorder(4,8,2,8));
        return l;
    }

    private void addNav(JPanel sb, String icon, String label, boolean selected, Color accent) {
        JToggleButton btn = new JToggleButton(icon+"  "+label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(isSelected()){
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),25));
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                    g2.setColor(accent);
                    g2.fillRoundRect(0,5,4,getHeight()-10,4,4);
                } else if(getModel().isRollover()){
                    g2.setColor(new Color(255,255,255,8));
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btn.setForeground(selected ? Color.WHITE : new Color(148,163,184));
        btn.setBorder(BorderFactory.createEmptyBorder(10,18,10,16));
        btn.setMaximumSize(new Dimension(240,42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setSelected(selected);
        navGroup.add(btn); sb.add(btn); sb.add(Box.createVerticalStrut(1));
        btn.getModel().addChangeListener(e -> btn.setForeground(btn.isSelected() ? Color.WHITE : new Color(148,163,184)));
        btn.addActionListener(e -> handleNav(label));
    }

    private void handleNav(String label) {
        User u = SessionManager.getInstance().getUser();
        String role = (u != null) ? u.getRole() : "Employee";
        switch (label) {
            // Dashboards
            case "Dashboard"           -> showRoleDefaultPanel();
            // Shared
            case "Notifications"       -> showPanel(new NotificationsPanel());
            case "Change Password"     -> new ChangePasswordDialog(this).setVisible(true);
            case "Audit Log"           -> showPanel(new AuditLogPanel());
            case "Recruitment"         -> showPanel(new RecruitmentPanel());
            case "Departments"         -> showPanel(new DepartmentPanel());
            // Admin
            case "Employees"           -> showPanel(new EmployeePanel());
            case "Salaries"            -> showPanel(new SalaryPanel());
            case "Attendance"          -> showPanel(new AttendancePanel());
            case "Leave Mgmt"          -> showPanel(new LeavePanel());
            case "Analytics"           -> showPanel(new AnalyticsDashboard());
            case "Payroll Run"         -> new PayrollRunDialog(this).setVisible(true);
            // Director
            case "Company Analytics"   -> showPanel(new AnalyticsDashboard());
            case "All Employees"       -> showPanel(new EmployeePanel());
            case "Payroll Overview"    -> showPanel(new SalaryPanel());
            case "Leave Approvals"     -> showPanel(new LeavePanel());
            // HR
            case "Employee Management" -> showPanel(new EmployeePanel());
            case "Leave Management"    -> showPanel(new LeavePanel());
            case "Attendance Tracking" -> showPanel(new AttendancePanel());
            // Team Lead
            case "My Team"             -> showPanel(new TeamMembersPanel());
            case "Team Attendance"     -> showPanel(new AttendancePanel());
            case "Team Leave Requests" -> showPanel(new LeavePanel());
            // Accountant
            case "Salary Management"   -> showPanel(new SalaryPanel());
            case "Financial Reports"   -> showPanel(new FinancialReportsPanel());
            case "Expense Tracking"    -> showPanel(new ExpenseTrackingPanel());
            // Employee
            case "My Attendance"       -> showPanel(new AttendancePanel());
            case "My Leave"            -> showPanel(new LeavePanel());
            case "My Salary"           -> showPanel(new SalaryPanel());
        }
    }

    public void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void startClock() {
        Timer t = new Timer(1000, e ->
            clockLabel.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEE dd MMM  hh:mm a"))));
        t.setInitialDelay(0); t.start();
    }
}
