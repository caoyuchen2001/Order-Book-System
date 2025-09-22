package final_project;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OrderHistory manages all received orders (LimitOrder, MarketOrder, StopOrder),
 * even those market orders been rejected.
 * 
 * 1. Uses a synchronized LinkedHashMap (orderMap) to maintain insert order and ensure thread-safe basic operations.
 * 2. addOrder() is synchronized at the method level to guarantee atomicity between "put" and "persist".
 * 3. Each new order is immediately saved to "order_history.json" after being received.
 * 4. Supports loading the order history from the JSON file at startup.
 * 
 * Thread Safety:
 * 1. Writing (addOrder + persist) is fully synchronized.
 * 2. Reading (getOrder) is thread-safe due to Collections.synchronizedMap.
 * 
 * Note: MarketOrders are recorded here but not added to the activeOrders map in Orderbook.
 */

public class OrderHistory {
	
	
    private static final String FILE_PATH = ServerConfig.getOrderHistoryFile();
    private static final Map<Integer, Order> orderMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    
    
    // Add an order and persist immediately
    public static synchronized void addOrder(Order order) {
    	
            orderMap.put(order.getOrderId(), order);
            persist();
    }
    
    
    
    
    public static Order getOrder(int orderId) {
    	
            return orderMap.get(orderId);
    }
    
    
    

    private static void persist() {
    	
        try (Writer writer = new FileWriter(FILE_PATH)) {
        	
                gson.toJson(orderMap, writer);
                
        } catch (IOException e) {
            System.err.println("[Main] Failed to save order history: " + e.getMessage());
        }
        
    }
    
    public static void load() {
    	
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(FILE_PATH)) {
        	
            Type type = new TypeToken<LinkedHashMap<Integer, JsonObject>>() {}.getType();
            Map<Integer, JsonObject> rawMap = gson.fromJson(reader, type);

            for (Map.Entry<Integer, JsonObject> entry : rawMap.entrySet()) {
            	
                JsonObject obj = entry.getValue();
                String classType = obj.get("orderType").getAsString();

                Order order = null;
                switch (classType) {
                    case "limit":
                        order = gson.fromJson(obj, LimitOrder.class);
                        break;
                    case "market":
                        order = gson.fromJson(obj, MarketOrder.class);
                        break;
                    case "stop":
                        order = gson.fromJson(obj, StopOrder.class);
                        break;
                    default:
                        System.err.println("[Main]️ Unrecognized order type: " + classType);
                        
                }

                if (order != null) {
                    orderMap.put(entry.getKey(), order);
                }
            }
            
        } catch (IOException e) {
            System.err.println("[Main] Failed to load order history: " + e.getMessage());
        }
        
    }

}

