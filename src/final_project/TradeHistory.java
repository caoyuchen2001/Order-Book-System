package final_project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * TradeHistory uses a LinkedBlockingQueue to hold trades in memory and 
 * saves them to a JSON file after each update.
 * 
 * Thread safety is ensured via synchronization on the 'trades' queue.
 */

public class TradeHistory {
	
    private static final LinkedBlockingQueue<TradeInfo> trades = new LinkedBlockingQueue<>();
    private static final String FILE_NAME = ServerConfig.getTradeHistoryFile();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    /**
     * Adds new trades from the tradeMap to the trade history and immediately persists to file.
     * 
     * Thread-safe: synchronized on the trades queue to ensure atomic "add and persist" operation.
     *
     * @param tradeMap Map of username to list of TradeInfo objects (trades to be recorded).
     */
    public static void addTrades(Map<String, List<TradeInfo>> tradeMap) {
    	
        	if (tradeMap.isEmpty()) return;
        	synchronized (trades) {
        	for (Map.Entry<String, List<TradeInfo>> entry : tradeMap.entrySet()) {
                List<TradeInfo> tradeList = entry.getValue();
                trades.addAll(tradeList);
        	}
        	 persist();
        	}
        
    }


    /**
     * Persists the current trade history to a JSON file.
     * 
     * Only called from synchronized contexts, so no extra synchronization needed inside this method.
     */
    private static void persist() {
    	
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("trades", new ArrayList<>(trades));
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(wrapper, writer);
        } catch (IOException e) {
            System.err.println("[Main] Failed to save trade history:" + e.getMessage());
        }
        
    }



    /**
     * Loads existing trade history from the JSON file on startup.
     * 
     */
    public static void load() {
    	
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(FILE_NAME)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray tradesArray = root.getAsJsonArray("trades");

            List<TradeInfo> loadedTrades = gson.fromJson(tradesArray, new TypeToken<List<TradeInfo>>(){}.getType());

            synchronized (trades) {
                trades.addAll(loadedTrades);
            }

        } catch (IOException e) {
            System.err.println("[Main] Error loading trade history:" + e.getMessage());
        }
    }

}


