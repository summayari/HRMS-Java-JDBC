package hrms.gui;

import hrms.dao.EmployeeDAO;
import hrms.dao.SalaryDAO;
import hrms.model.Employee;
import hrms.model.Salary;
import hrms.util.AuditLogger;
import hrms.util.DialogHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Monthly bulk payroll run — loads last salary for each employee, allows adjustment, then saves all. */
public class PayrollRunDialog extends JDialog {

    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final SalaryDAO   salDAO = new SalaryDAO();
    private DefaultTableModel runModel;

    public PayrollRunDialog(Component parent) {
        
        setTitle("Monthly Payroll Run");
        setModal(true);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_SECONDARY);

        root.add(DialogHelper.header("💰", "Monthly Payroll Run",
            "Review and confirm salary records for the selected month", Theme.ACCENT), BorderLayout.NORTH);

        // Month selector bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        topBar.setBackground(Theme.BG_SECONDARY);
        topBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(51,65,85)));

        JLabel monLbl = new JLabel("Pay Month:");
        monLbl.setForeground(Theme.TEXT_MUTED); monLbl.setFont(Theme.FONT_SMALL);

        LocalDate now = LocalDate.now();
        JTextField fMonth = Theme.field(14);
        fMonth.setText(now.getMonth().toString().substring(0,1)
            + now.getMonth().toString().substring(1).toLowerCase() + "-" + now.getYear());

        JButton btnLoad = Theme.primaryBtn("Load Employees");
        JLabel  hint    = new JLabel("  Loads last known salary for each active employee. You can edit before saving.");
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.TEXT_MUTED);

        topBar.add(monLbl); topBar.add(fMonth); topBar.add(btnLoad); topBar.add(hint);
        root.add(topBar, BorderLayout.NORTH);  // overrides header — attach differently

        // Re-do layout
        root.removeAll();
        root.setLayout(new BorderLayout());
        JPanel headerWithBar = new JPanel(new BorderLayout());
        headerWithBar.setBackground(Theme.BG_SECONDARY);
        headerWithBar.add(DialogHelper.header("💰", "Monthly Payroll Run",
            "Review, adjust and confirm salary for the selected month", Theme.ACCENT), BorderLayout.NORTH);
        headerWithBar.add(topBar, BorderLayout.SOUTH);
        root.add(headerWithBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Emp ID", "Name", "Department", "Basic Salary", "Bonus", "Deductions", "Net Salary"};
        runModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c >= 3 && c <= 5; // only salary columns editable
            }
            @Override public Class<?> getColumnClass(int c) {
                return c >= 3 ? Double.class : Object.class;
            }
        };

        JTable table = new JTable(runModel);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(6).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean sel, boolean focus, int row, int col) {
                    if (value instanceof Double)
                        setText(String.format("PKR %,.0f", (Double) value));
                    else setText(value == null ? "" : value.toString());
                    setForeground(sel ? Theme.TEXT_MAIN : Theme.SUCCESS);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setBackground(sel ? new Color(99,102,241,80) : Theme.CARD_BG);
                    setOpaque(true);
                    return this;
                }
            });

        // Auto-recalc net when basic/bonus/deductions change
        runModel.addTableModelListener(e -> {
            if (e.getColumn() >= 3 && e.getColumn() <= 5) {
                int r = e.getFirstRow();
                try {
                    double basic  = toDouble(runModel.getValueAt(r, 3));
                    double bonus  = toDouble(runModel.getValueAt(r, 4));
                    double deduct = toDouble(runModel.getValueAt(r, 5));
                    runModel.setValueAt(basic + bonus - deduct, r, 6);
                } catch (Exception ignored) {}
            }
        });

        JScrollPane sp = Theme.scrollPane(table);
        root.add(sp, BorderLayout.CENTER);

        // Summary + buttons
        JLabel totalLbl = new JLabel("  Load employees to see total payroll");
        totalLbl.setFont(Theme.FONT_SMALL); totalLbl.setForeground(Theme.TEXT_MUTED);

        JButton btnSave   = Theme.accentBtn("✔ Process Payroll");
        JButton btnCancel = Theme.ghostBtn("Cancel");

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BG_SECONDARY);
        south.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        JPanel southLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,14,12));
        southLeft.setBackground(Theme.BG_SECONDARY);
        southLeft.add(totalLbl);
        JPanel southRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12));
        southRight.setBackground(Theme.BG_SECONDARY);
        southRight.add(btnCancel); southRight.add(btnSave);
        south.add(southLeft, BorderLayout.WEST);
        south.add(southRight, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);

        // Load action
        btnLoad.addActionListener(ev -> {
            runModel.setRowCount(0);
            List<Employee> emps = empDAO.getAllEmployees();
            double grand = 0;
            for (Employee emp : emps) {
                if (!"Active".equals(emp.getStatus())) continue;
                List<Salary> hist = salDAO.getByEmployee(emp.getEmpId());
                double basic  = hist.isEmpty() ? 0 : hist.get(0).getBasicSalary();
                double bonus  = hist.isEmpty() ? 0 : hist.get(0).getBonus();
                double deduct = hist.isEmpty() ? 0 : hist.get(0).getDeductions();
                double net    = basic + bonus - deduct;
                grand += net;
                runModel.addRow(new Object[]{emp.getEmpId(), emp.getEmpName(), emp.getDeptName(),
                    basic, bonus, deduct, net});
            }
            final double total = grand;
            totalLbl.setText(String.format("  %d employees  |  Total Payroll: PKR %,.0f",
                runModel.getRowCount(), total));
        });

        // Save action
        btnSave.addActionListener(ev -> {
            if (runModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Load employees first."); return;
            }
            String month = fMonth.getText().trim();
            if (month.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a pay month."); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Process payroll for " + runModel.getRowCount() + " employees for " + month + "?",
                "Confirm Payroll Run", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            int success = 0;
            for (int i = 0; i < runModel.getRowCount(); i++) {
                int    empId  = (int) runModel.getValueAt(i, 0);
                double basic  = toDouble(runModel.getValueAt(i, 3));
                double bonus  = toDouble(runModel.getValueAt(i, 4));
                double deduct = toDouble(runModel.getValueAt(i, 5));
                if (salDAO.addSalary(new Salary(empId, basic, bonus, deduct, month))) success++;
            }
            AuditLogger.log("Payroll", "Payroll Run", "Processed " + success + " salaries for " + month);
            JOptionPane.showMessageDialog(this,
                "✅ Payroll processed!\n" + success + " / " + runModel.getRowCount() + " records saved.");
            dispose();
        });

        btnCancel.addActionListener(ev -> dispose());
        setContentPane(root);
    }

    private double toDouble(Object v) {
        if (v instanceof Double) return (Double) v;
        try { return Double.parseDouble(v.toString().replace(",","")); }
        catch (Exception e) { return 0; }
    }
}
