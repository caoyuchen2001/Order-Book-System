package final_project;

/**
 * User class, including fields
 * 
 * 1. username
 * 2. password
 * 3. loggedIn
 */


public class User {
    private String username;
    private String password;
    private boolean loggedIn; 

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.loggedIn = false; 
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void setPassword(String password) {
    	this.password=password;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}

