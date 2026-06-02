package hrms.gui;

import hrms.auth.SessionManager;
import hrms.dao.LeaveDAO;
import hrms.model.LeaveApplication;
import hrms.util.CsvExporter;
import hrms.util.DialogHelper;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class LeavePanel extends JPanel {

    private final LeaveDAO dao = new LeaveDAO();
    private final DefaultTableModel model;
    private final JTable            table;
    private final JLabel            summaryLabel;

    // Tab state
    private String currentFilter = "All";

    private static final String[] COLS = {
        "Leave ID", "Emp ID", "Employee", "Type", "Start", "End", "Days", "Reason", "Status", "Applied On"
    };

    public LeavePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(Theme.FONT_SMALL);
        summaryLabel.setForeground(Theme.TEXT_MUTED);

        add(buildHeader(),  BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(8).setCellRenderer(statusRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(44);
        table.getColumnModel().getColumn(7).setPreferredWidth(180);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        center.add(buildStatusTabs(), BorderLayout.NORTH);
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);
        loadAll();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(67,20,7),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        JPanel left = new JPanel(new GridLayout(2,1,0,3));
        left.setOpaque(false);
        JLabel title = new JLabel("📋  Leave Management");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        left.add(title); left.add(summaryLabel);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnApply  = Theme.primaryBtn("＋ Apply Leave");
        JButton btnExport = Theme.ghostBtn("↓ CSV");
        right.add(btnApply); right.add(btnExport);
        header.add(right, BorderLayout.EAST);

        btnApply.addActionListener(e -> showApplyDialog());
        btnExport.addActionListener(e -> CsvExporter.export(this, model, "LeaveApplications"));
        return header;
    }

    // ── Status filter tabs ────────────────────────────────────────────────────

    private JPanel buildStatusTabs() {
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        tabs.setBackground(Theme.BG);

        String[] filters = {"All", "Pending", "Approved", "Rejected"};
        Color[]  colors  = {Theme.TEXT_MUTED, Theme.WARNING, Theme.SUCCESS, Theme.DANGER};
        ButtonGroup bg = new ButtonGroup();

        for (int i = 0; i < filters.length; i++) {
            final String filter = filters[i];
            final Color  color  = colors[i];
            JToggleButton btn = new JToggleButton(filter) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected()) {
                        g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),40));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                        g2.setColor(color);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(255,255,255,8));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(filter.equals("All") ? color : Theme.TEXT_MUTED);
            btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setSelected(filter.equals("All"));

            final Color finalColor = color;
            btn.addActionListener(e -> { currentFilter = filter; btn.setForeground(finalColor); applyFilter(); });
            btn.getModel().addChangeListener(e -> btn.setForeground(btn.isSelected() ? finalColor : Theme.TEXT_MUTED));

            bg.add(btn);
            tabs.add(btn);
        }
        return tabs;
    }

    // ── Action bar ────────────────────────────────────────────────────────────

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));

        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (isAdmin) {
            JButton btnApprove = Theme.accentBtn("✔ Approve");
            JButton btnReject  = Theme.dangerBtn("✘ Reject");
            bar.add(btnApprove); bar.add(btnReject);
            btnApprove.addActionListener(e -> updateStatus("Approved"));
            btnReject.addActionListener(e  -> updateStatus("Rejected"));
        }

        JButton btnView    = Theme.primaryBtn("👁 View Details");
        JButton btnRefresh = Theme.ghostBtn("↺ Refresh");
        bar.add(btnView); bar.add(btnRefresh);
        btnView.addActionListener(e -> viewDetails());
        btnRefresh.addActionListener(e -> loadAll());
        return bar;
    }

    // ── Renderers ─────────────────────────────────────────────────────────────

    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if (!sel) {
                    switch (value == null ? "" : value.toString()) {
                        case "Approved" -> { lbl.setForeground(Theme.SUCCESS); lbl.setBackground(new Color(22,101,52,70)); }
                        case "Rejected" -> { lbl.setForeground(Theme.DANGER);  lbl.setBackground(new Color(153,27,27,70)); }
                        default         -> { lbl.setForeground(Theme.WARNING); lbl.setBackground(new Color(120,53,15,70)); }
                    }
                }
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return lbl;
            }
        };
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadAll() {
        model.setRowCount(0);
        List<LeaveApplication> all = dao.getAll();
        long pending  = all.stream().filter(l -> "Pending".equals(l.getStatus())).count();
        long approved = all.stream().filter(l -> "Approved".equals(l.getStatus())).count();
        long rejected = all.stream().filter(l -> "Rejected".equals(l.getStatus())).count();
        summaryLabel.setText(String.format("Total: %d  |  ⏳ Pending: %d  |  ✅ Approved: %d  |  ❌ Rejected: %d",
            all.size(), pending, approved, rejected));

        for (LeaveApplication l : all) addRow(l);
        applyFilter();
    }

    private void applyFilter() {
        // Show/hide rows based on tab — we reload to keep it simple
        List<LeaveApplication> all = dao.getAll();
        model.setRowCount(0);
        for (LeaveApplication l : all) {
            if ("All".equals(currentFilter) || currentFilter.equals(l.getStatus()))
                addRow(l);
        }
    }

    private void addRow(LeaveApplication l) {
        // Calculate days
        long days = 0;
        try {
            java.time.LocalDate s = java.time.LocalDate.parse(l.getStartDate());
            java.time.LocalDate e = java.time.LocalDate.parse(l.getEndDate());
            days = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
        } catch (Exception ignored) {}
        model.addRow(new Object[]{
            l.getLeaveId(), l.getEmpId(), l.getEmpName(), l.getLeaveType(),
            l.getStartDate(), l.getEndDate(), days + "d",
            l.getReason(), l.getStatus(), l.getAppliedOn()
        });
    }

    // ── View Details ──────────────────────────────────────────────────────────

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a leave application first."); return; }

        JDialog dlg = DialogHelper.create(this, "Leave Details", "", 420, 340);
        JPanel root = DialogHelper.rootPanel();
        String status = (String) model.getValueAt(row, 8);
        Color  accent = "Approved".equals(status) ? Theme.SUCCESS : "Rejected".equals(status) ? Theme.DANGER : Theme.WARNING;
        root.add(DialogHelper.header("📋", "Leave Application #" + model.getValueAt(row, 0), status, accent), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG_SECONDARY);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        String[][] rows = {
            {"Employee",   model.getValueAt(row, 2).toString()},
            {"Leave Type", model.getValueAt(row, 3).toString()},
            {"Start Date", model.getValueAt(row, 4).toString()},
            {"End Date",   model.getValueAt(row, 5).toString()},
            {"Duration",   model.getValueAt(row, 6).toString()},
            {"Reason",     model.getValueAt(row, 7).toString()},
            {"Status",     status},
            {"Applied On", model.getValueAt(row, 9).toString()},
        };
        for (int i = 0; i < rows.length; i++) {
            body.add(DialogHelper.label(rows[i][0]), DialogHelper.gbc(0, i, 0));
            JLabel val = new JLabel(rows[i][1] == null ? "—" : rows[i][1]);
            val.setForeground(i == 6 ? accent : Theme.TEXT_MAIN);
            val.setFont(i == 6 ? new Font("Segoe UI", Font.BOLD, 13) : Theme.FONT_BODY);
            body.add(val, DialogHelper.gbc(1, i, 1));
        }
        root.add(body, BorderLayout.CENTER);
        JButton close = Theme.primaryBtn("Close");
        root.add(DialogHelper.buttonRow(close), BorderLayout.SOUTH);
        close.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(root); dlg.setVisible(true);
    }

    // ── Approve / Reject ──────────────────────────────────────────────────────

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a leave application first."); return; }
        String current = (String) model.getValueAt(row, 8);
        if (!current.equals("Pending")) {
            JOptionPane.showMessageDialog(this, "Only Pending applications can be " + newStatus.toLowerCase() + "d."); return;
        }
        int id   = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            newStatus + " leave for " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.updateStatus(id, newStatus)) { JOptionPane.showMessageDialog(this, "Status → " + newStatus); loadAll(); }
            else JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Apply Leave Dialog ────────────────────────────────────────────────────

    private void showApplyDialog() {
        JDialog dlg = DialogHelper.create(this, "Apply for Leave", "", 460, 420);
        JPanel root = DialogHelper.rootPanel();
        root.add(DialogHelper.header("📅", "Apply for Leave", "Submit a new leave request", Theme.PRIMARY), BorderLayout.NORTH);

        JPanel form = DialogHelper.formPanel();

        // Pre-fill emp ID if employee role
        String preEmp = "";
        if (!SessionManager.getInstance().isAdmin()) {
            int empId = SessionManager.getInstance().getUser().getEmpId();
            if (empId > 0) preEmp = String.valueOf(empId);
        }
        JTextField fEmpId  = Theme.field(10); fEmpId.setText(preEmp);
        if (!preEmp.isEmpty()) fEmpId.setEditable(false);

        JComboBox<String> cbType  = Theme.combo("Casual", "Medical", "Annual", "Unpaid");
        JTextField fStart  = Theme.field(12);
        JTextField fEnd    = Theme.field(12);
        JTextArea  fReason = new JTextArea(3, 20);
        fReason.setFont(Theme.FONT_BODY);
        fReason.setBackground(Theme.INPUT_BG);
        fReason.setForeground(Theme.TEXT_MAIN);
        fReason.setCaretColor(Theme.PRIMARY_LIGHT);
        fReason.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71,85,105)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        fReason.setLineWrap(true); fReason.setWrapStyleWord(true);

        String today = java.time.LocalDate.now().toString();
        fStart.setText(today); fEnd.setText(today);

        String[] lbls  = {"Employee ID *", "Leave Type *", "Start Date *", "End Date *", "Reason"};
        Component[] ctrls = {fEmpId, cbType, fStart, fEnd, new JScrollPane(fReason)};
        for (int i = 0; i < lbls.length; i++) {
            form.add(DialogHelper.label(lbls[i]), DialogHelper.gbc(0, i, 0));
            GridBagConstraints gc = DialogHelper.gbc(1, i, 1);
            if (i == 4) gc.ipady = 30;
            form.add(ctrls[i], gc);
        }
        // Hint label
        GridBagConstraints hintGc = DialogHelper.gbc(0, 5, 0);
        hintGc.gridwidth = 2;
        JLabel hint = new JLabel("Format: YYYY-MM-DD  e.g. 2025-06-15");
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.TEXT_MUTED);
        form.add(hint, hintGc);

        root.add(form, BorderLayout.CENTER);

        JButton btnSave   = Theme.primaryBtn("Submit Application");
        JButton btnCancel = Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel, btnSave), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            try {
                int    empId  = Integer.parseInt(fEmpId.getText().trim());
                String type   = (String) cbType.getSelectedItem();
                String start  = fStart.getText().trim();
                String end    = fEnd.getText().trim();
                String reason = fReason.getText().trim();
                if (start.isEmpty() || end.isEmpty()) throw new IllegalArgumentException("Dates are required.");
                java.time.LocalDate s = java.time.LocalDate.parse(start);
                java.time.LocalDate en = java.time.LocalDate.parse(end);
                if (en.isBefore(s)) throw new IllegalArgumentException("End date cannot be before start date.");
                LeaveApplication la = new LeaveApplication(empId, type, start, end, reason);
                if (dao.applyLeave(la)) {
                    JOptionPane.showMessageDialog(dlg, "Leave application submitted successfully.");
                    dlg.dispose(); loadAll();
                } else JOptionPane.showMessageDialog(dlg, "Submit failed.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Enter a valid Employee ID."); }
            catch (java.time.format.DateTimeParseException ex) { JOptionPane.showMessageDialog(dlg, "Invalid date format. Use YYYY-MM-DD."); }
            catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(dlg, ex.getMessage()); }
        });

        dlg.setContentPane(root); dlg.setVisible(true);
    }
}
