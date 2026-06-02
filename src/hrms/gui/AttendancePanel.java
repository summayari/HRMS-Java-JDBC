package hrms.gui;

import hrms.dao.AttendanceDAO;
import hrms.dao.EmployeeDAO;
import hrms.model.Attendance;
import hrms.model.Employee;
import hrms.util.CsvExporter;
import hrms.util.DialogHelper;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final AttendanceDAO dao    = new AttendanceDAO();
    private final EmployeeDAO   empDAO = new EmployeeDAO();

    private final DefaultTableModel model;
    private final JTable            table;
    private final JTextField        fEmpId;
    private final JTextField        fDate;
    private final JLabel            summaryLabel;

    private static final String[] COLS = {"Att ID", "Emp ID", "Employee Name", "Date", "Status"};

    public AttendancePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        fEmpId = Theme.field(6);
        fDate  = Theme.field(10);
        fDate.setText(LocalDate.now().toString());
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(Theme.FONT_SMALL);
        summaryLabel.setForeground(Theme.TEXT_MUTED);

        add(buildHeader(), BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        center.add(buildSummaryBar(),       BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);
        loadAll();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(5,46,22),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        JPanel left = new JPanel(new GridLayout(2,1,0,3));
        left.setOpaque(false);
        JLabel title = new JLabel("📅  Attendance Tracking");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        left.add(title); left.add(summaryLabel);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel empLbl = new JLabel("Emp ID:"); empLbl.setForeground(Theme.TEXT_MUTED); empLbl.setFont(Theme.FONT_SMALL);
        JLabel dateLbl= new JLabel("Date:");  dateLbl.setForeground(Theme.TEXT_MUTED); dateLbl.setFont(Theme.FONT_SMALL);

        JButton btnByEmp  = Theme.primaryBtn("By Employee");
        JButton btnByDate = Theme.accentBtn("By Date");
        JButton btnAll    = Theme.ghostBtn("Show All");
        JButton btnExport = Theme.ghostBtn("↓ CSV");

        right.add(empLbl);  right.add(fEmpId);  right.add(btnByEmp);
        right.add(Box.createHorizontalStrut(8));
        right.add(dateLbl); right.add(fDate);   right.add(btnByDate);
        right.add(Box.createHorizontalStrut(8));
        right.add(btnAll);  right.add(btnExport);

        header.add(right, BorderLayout.EAST);

        btnByEmp.addActionListener(e -> {
            String txt = fEmpId.getText().trim();
            if (txt.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter an Employee ID."); return; }
            try { populate(dao.getByEmployee(Integer.parseInt(txt))); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Invalid ID."); }
        });
        btnByDate.addActionListener(e -> populate(dao.getByDate(fDate.getText().trim())));
        btnAll.addActionListener(e -> loadAll());
        btnExport.addActionListener(e -> CsvExporter.export(this, model, "Attendance"));
        return header;
    }

    private JPanel buildSummaryBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));

        // Will be populated after load
        bar.setName("summaryBar");
        return bar;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));

        JButton btnMark  = Theme.primaryBtn("＋ Mark Attendance");
        JButton btnBulk  = Theme.accentBtn("📋 Bulk Mark Today");
        JButton btnRefresh = Theme.ghostBtn("↺ Refresh");

        bar.add(btnMark); bar.add(btnBulk); bar.add(btnRefresh);
        btnMark.addActionListener(e -> showMarkDialog(false));
        btnBulk.addActionListener(e -> showBulkMarkDialog());
        btnRefresh.addActionListener(e -> loadAll());
        return bar;
    }

    // ── Status renderer ───────────────────────────────────────────────────────

    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if (!sel) {
                    switch (value == null ? "" : value.toString()) {
                        case "Present"  -> { lbl.setForeground(Theme.SUCCESS); lbl.setBackground(new Color(22,101,52,70)); }
                        case "Absent"   -> { lbl.setForeground(Theme.DANGER);  lbl.setBackground(new Color(153,27,27,70)); }
                        case "Leave"    -> { lbl.setForeground(Theme.WARNING); lbl.setBackground(new Color(120,53,15,70)); }
                        case "Half Day" -> { lbl.setForeground(Theme.INFO);    lbl.setBackground(new Color(7,89,133,70));  }
                        default         -> { lbl.setForeground(Theme.TEXT_MUTED); lbl.setBackground(Theme.CARD_BG); }
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

    private void loadAll() { populate(dao.getAll()); }

    private void populate(List<Attendance> list) {
        model.setRowCount(0);
        int present=0, absent=0, leave=0, half=0;
        for (Attendance a : list) {
            model.addRow(new Object[]{a.getAttId(), a.getEmpId(), a.getEmpName(), a.getAttDate(), a.getStatus()});
            switch (a.getStatus()) {
                case "Present"  -> present++;
                case "Absent"   -> absent++;
                case "Leave"    -> leave++;
                case "Half Day" -> half++;
            }
        }
        summaryLabel.setText(String.format(
            "Total: %d  |  ✅ Present: %d  |  ❌ Absent: %d  |  🏖 Leave: %d  |  ½ Half Day: %d",
            list.size(), present, absent, leave, half));
    }

    // ── Mark dialog ───────────────────────────────────────────────────────────

    private void showMarkDialog(boolean prefillToday) {
        JDialog dlg = DialogHelper.create(this, "Mark Attendance", "", 400, 300);
        JPanel root = DialogHelper.rootPanel();
        root.add(DialogHelper.header("📅", "Mark Attendance", "Record employee attendance", Theme.ACCENT), BorderLayout.NORTH);

        JPanel form = DialogHelper.formPanel();
        JTextField fEmpF = Theme.field(10);
        JTextField fDateF = Theme.field(10);
        fDateF.setText(prefillToday ? LocalDate.now().toString() : fDate.getText());
        JComboBox<String> cbStatus = Theme.combo("Present", "Absent", "Leave", "Half Day");

        String[] lbls = {"Employee ID *", "Date (YYYY-MM-DD) *", "Status *"};
        Component[] ctrls = {fEmpF, fDateF, cbStatus};
        for (int i = 0; i < lbls.length; i++) {
            form.add(DialogHelper.label(lbls[i]), DialogHelper.gbc(0, i, 0));
            form.add(ctrls[i],                    DialogHelper.gbc(1, i, 1));
        }
        root.add(form, BorderLayout.CENTER);

        JButton btnSave = Theme.accentBtn("Mark");
        JButton btnCancel = Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel, btnSave), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            try {
                int    empId  = Integer.parseInt(fEmpF.getText().trim());
                String date   = fDateF.getText().trim();
                String status = (String) cbStatus.getSelectedItem();
                if (date.isEmpty()) throw new IllegalArgumentException("Date required");
                if (dao.markAttendance(new Attendance(empId, date, status))) {
                    JOptionPane.showMessageDialog(dlg, "Attendance marked successfully.");
                    dlg.dispose(); loadAll();
                } else JOptionPane.showMessageDialog(dlg, "Failed (duplicate entry?)", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Enter a valid Employee ID."); }
            catch (IllegalArgumentException ex){ JOptionPane.showMessageDialog(dlg, ex.getMessage()); }
        });

        dlg.setContentPane(root); dlg.setVisible(true);
    }

    // ── Bulk Mark dialog ──────────────────────────────────────────────────────

    private void showBulkMarkDialog() {
        List<Employee> employees = empDAO.getAllEmployees();
        if (employees.isEmpty()) { JOptionPane.showMessageDialog(this, "No employees found."); return; }

        JDialog dlg = DialogHelper.create(this, "Bulk Mark Attendance", "", 560, 520);
        JPanel root = DialogHelper.rootPanel();
        root.add(DialogHelper.header("📋", "Bulk Mark Attendance",
            "Mark attendance for all employees – " + LocalDate.now(), Theme.ACCENT), BorderLayout.NORTH);

        // Date picker at top of form
        JPanel datePick = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        datePick.setBackground(Theme.BG_SECONDARY);
        JLabel dateLbl = new JLabel("Date:");
        dateLbl.setForeground(Theme.TEXT_MUTED); dateLbl.setFont(Theme.FONT_SMALL);
        JTextField bulkDate = Theme.field(12);
        bulkDate.setText(LocalDate.now().toString());
        JButton btnSetAll = Theme.primaryBtn("Set All →");
        JComboBox<String> cbSetAll = Theme.combo("Present","Absent","Leave","Half Day");
        datePick.add(dateLbl); datePick.add(bulkDate);
        datePick.add(Box.createHorizontalStrut(16));
        datePick.add(new JLabel("Set all to:") {{ setForeground(Theme.TEXT_MUTED); setFont(Theme.FONT_SMALL); }});
        datePick.add(cbSetAll); datePick.add(btnSetAll);

        // Table of employees with dropdown per row
        String[] bulkCols = {"ID", "Employee Name", "Department", "Status"};
        DefaultTableModel bulkModel = new DefaultTableModel(bulkCols, 0);
        for (Employee e : employees)
            bulkModel.addRow(new Object[]{e.getEmpId(), e.getEmpName(), e.getDeptName(), "Present"});

        JTable bulkTable = new JTable(bulkModel);
        Theme.styleTable(bulkTable);
        bulkTable.getColumnModel().getColumn(0).setPreferredWidth(44);
        bulkTable.getColumnModel().getColumn(3).setCellEditor(
            new DefaultCellEditor(new JComboBox<>(new String[]{"Present","Absent","Leave","Half Day"})));
        bulkTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer());

        btnSetAll.addActionListener(e -> {
            String val = (String) cbSetAll.getSelectedItem();
            for (int i = 0; i < bulkModel.getRowCount(); i++) bulkModel.setValueAt(val, i, 3);
        });

        JPanel center = new JPanel(new BorderLayout(0,0));
        center.setBackground(Theme.BG_SECONDARY);
        center.add(datePick, BorderLayout.NORTH);
        center.add(Theme.scrollPane(bulkTable), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JButton btnSave   = Theme.accentBtn("Save All Records");
        JButton btnCancel = Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel, btnSave), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            // Stop any active edit
            if (bulkTable.isEditing()) bulkTable.getCellEditor().stopCellEditing();
            String date = bulkDate.getText().trim();
            if (date.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Please enter a date."); return; }
            int success=0, fail=0;
            for (int i = 0; i < bulkModel.getRowCount(); i++) {
                int    empId  = (int) bulkModel.getValueAt(i, 0);
                String status = (String) bulkModel.getValueAt(i, 3);
                if (dao.markAttendance(new Attendance(empId, date, status))) success++; else fail++;
            }
            JOptionPane.showMessageDialog(dlg,
                String.format("Done!  ✅ %d marked   ⚠ %d skipped (already exists for date)", success, fail));
            dlg.dispose(); loadAll();
        });

        dlg.setContentPane(root); dlg.setVisible(true);
    }
}
