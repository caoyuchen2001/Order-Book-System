package final_project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * RegisteredUsers manages the list of registered users and handles user data persistence.
 * 
 * 
 * 1. Uses ConcurrentHashMap to safely access user data in multi-threaded environment.
 * 2. Supports loading user data from "registered_users.json" on server startup.
 * 3. Persists user data to the JSON file whenever the user list changes.
 * 
 * Thread Safety:
 * 1. The user data map (registeredUsers) is thread-safe due to ConcurrentHashMap.
 * 2. The persistUsers() method is synchronized to avoid concurrent file write issues.
 * 3. add() uses putIfAbsent() for atomic registration, ensuring no duplicate usernames.
 * 
 */

public class RegisteredUsers {
	
    private static final String USERS_FILE = ServerConfig.getUsersFile();
    private static final ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    
    public static void load() {
    	
        File file = new File(USERS_FILE);
        
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                List<User> users = gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());
                if (users != null) {
                    for (User user : users) {
                        user.setLoggedIn(false);
                        registeredUsers.put(user.getUsername(), user);
                    }
                    System.out.println("[Main] Loaded " + registeredUsers.size() + " users from " + USERS_FILE);
                } else {
                    System.out.println("[Main] No users loaded from " + USERS_FILE);
                }
            } catch (IOException e) {
                System.err.println("[Main] Error loading users: " + e.getMessage());
            }
        } else {
            System.out.println("[Main] User file not found. Starting with an empty user set.");
        }
        
    }

    
    public static synchronized void persistUsers() {
    	
        try (Writer writer = new FileWriter(USERS_FILE)) {
        	
            gson.toJson(registeredUsers.values(), writer);
        } catch (IOException e) {
            System.err.println("[Main] Failed to persist registered user information: " + e.getMessage());
        }
        
    }

    
    public static boolean contains(String username) {
    	
        return registeredUsers.containsKey(username);
    }

    
    public static User get(String username) {
    	
        return registeredUsers.get(username);
    }

    
    public static boolean add(String username, String password) {
    	
        User res = registeredUsers.putIfAbsent(username, new User(username, password));
        if (res == null) { 
            persistUsers();
            return true;
        }
        return false;
    }
}
