package hrms.gui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

/** Central styling for HRMS – v5 Modern Dark Theme. */
public class Theme {

    // ── Core Palette ─────────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(99,  102, 241);
    public static final Color PRIMARY_DARK  = new Color(67,  56,  202);
    public static final Color PRIMARY_LIGHT = new Color(165, 180, 252);
    public static final Color ACCENT        = new Color(16,  185, 129);
    public static final Color ACCENT2       = new Color(245, 158,  11);
    public static final Color BG            = new Color(8,   12,  28);
    public static final Color BG_SECONDARY  = new Color(22,  32,  52);
    public static final Color CARD_BG       = new Color(22,  32,  52);
    public static final Color SIDEBAR_BG    = new Color(6,   10,  20);
    public static final Color SIDEBAR_SEL   = new Color(99,  102, 241);
    public static final Color TEXT_MAIN     = new Color(241, 245, 249);
    public static final Color TEXT_MUTED    = new Color(148, 163, 184);
    public static final Color INPUT_BG      = new Color(40,  55,  80);
    public static final Color INPUT_FOCUS   = new Color(99,  102, 241);
    public static final Color DANGER        = new Color(239,  68,  68);
    public static final Color SUCCESS       = new Color(34,  197,  94);
    public static final Color WARNING       = new Color(251, 191,  36);
    public static final Color INFO          = new Color(56,  189, 248);

    // Role colors
    public static final Color ADMIN_COLOR       = new Color(239,  68,  68);
    public static final Color DIRECTOR_COLOR    = new Color(168,  85, 247);
    public static final Color HR_COLOR          = new Color(59,  130, 246);
    public static final Color TEAMLEAD_COLOR    = new Color(16,  185, 129);
    public static final Color ACCOUNTANT_COLOR  = new Color(245, 158,  11);
    public static final Color EMPLOYEE_COLOR    = new Color(148, 163, 184);

