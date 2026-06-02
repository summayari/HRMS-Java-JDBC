package hrms.gui;

import hrms.db.DBConnection;
import hrms.util.CsvExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuditLogPanel extends JPanel {

    private DefaultTableModel model;
    private final JTextField searchField;

    private static final String[] COLS = {"Log ID", "User", "Module", "Action", "Details", "Time"};

    public AuditLogPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        searchField = Theme.field(16);
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(7,89,133),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16,20,14,20));
        JLabel title = new JLabel("📜  System Audit Log");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnSearch  = Theme.primaryBtn("Search");
        JButton btnAll     = Theme.ghostBtn("Show All");
        JButton btnExport  = Theme.ghostBtn("↓ Export CSV");
        right.add(searchField); right.add(btnSearch); right.add(btnAll); right.add(btnExport);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> search(searchField.getText().trim()));
        searchField.addActionListener(e -> btnSearch.doClick());
        btnAll.addActionListener(e -> { searchField.setText(""); loadAll(); });
        btnExport.addActionListener(e -> CsvExporter.export(this, model, "AuditLog"));
        loadAll();
    }

    private void loadAll() {
        model.setRowCount(0);
        String sql = "SELECT TOP 500 Log_ID,Username,Module,Action,Details,"
                   + "CONVERT(varchar,LogTime,120) FROM SystemAuditLog ORDER BY Log_ID DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),
                    rs.getString(4),rs.getString(5),rs.getString(6)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void search(String kw) {
        model.setRowCount(0);
        if (kw.isEmpty()) { loadAll(); return; }
        String sql = "SELECT TOP 500 Log_ID,Username,Module,Action,Details,"
                   + "CONVERT(varchar,LogTime,120) FROM SystemAuditLog "
                   + "WHERE Username LIKE ? OR Module LIKE ? OR Action LIKE ? OR Details LIKE ? "
                   + "ORDER BY Log_ID DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + kw + "%";
            ps.setString(1,p); ps.setString(2,p); ps.setString(3,p); ps.setString(4,p);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),
                    rs.getString(4),rs.getString(5),rs.getString(6)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
