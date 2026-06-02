package hrms.auth;

import hrms.model.User;

/** Simple singleton session holder. */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setUser(User u)  { this.currentUser = u; }
    public User getUser()        { return currentUser;   }
    public boolean isAdmin()     { return currentUser != null && currentUser.isAdmin(); }
    public void logout()         { currentUser = null;   }
}
