package hrms.gui;

import hrms.auth.SessionManager;
import hrms.dao.UserDAO;
import hrms.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Full-screen split login screen.
 * Left  = animated branding panel.
 * Right = login form.
 */
public class LoginDialog extends JDialog {

    private boolean loggedIn = false;
    private final UserDAO userDAO = new UserDAO();

    // Form fields
    private JTextField     fUsername;
    private JPasswordField fPassword;
    private JComboBox<String> cRole;
    private JLabel         lblError;

    public LoginDialog() {
        super((Frame) null, "HRMS Login", true);
        setUndecorated(true);
        setSize(900, 580);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        buildUI();
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(99,102,241,80), 1));
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        root.setOpaque(true);

        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);

        setContentPane(root);

        // Allow dragging the undecorated window
        Point[] drag = {null};
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (drag[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - drag[0].x,
                                loc.y + e.getY() - drag[0].y);
                }
            }
        });
    }

    // ── Left branding panel ───────────────────────────────────────────────────

    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0,            new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(49, 46, 129));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());

                // Decorative circles
                g2.setColor(new Color(99,102,241,25));
                g2.fillOval(-60,-60,220,220);
                g2.setColor(new Color(99,102,241,15));
                g2.fillOval(getWidth()-120, getHeight()-120, 240, 240);
                g2.setColor(new Color(16,185,129,20));
                g2.fillOval(20, getHeight()/2-60, 120, 120);

                // Thin grid dots for texture
                g2.setColor(new Color(255,255,255,12));
                for (int x = 20; x < getWidth(); x += 28)
                    for (int y = 20; y < getHeight(); y += 28)
                        g2.fillOval(x,y,2,2);

                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(340, 0));
        p.setOpaque(true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8,30,8,30);

        // Logo/Icon
        JLabel icon = new JLabel("⬡", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 52));
        icon.setForeground(new Color(99,102,241));
        gc.gridy = 0;
        p.add(icon, gc);

        // Title
        JLabel title = new JLabel("HRMS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(Color.WHITE);
        gc.gridy = 1;
        p.add(title, gc);

        // Subtitle
        JLabel sub = new JLabel("<html><center>Human Resource<br>Management System</center></html>", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(148,163,184));
        gc.gridy = 2;
        gc.insets = new Insets(4,30,24,30);
        p.add(sub, gc);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(99,102,241,60));
        gc.gridy = 3;
        gc.insets = new Insets(0,30,20,30);
        p.add(sep, gc);

        // Features list
        String[] features = {"👥  Employee Management", "📊  Real-time Dashboard",
                             "💰  Payroll & Salaries", "📅  Attendance Tracking",
                             "📋  Leave Management"};
        for (int i = 0; i < features.length; i++) {
            JLabel fl = new JLabel(features[i]);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fl.setForeground(new Color(165,180,252));
            gc.gridy = 4 + i;
            gc.insets = new Insets(4, 36, 4, 30);
            p.add(fl, gc);
        }

        // Version badge
        JLabel ver = new JLabel("v2.0  •  BCE 6A – DBMS Lab", SwingConstants.CENTER);
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(new Color(71,85,105));
        gc.gridy = 10;
        gc.insets = new Insets(30,30,0,30);
        p.add(ver, gc);

        return p;
    }

    // ── Right form panel ──────────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BG_SECONDARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_SECONDARY);
        form.setPreferredSize(new Dimension(380, 460));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Close button (top-right of full dialog)
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setBackground(Theme.BG_SECONDARY);
        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(new Color(148,163,184));
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(Theme.DANGER); }
            public void mouseExited(MouseEvent e)  { closeBtn.setForeground(new Color(148,163,184)); }
        });
        topRow.add(closeBtn);
        gc.gridy = 0; gc.insets = new Insets(0,0,10,0);
        form.add(topRow, gc);

        // Heading
        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(Theme.TEXT_MAIN);
        gc.gridy = 1; gc.insets = new Insets(0,0,4,0);
        form.add(heading, gc);

        JLabel subheading = new JLabel("Sign in to your HRMS account");
        subheading.setFont(Theme.FONT_BODY);
        subheading.setForeground(Theme.TEXT_MUTED);
        gc.gridy = 2; gc.insets = new Insets(0,0,28,0);
        form.add(subheading, gc);

        // Role selector
        gc.gridy = 3; gc.insets = new Insets(0,0,6,0);
        form.add(fieldLabel("Login as"), gc);

        cRole = new JComboBox<>(new String[]{"Admin","Employee","HR Manager","Team Lead","Accountant","Director"});
        cRole.setFont(Theme.FONT_BODY);
        cRole.setBackground(Theme.INPUT_BG);
        cRole.setForeground(Theme.TEXT_MAIN);
        ((JLabel)cRole.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        gc.gridy = 4; gc.insets = new Insets(0,0,16,0);
        form.add(cRole, gc);

        // Username
        gc.gridy = 5; gc.insets = new Insets(0,0,6,0);
        form.add(fieldLabel("Username"), gc);

        fUsername = Theme.field(20);
        fUsername.putClientProperty("JTextField.placeholderText", "Enter your username");
        gc.gridy = 6; gc.insets = new Insets(0,0,16,0);
        form.add(fUsername, gc);

        // Password
        gc.gridy = 7; gc.insets = new Insets(0,0,6,0);
        form.add(fieldLabel("Password"), gc);

        JPanel pwRow = new JPanel(new BorderLayout(0,0));
        pwRow.setBackground(Theme.BG_SECONDARY);
        fPassword = Theme.passwordField(20);
        pwRow.add(fPassword, BorderLayout.CENTER);
        gc.gridy = 8; gc.insets = new Insets(0,0,8,0);
        form.add(pwRow, gc);

        // Show/hide password toggle
        JCheckBox showPw = new JCheckBox("Show password");
        showPw.setBackground(Theme.BG_SECONDARY);
        showPw.setForeground(Theme.TEXT_MUTED);
        showPw.setFont(Theme.FONT_SMALL);
        showPw.addActionListener(e ->
            fPassword.setEchoChar(showPw.isSelected() ? '\0' : '•'));
        gc.gridy = 9; gc.insets = new Insets(0,0,20,0);
        form.add(showPw, gc);

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(Theme.FONT_SMALL);
        lblError.setForeground(Theme.DANGER);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 10; gc.insets = new Insets(0,0,10,0);
        form.add(lblError, gc);

        // Login button
        JButton btnLogin = Theme.primaryBtn("Sign In  →");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(300, 44));
        gc.gridy = 11; gc.insets = new Insets(0,0,16,0);
        form.add(btnLogin, gc);

        // Default credentials hint
        JLabel hint = new JLabel("<html><center><font color='#475569'>Hint: admin / admin123 &nbsp;|&nbsp; emp001 / emp123</font></center></html>", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        gc.gridy = 12; gc.insets = new Insets(0,0,0,0);
        form.add(hint, gc);

        // Wire up login
        ActionListener loginAction = e -> doLogin();
        btnLogin.addActionListener(loginAction);
        fPassword.addActionListener(loginAction);
        fUsername.addActionListener(e -> fPassword.requestFocus());

        outer.add(form);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    // ── Auth logic ────────────────────────────────────────────────────────────

    private void doLogin() {
        String username = fUsername.getText().trim();
        String password = new String(fPassword.getPassword()).trim();
        String role     = (String) cRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        lblError.setForeground(new Color(251,191,36));
        lblError.setText("Authenticating…");

        // Validate on background thread to avoid EDT freeze
        SwingWorker<User,Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return userDAO.authenticate(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user == null) {
                        showError("Invalid credentials. Please try again.");
                        fPassword.setText("");
                        fPassword.requestFocus();
                    } else if (!user.getRole().equalsIgnoreCase(role)) {
                        showError("This account is not registered as " + role + ".");
                        fPassword.setText("");
                    } else {
                        userDAO.recordLogin(user.getUserId());
                        SessionManager.getInstance().setUser(user);
                        loggedIn = true;
                        dispose();
                    }
                } catch (Exception ex) {
                    showError("Authentication error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        lblError.setForeground(Theme.DANGER);
        lblError.setText(msg);
    }

    public boolean isLoggedIn() { return loggedIn; }
}
