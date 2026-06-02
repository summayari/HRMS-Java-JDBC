package hrms.util;

import hrms.gui.Theme;
import javax.swing.*;
import java.awt.*;

/** Factory for consistently dark-themed modal dialogs. */
public class DialogHelper {

    /**
     * Creates a dark-themed dialog with a gradient header.
     * @param parent   parent component
     * @param title    dialog title
     * @param iconText emoji or single char to show left of title
     * @param w        width
     * @param h        height
     */
    public static JDialog create(Component parent, String title, String iconText, int w, int h) {
        Window win = SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = (win instanceof Frame)
            ? new JDialog((Frame) win, title, true)
            : new JDialog((Dialog) win, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(parent);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return dlg;
    }

    /** Builds the gradient header panel for a dialog. */
    public static JPanel header(String iconText, String titleText, String subtitle, Color accent) {
        JPanel hdr = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200),
                    getWidth(), getHeight(), Theme.BG_SECONDARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        hdr.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(subtitle.isEmpty() ? 1 : 2, 1, 0, 2));
        textPanel.setOpaque(false);
        JLabel ttl = new JLabel(titleText);
        ttl.setFont(Theme.FONT_HEADER);
        ttl.setForeground(Color.WHITE);
        textPanel.add(ttl);
        if (!subtitle.isEmpty()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(Theme.FONT_SMALL);
            sub.setForeground(new Color(165, 180, 252));
            textPanel.add(sub);
        }
        hdr.add(textPanel, BorderLayout.CENTER);
        return hdr;
    }

    /** Standard form panel with dark background. */
    public static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_SECONDARY);
        p.setBorder(BorderFactory.createEmptyBorder(20, 26, 10, 26));
        return p;
    }

    /** Root panel for a dialog (dark BG, BorderLayout). */
    public static JPanel rootPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_SECONDARY);
        return p;
    }

    /** Standard form label. */
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    /** Standard button row (right-aligned). */
    public static JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        row.setBackground(Theme.BG_SECONDARY);
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));
        for (JButton b : buttons) row.add(b);
        return row;
    }

    /** Standard GBC for form rows. */
    public static GridBagConstraints gbc(int x, int y, double weightX) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x; gc.gridy = y;
        gc.weightx = weightX;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        return gc;
    }
}
