package hrms.gui;

import hrms.db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Modal dialog shown on launch to enter SQL Server credentials. */
public class ConnectionDialog extends JDialog {

    private boolean connected = false;

    public ConnectionDialog(Frame parent) {
        super(parent, "HRMS – Connect to Database", true);
        buildUI();
        setSize(480, 360);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setUndecorated(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_SECONDARY);

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(49,46,129),getWidth(),getHeight(),Theme.BG_SECONDARY);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));

        JLabel icon  = new JLabel("🗄️ ");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        JLabel title = new JLabel("Database Connection");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Connect to SQL Server before proceeding");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(165,180,252));

        JPanel hLeft = new JPanel(new GridLayout(2,1,0,2));
        hLeft.setOpaque(false);
        hLeft.add(title);
        hLeft.add(sub);

        JPanel hWrap = new JPanel(new BorderLayout(10,0));
        hWrap.setOpaque(false);
        hWrap.add(icon,  BorderLayout.WEST);
        hWrap.add(hLeft, BorderLayout.CENTER);
        header.add(hWrap, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_SECONDARY);
        form.setBorder(BorderFactory.createEmptyBorder(22, 30, 10, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField fServer = Theme.field(20); fServer.setText("localhost\\SQLEXPRESS:1433");
        JTextField fDB     = Theme.field(20); fDB.setText("HRMS_DB");
        JTextField fUser   = Theme.field(20); fUser.setText("sa");
        JPasswordField fPass = Theme.passwordField(20); fPass.setText("Admin123");

        String[]    labels = {"Server :", "Database :", "Username :", "Password :"};
        JComponent[] fields = {fServer, fDB, fUser, fPass};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Theme.TEXT_MUTED);
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 1;
            form.add(fields[i], gc);
        }
        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnRow.setBackground(Theme.BG_SECONDARY);
        JButton btnConnect = Theme.primaryBtn("Connect →");
        JButton btnExit    = Theme.dangerBtn("Exit");

        btnConnect.addActionListener(e -> {
            String server = fServer.getText().trim();
            String db     = fDB.getText().trim();
            String user   = fUser.getText().trim();
            String pass   = new String(fPass.getPassword());
            DBConnection.configure(server, db, user, pass);
            try {
                DBConnection.getConnection();
                connected = true;
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Connection failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnExit.addActionListener(e -> System.exit(0));

        btnRow.add(btnExit);
        btnRow.add(btnConnect);
        root.add(btnRow, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public boolean isConnected() { return connected; }
}
