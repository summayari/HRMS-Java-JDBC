package hrms.util;

import hrms.auth.SessionManager;
import hrms.db.DBConnection;
import java.sql.*;

/** Writes every action to SystemAuditLog table. */
public class AuditLogger {

    public static void log(String module, String action, String details) {
        String user = "system";
        try {
            if (SessionManager.getInstance().getUser() != null)
                user = SessionManager.getInstance().getUser().getUsername();
        } catch (Exception ignored) {}

        String sql = "INSERT INTO SystemAuditLog (Username, Module, Action, Details, LogTime) "
                   + "VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, module);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (Exception ex) { /* silent — never break the app for logging */ }
    }
}
