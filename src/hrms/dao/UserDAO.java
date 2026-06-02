package hrms.dao;

import hrms.db.DBConnection;
import hrms.model.User;

import java.sql.*;

/** Data-access for the SystemUsers table. */
public class UserDAO {

    /** Returns the authenticated User, or null on bad credentials. */
    public User authenticate(String username, String password) {
        String sql = "SELECT User_ID, Username, Role, Emp_ID " +
                     "FROM SystemUsers " +
                     "WHERE Username = ? AND Password_Hash = ? AND Is_Active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password.trim());   // plain-text for lab; hash in production
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("User_ID"),
                    rs.getString("Username"),
                    rs.getString("Role"),
                    rs.getInt("Emp_ID")
                );
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    /** Records the login timestamp. */
    public void recordLogin(int userId) {
        String sql = "UPDATE SystemUsers SET Last_Login = GETDATE() WHERE User_ID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
