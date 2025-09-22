package final_project;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * OrderIdGenerator generates **unique** order IDs in a thread-safe way.
 * 
 * 1. Uses AtomicInteger to ensure atomic increment operations.
 * 2. The getNextOrderId() method is synchronized to guarantee atomicity between ID generation and persist.
 * 3. Each generated ID is immediately saved to "order_id_counter.txt" for recovery on server restart.
 * 4. Loads the last saved ID value at startup to continue from the previous state.
 * 
 * 
 */

public class OrderIdGenerator {
	
    private static final String FILE_PATH = ServerConfig.getOrderIdCounterFile();
    private static final AtomicInteger counter = new AtomicInteger(1);


    public static synchronized int getNextOrderId() {
    	
        int id = counter.getAndIncrement();
        persist();
        return id;
    }
    
    public static void load() {
    	
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                int savedValue = Integer.parseInt(line.trim());
                counter.set(savedValue);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("⚠️ Failed to load order ID counter: " + e.getMessage());
        }
    }

    private static void persist() {
    	
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(counter.get()));
        } catch (IOException e) {
            System.err.println("⚠️ Failed to save order ID counter: " + e.getMessage());
        }
    }
}
