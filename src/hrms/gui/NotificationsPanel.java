package hrms.gui;

import hrms.auth.SessionManager;
import hrms.dao.NotificationDAO;
import hrms.dao.NotificationDAO.Notification;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final NotificationDAO dao = new NotificationDAO();

    public NotificationsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(67,20,7),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));

        JLabel title = new JLabel("🔔  Notifications");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        JButton refresh = Theme.ghostBtn("↺ Refresh");
        refresh.addActionListener(e -> { removeAll(); build(); revalidate(); repaint(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false); right.add(refresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Cards
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        int empId = 0;
        if (!isAdmin && SessionManager.getInstance().getUser() != null)
            empId = SessionManager.getInstance().getUser().getEmpId();

        List<Notification> notes = isAdmin
            ? dao.getAdminNotifications()
            : dao.getEmployeeNotifications(empId);

        JPanel cardArea = new JPanel();
        cardArea.setLayout(new BoxLayout(cardArea, BoxLayout.Y_AXIS));
        cardArea.setBackground(Theme.BG);
        cardArea.setBorder(BorderFactory.createEmptyBorder(14, 20, 20, 20));

        if (notes.isEmpty()) {
            JLabel empty = new JLabel("✅  All clear — no notifications right now.", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BODY); empty.setForeground(Theme.TEXT_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            cardArea.add(empty);
        } else {
            for (Notification n : notes) {
                cardArea.add(buildCard(n));
                cardArea.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane sp = new JScrollPane(cardArea);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildCard(Notification n) {
        Color accent = switch (n.type) {
            case "danger"  -> Theme.DANGER;
            case "success" -> Theme.SUCCESS;
            case "warn"    -> Theme.WARNING;
            default        -> Theme.INFO;
        };

        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Left accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Subtle border
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Icon circle
        JLabel iconLbl = new JLabel(n.icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
        iconLbl.setPreferredSize(new Dimension(44, 44));
        iconLbl.setBorder(BorderFactory.createLineBorder(
            new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60), 1));
        card.add(iconLbl, BorderLayout.WEST);

        // Text
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setOpaque(false);
        JLabel titleLbl = new JLabel(n.title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(Theme.TEXT_MAIN);
        JLabel detailLbl = new JLabel(n.detail);
        detailLbl.setFont(Theme.FONT_SMALL);
        detailLbl.setForeground(Theme.TEXT_MUTED);
        text.add(titleLbl); text.add(detailLbl);
        card.add(text, BorderLayout.CENTER);

        // Badge
        JLabel badge = new JLabel("  " + n.type.toUpperCase() + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(accent);
        badge.setOpaque(true);
        badge.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60)),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        right.setOpaque(false); right.add(badge);
        card.add(right, BorderLayout.EAST);

        return card;
    }
}