    // Fonts
    public static final Font FONT_HERO   = new Font("Segoe UI", Font.BOLD,  32);
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN, 12);

    // ── Role helpers ─────────────────────────────────────────────────────────

    public static Color getRoleColor(String role) {
        if (role == null) return EMPLOYEE_COLOR;
        switch (role) {
            case "Admin":      return ADMIN_COLOR;
            case "Director":   return DIRECTOR_COLOR;
            case "HR Manager": return HR_COLOR;
            case "Team Lead":  return TEAMLEAD_COLOR;
            case "Accountant": return ACCOUNTANT_COLOR;
            default:           return EMPLOYEE_COLOR;
        }
    }

    public static String getRoleIcon(String role) {
        if (role == null) return "👤";
        switch (role) {
            case "Admin":      return "⚙️";
            case "Director":   return "👑";
            case "HR Manager": return "🧑‍💼";
            case "Team Lead":  return "🎯";
            case "Accountant": return "💰";
            default:           return "👤";
        }
    }

    // ── Global L&F ───────────────────────────────────────────────────────────

    public static void applyGlobal() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        UIManager.put("Table.font",             FONT_BODY);
        UIManager.put("TableHeader.font",       new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("TextField.font",         FONT_BODY);
        UIManager.put("PasswordField.font",     FONT_BODY);
        UIManager.put("ComboBox.font",          FONT_BODY);
        UIManager.put("Label.font",             FONT_BODY);
        UIManager.put("Button.font",            FONT_BODY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);

        // Scrollbar styling
        UIManager.put("ScrollBar.thumb",            BG_SECONDARY);
        UIManager.put("ScrollBar.thumbHighlight",   PRIMARY);
        UIManager.put("ScrollBar.thumbShadow",      BG);
        UIManager.put("ScrollBar.track",            BG);
        UIManager.put("ScrollBar.width",            8);

        // ToolTip
        UIManager.put("ToolTip.background", new Color(30, 42, 68));
        UIManager.put("ToolTip.foreground", TEXT_MAIN);
        UIManager.put("ToolTip.font",       FONT_SMALL);
        UIManager.put("ToolTip.border",
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1));
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    public static JButton primaryBtn(String text) {
        return roundBtn(text, PRIMARY, new Color(118, 120, 255), PRIMARY_DARK);
    }
    public static JButton accentBtn(String text) {
        return roundBtn(text, ACCENT, new Color(52, 211, 153), new Color(5, 150, 105));
    }
    public static JButton dangerBtn(String text) {
        return roundBtn(text, DANGER, new Color(252, 90, 90), new Color(185, 28, 28));
    }
    public static JButton warningBtn(String text) {
        return roundBtn(text, new Color(217, 119, 6), new Color(245, 158, 11), new Color(180, 83, 9));
    }
    public static JButton infoBtn(String text) {
        return roundBtn(text, INFO, new Color(100, 210, 255), new Color(14, 165, 233));
    }
    public static JButton purpleBtn(String text) {
        return roundBtn(text, new Color(168, 85, 247), new Color(192, 132, 252), new Color(126, 34, 206));
    }

    public static JButton ghostBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setForeground(TEXT_MUTED);
        b.setFont(FONT_BODY); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton roundBtn(String text, Color normal, Color hover, Color pressed) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? pressed
                        : getModel().isRollover() ? hover : normal;
                // subtle glow on hover
                if (getModel().isRollover()) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
                    g2.fillRoundRect(-3, -3, getWidth()+6, getHeight()+6, 14, 14);
                }
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return b;
    }

    // ── Fields & Combos ───────────────────────────────────────────────────────

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols) {
            boolean focused = false;
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                    public void focusLost(java.awt.event.FocusEvent e)   { focused = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // focus ring
                if (focused) {
                    g2.setColor(new Color(INPUT_FOCUS.getRed(), INPUT_FOCUS.getGreen(), INPUT_FOCUS.getBlue(), 120));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false); f.setForeground(TEXT_MAIN); f.setCaretColor(PRIMARY_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setFont(FONT_BODY);
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols) {
            boolean focused = false;
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                    public void focusLost(java.awt.event.FocusEvent e)   { focused = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (focused) {
                    g2.setColor(new Color(INPUT_FOCUS.getRed(), INPUT_FOCUS.getGreen(), INPUT_FOCUS.getBlue(), 120));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false); f.setForeground(TEXT_MAIN); f.setCaretColor(PRIMARY_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setFont(FONT_BODY);
        return f;
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(INPUT_BG); cb.setForeground(TEXT_MAIN); cb.setFont(FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? PRIMARY : INPUT_BG);
                setForeground(isSelected ? Color.WHITE : TEXT_MAIN);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return cb;
    }

    // ── Cards ─────────────────────────────────────────────────────────────────

    public static JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, CARD_BG,
                    getWidth(), getHeight(),
                    new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                // top accent bar
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(accent);
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(FONT_SMALL);
        lblLbl.setForeground(TEXT_MUTED);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(valLbl, BorderLayout.WEST); top.add(dot, BorderLayout.EAST);
        card.add(top, BorderLayout.CENTER);
        card.add(lblLbl, BorderLayout.SOUTH);
        return card;
    }

    /** Card with no title. */
    public static JPanel card() { return card(""); }

    public static JPanel card(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(71, 85, 105, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        if (title != null && !title.isEmpty()) {
            JLabel t = new JLabel(title);
            t.setFont(FONT_HEADER); t.setForeground(TEXT_MAIN);
            t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(71, 85, 105, 80)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));
            card.add(t, BorderLayout.NORTH);
        }
        return card;
    }

    public static JPanel actionCard(String icon, String title, String desc, Color accent, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(10, 4)) {
            boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                    public void mouseClicked(java.awt.event.MouseEvent e) { if (action != null) action.run(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovered
                    ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35)
                    : CARD_BG;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hovered ? 140 : 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLbl.setPreferredSize(new Dimension(40, 40));
        card.add(iconLbl, BorderLayout.WEST);
        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 2)); txt.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 13)); t.setForeground(TEXT_MAIN);
        JLabel d = new JLabel(desc);  d.setFont(FONT_SMALL); d.setForeground(TEXT_MUTED);
        txt.add(t); txt.add(d); card.add(txt, BorderLayout.CENTER);
        JLabel arr = new JLabel("→");
        arr.setFont(new Font("Segoe UI", Font.BOLD, 16));
        arr.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        card.add(arr, BorderLayout.EAST);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    public static void styleTable(JTable t) {
        t.setBackground(CARD_BG);
        t.setForeground(TEXT_MAIN);
        t.setSelectionBackground(new Color(99, 102, 241, 60));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(FONT_BODY);
        t.getTableHeader().setBackground(new Color(30, 42, 68));
        t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(71, 85, 105)));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                comp.setBackground(sel
                    ? new Color(99, 102, 241, 60)
                    : (r % 2 == 0 ? CARD_BG : new Color(28, 40, 62)));
                comp.setForeground(sel ? Color.WHITE : TEXT_MAIN);
                if (comp instanceof JLabel)
                    ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });
    }

    public static JScrollPane scrollPane(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105, 80)));
        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setBackground(BG);
        bar.setForeground(new Color(71, 85, 105));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor      = new Color(71, 85, 105);
                thumbHighlightColor = PRIMARY;
                trackColor      = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? PRIMARY : thumbColor);
                g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(trackColor);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        bar.setPreferredSize(new Dimension(8, 8));
    }

    // ── Page Header ───────────────────────────────────────────────────────────

    public static JPanel pageHeader(String icon, String title, String sub, Color accent) {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50),
                    getWidth(), getHeight(), BG);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle dot grid
                g2.setColor(new Color(255, 255, 255, 5));
                for (int x = 0; x < getWidth(); x += 24)
                    for (int y = 0; y < getHeight(); y += 24)
                        g2.fillOval(x, y, 2, 2);
                // left accent bar
                GradientPaint bar = new GradientPaint(
                    0, 0, accent, 0, getHeight(), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.setPaint(bar);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        hdr.setPreferredSize(new Dimension(0, 90));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLbl.setPreferredSize(new Dimension(48, 48));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 3)); info.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(FONT_TITLE); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sub);   s.setFont(FONT_SMALL);
        s.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
        info.add(t); info.add(s);

        JPanel left = new JPanel(new BorderLayout(12, 0)); left.setOpaque(false);
        left.add(iconLbl, BorderLayout.WEST); left.add(info, BorderLayout.CENTER);
        hdr.add(left, BorderLayout.WEST);
        return hdr;
    }

    // ── Helper labels ─────────────────────────────────────────────────────────

    /** Bold header label. */
    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_MAIN);
        return l;
    }

    /** Muted small label. */
    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    /** Badge label (small pill). */
    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 100));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(bg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    /** Divider panel (1px horizontal rule). */
    public static JPanel divider() {
        JPanel d = new JPanel();
        d.setOpaque(true);
        d.setBackground(new Color(71, 85, 105, 80));
        d.setPreferredSize(new Dimension(0, 1));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return d;
    }

    /** Styled JTextArea (multi-line input). */
    public static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(INPUT_BG);
        ta.setForeground(TEXT_MAIN);
        ta.setCaretColor(PRIMARY_LIGHT);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return ta;
    }
}
