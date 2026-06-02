package hrms;

import hrms.gui.ConnectionDialog;
import hrms.gui.LoginDialog;
import hrms.gui.MainWindow;
import hrms.gui.Theme;

import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        Theme.applyGlobal();
        SwingUtilities.invokeLater(() -> {
            // Step 1: DB connection
            ConnectionDialog conn = new ConnectionDialog(null);
            conn.setVisible(true);
            if (!conn.isConnected()) return;

            // Step 2: Login
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
            if (!login.isLoggedIn()) return;

            // Step 3: Main window
            MainWindow win = new MainWindow();
            win.setVisible(true);
        });
    }
}
