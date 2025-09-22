package final_project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Loads client configuration parameters.
 * 
 */
public class ClientConfig {
	
    private static final Properties properties = new Properties();
    
    

    static {
    	// open file and load config data
        try (FileInputStream input = new FileInputStream("Config_Client.properties")) {
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

    
    // Client data
    public static String getSERVER_HOST() {
    	
    	return properties.getProperty("SERVER_HOST", "127.0.0.1");
    }
    
    // Server data
    public static int getSERVER_PORT() {
    	return getIntProperty("SERVER_PORT", "12345");
    }
    public static int getSERVER_PORT_UDP() {
    	return getIntProperty("SERVER_PORT_UDP", "54321");
    }
    
    // Buffer size for UDP
    public static int getBufSize() {
    	return getIntProperty("BUFFER_SIZE", "2048");
    }
    
    
	
}
