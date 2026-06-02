package hrms.gui;

import hrms.dao.DepartmentDAO;
import hrms.model.Department;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Department Management Panel
 *
 * When adding or editing a department, the manager field now offers two modes:
 *   1) "Existing Employee" – pick from a dropdown of active employees
 *   2) "Direct Hire"       – type any name (for a brand-new hire not yet in the system)
 *
 * The chosen name is stored in the Departments.Manager VARCHAR column unchanged,
 * so no DB schema change is required.
 */
public class DepartmentPanel extends JPanel {

    private final DepartmentDAO dao = new DepartmentDAO();

    private static final String[] COLS = {"Code", "Department Name", "Manager", "Location"};
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    // ── Constructor ──────────────────────────────────────────────────────────

    public DepartmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header row
        JLabel headerLabel = Theme.headerLabel("Department Management");
        JButton addBtn = Theme.primaryBtn("+ Add Department");
        addBtn.addActionListener(e -> showDialog(null));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(headerLabel, BorderLayout.WEST);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(addBtn);
        north.add(btnWrap, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        // Table
        Theme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action buttons
        JButton editBtn   = Theme.accentBtn("Edit Selected");
        JButton deleteBtn = Theme.dangerBtn("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        editBtn.addActionListener(e   -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadAll());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.setOpaque(false);
        south.add(refreshBtn);
        south.add(editBtn);
        south.add(deleteBtn);
        add(south, BorderLayout.SOUTH);

        loadAll();
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private void loadAll() {
        model.setRowCount(0);
        for (Department d : dao.getAllDepartments()) {
            model.addRow(new Object[]{
                d.getDeptId(),
                d.getDeptName(),
                d.getManager(),
                d.getLocation()
            });
        }
    }

    // ── Edit / Delete ────────────────────────────────────────────────────────

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }

        int id = (int) model.getValueAt(row, 0);
        dao.getAllDepartments().stream()
           .filter(d -> d.getDeptId() == id)
           .findFirst()
           .ifPresent(this::showDialog);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }

        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete department: " + name + "\nAll employees in this department will be affected.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteDepartment(id)) {
                JOptionPane.showMessageDialog(this, "Department deleted.");
                loadAll();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Delete failed (employees still assigned?).", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Add / Edit Dialog ────────────────────────────────────────────────────

    private void showDialog(Department existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Department" : "Add Department",
            Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(460, 340);
        dialog.setLocationRelativeTo(this);

        // ── Form fields ──────────────────────────────────────────────────────
        JTextField nameField     = Theme.field(20);
        JTextField locationField = Theme.field(20);

        if (isEdit) {
            nameField.setText(existing.getDeptName());
            locationField.setText(existing.getLocation());
        }

        // Manager section: two radio buttons to switch mode
        JRadioButton rbExisting = new JRadioButton("Existing Employee");
        JRadioButton rbDirect   = new JRadioButton("Direct Hire (new person)");
        ButtonGroup modeGroup   = new ButtonGroup();
        modeGroup.add(rbExisting);
        modeGroup.add(rbDirect);
        rbExisting.setOpaque(false);
        rbDirect.setOpaque(false);
        rbExisting.setForeground(Theme.CARD_BG == null ? Color.WHITE : Theme.PRIMARY);
        rbDirect.setForeground(rbExisting.getForeground());

        // Populate employee dropdown from DB
        Map<Integer, String> empMap = dao.getEmployeeNames();
        // Build display items: "Name (ID)"
        String[] empItems = empMap.entrySet().stream()
            .map(e -> e.getValue() + "  [ID " + e.getKey() + "]")
            .toArray(String[]::new);

        JComboBox<String> empCombo = new JComboBox<>(empItems);
        empCombo.setFont(Theme.FONT_BODY);

        JTextField directField = Theme.field(20);

        // CardLayout to swap between combo and text field
        JPanel managerCard = new JPanel(new CardLayout());
        managerCard.setOpaque(false);
        managerCard.add(empCombo,   "existing");
        managerCard.add(directField, "direct");

        // Default mode
        boolean currentIsExisting = true;
        if (isEdit && existing.getManager() != null && !existing.getManager().isBlank()) {
            // Try to detect whether the saved manager name matches an existing employee
            String savedMgr = existing.getManager();
            boolean matched = empMap.values().stream()
                .anyMatch(n -> n.equalsIgnoreCase(savedMgr));
            currentIsExisting = matched;
            if (!matched) {
                directField.setText(savedMgr);
            } else {
                // Pre-select the matching employee in the combo
                for (int i = 0; i < empItems.length; i++) {
                    if (empItems[i].startsWith(savedMgr + "  [")) {
                        empCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        if (currentIsExisting) { rbExisting.setSelected(true); }
        else                   { rbDirect.setSelected(true);   }

        CardLayout cl = (CardLayout) managerCard.getLayout();
        cl.show(managerCard, currentIsExisting ? "existing" : "direct");

        rbExisting.addActionListener(e -> cl.show(managerCard, "existing"));
        rbDirect.addActionListener(e   -> cl.show(managerCard, "direct"));

        // ── Layout ───────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Dept Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(label("Dept Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(nameField, gbc);

        // Row 1: Manager mode label
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(label("Manager:"), gbc);

        // Radio buttons in a sub-panel
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbExisting);
        radioPanel.add(rbDirect);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(radioPanel, gbc);

        // Row 2: Manager input card (combo or text)
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        form.add(managerCard, gbc);

        // Row 3: Location
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(label("Location:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(locationField, gbc);

        // Buttons
        JButton saveBtn   = Theme.primaryBtn("Save");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String deptName = nameField.getText().trim();
            if (deptName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required.");
                return;
            }

            // Resolve manager name from whichever mode is active
            String managerName;
            if (rbExisting.isSelected()) {
                int idx = empCombo.getSelectedIndex();
                if (idx < 0 || empItems.length == 0) {
                    managerName = "";
                } else {
                    // Strip the "[ID N]" suffix
                    String item = empItems[idx];
                    managerName = item.replaceAll("\\s+\\[ID \\d+\\]$", "").trim();
                }
            } else {
                managerName = directField.getText().trim();
            }

            Department d = isEdit ? existing : new Department();
            d.setDeptName(deptName);
            d.setManager(managerName);
            d.setLocation(locationField.getText().trim());

            boolean ok = isEdit ? dao.updateDepartment(d) : dao.addDepartment(d);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, isEdit ? "Updated." : "Added.");
                dialog.dispose();
                loadAll();
            } else {
                JOptionPane.showMessageDialog(dialog, "Operation failed.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRow.setBackground(Theme.CARD_BG);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.CARD_BG);
        content.add(form, BorderLayout.CENTER);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        return l;
    }
}
