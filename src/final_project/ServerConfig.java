package final_project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Config class loads configuration values from 'config.properties' file.
 */

public class ServerConfig {
	
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream("Config_Server.properties")) {
            properties.load(input);
            System.out.println("[Config] Configuration loaded successfully.");
        } catch (IOException e) {
            System.err.println("[Config] Failed to load configuration: " + e.getMessage());
        }
    }
    
    public static int getIntProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("[Config] Invalid integer format for key: " + key + ", using default: " + defaultValue);
            return Integer.parseInt(defaultValue); // fallback to default
        }
    }
    
    //Server configuration
    public static int getSERVER_PORT() {
        return getIntProperty("SERVER_PORT", "12345");
    }
    
    public static int getSERVER_PORT_UDP() {
        return getIntProperty("SERVER_PORT_UDP", "54321");
    }
    
    public static int getINACTIVITY_THRESHOLD() {
    	return getIntProperty("INACTIVITY_THRESHOLD", "1800000");
    }
    
    
    // Order Id generator 
    public static String getOrderIdCounterFile() {
        return properties.getProperty("order_id_counter", "order_id_counter.txt");
    }
    
    // Order history 
    public static String getOrderHistoryFile() {
        return properties.getProperty("order_history", "order_history.json");
    }
    
    // Order book 
    public static String getOrderBookFile() {
        return properties.getProperty("order_book", "orderbook_data.json");
    }
    
    // Registerd users 
    public static String getUsersFile() {
        return properties.getProperty("user_file", "registered_users.json");
    }
    
    // Trade history 
    public static String getTradeHistoryFile() {
        return properties.getProperty("trade_history", "trade_history.json");
    }
    
    // Buffer size for UDP
    public static int getBufSize() {
        return getIntProperty("BUFFER_SIZE", "2048");
    }
   
    
    
}










