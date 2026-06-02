package hrms.gui;

import hrms.auth.SessionManager;
import hrms.db.DBConnection;
import hrms.util.AuditLogger;
import hrms.util.DialogHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ChangePasswordDialog extends JDialog {

    public ChangePasswordDialog(Component parent) {
        
        setSize(420, 360);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = DialogHelper.rootPanel();
        root.add(DialogHelper.header("🔒", "Change Password",
            "Update your account password", Theme.WARNING), BorderLayout.NORTH);

        JPanel form = DialogHelper.formPanel();
        JPasswordField fCurrent = Theme.passwordField(18);
        JPasswordField fNew     = Theme.passwordField(18);
        JPasswordField fConfirm = Theme.passwordField(18);

        String[] lbls = {"Current Password", "New Password", "Confirm New Password"};
        JPasswordField[] flds = {fCurrent, fNew, fConfirm};
        for (int i = 0; i < lbls.length; i++) {
            form.add(DialogHelper.label(lbls[i]), DialogHelper.gbc(0, i, 0));
            form.add(flds[i],                     DialogHelper.gbc(1, i, 1));
        }

        // Password strength indicator
        JLabel strengthLbl = new JLabel("  ");
        strengthLbl.setFont(Theme.FONT_SMALL);
        GridBagConstraints gc = DialogHelper.gbc(0, 3, 0);
        gc.gridwidth = 2;
        form.add(strengthLbl, gc);

        fNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String pw = new String(fNew.getPassword());
                if (pw.length() == 0)      { strengthLbl.setText(""); return; }
                if (pw.length() < 4)       { strengthLbl.setText("Strength: Weak"); strengthLbl.setForeground(Theme.DANGER); }
                else if (pw.length() < 8)  { strengthLbl.setText("Strength: Fair"); strengthLbl.setForeground(Theme.WARNING); }
                else                       { strengthLbl.setText("Strength: Strong ✓"); strengthLbl.setForeground(Theme.SUCCESS); }
            }
        });

        root.add(form, BorderLayout.CENTER);

        JButton btnSave   = Theme.warningBtn("Update Password");
        JButton btnCancel = Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel, btnSave), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            String current = new String(fCurrent.getPassword());
            String newPw   = new String(fNew.getPassword());
            String confirm = new String(fConfirm.getPassword());

            if (current.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required."); return;
            }
            if (!newPw.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match."); return;
            }
            if (newPw.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters."); return;
            }
            if (doPasswordChange(current, newPw)) {
                AuditLogger.log("Security", "Password Changed", "User changed their password");
                JOptionPane.showMessageDialog(this, "Password updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setContentPane(root);
    }

    private boolean doPasswordChange(String currentPw, String newPw) {
        String username = SessionManager.getInstance().getUser().getUsername();
        String checkSql = "SELECT COUNT(*) FROM SystemUsers WHERE Username=? AND Password_Hash=?";
        String updateSql= "UPDATE SystemUsers SET Password_Hash=? WHERE Username=?";
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement(checkSql);
            ps.setString(1, username); ps.setString(2, currentPw);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || rs.getInt(1) == 0) return false;
            ps = c.prepareStatement(updateSql);
            ps.setString(1, newPw); ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
}
