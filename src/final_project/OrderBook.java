package final_project;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * OrderBook
 * 
 * 1. A thread-safe singleton of a limit order book that supports:
 * 
 *        1. Limit orders (bid/ask)
 *        2. Market orders (bid/ask)
 *   	  3. Stop orders (bid/ask)
 *        4. Trade matching and execution
 *        5. Persistent storage (save/load to JSON)
 *   
 *
 * 2. Concurrency model:
 *    All state-changing operations (add/remove orders, trigger stop orders, persist, load)
 *    are protected by a global "ReentrantLock(true)" to ensure:
 *    
 *        1. Full mutual exclusion
 *        2. Fair FIFO ordering between threads (first come, first served). 
 *           Trading systems must ensure that earlier requests are processed first.
 * 
 * 4. Data structures covered by the lock:
 * 
 *        1. "bidOrders": PriorityBlockingQueue for limit bid orders
 *        2. "askOrders": PriorityBlockingQueue for limit ask orders
 *        3. "bidStopOrders": PriorityBlockingQueue for stop bid orders
 *        4. "askStopOrders": PriorityBlockingQueue for stop ask orders
 *        5. "activeOrders": ConcurrentHashMap tracking all limit and stop orders
 * 
 * 
 * 5. Design considerations:
 * 
 *        1. Stop orders are checked and triggered after each matching operation.
 *        2. Stop and Market orders must fully match or are rejected.
 *        3. Persistent storage (via "persist()") is called immediately after any state changes.
 *        4. The entire order book state can be restored on server startup via "load()".
 * 
 * 
 */


public class OrderBook {
	
	
	
	// Get OrderBook file path
	 private static final String FILE_PATH = ServerConfig.getOrderBookFile();
	 
	 
	// Global lock to protect the entire order book and ensure strict operation ordering.
	private final ReentrantLock orderBookLock = new ReentrantLock(true);  // fair lock
	
	
	// Singleton instance of OrderBook and Gson for JSON serialization.
    // activeOrders stores all received limit and stop orders, 
    // using ConcurrentHashMap for thread-safe access.
	private static final OrderBook INSTANCE = new OrderBook();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final ConcurrentHashMap<Integer, Order> activeOrders = new ConcurrentHashMap<>();
	
	
	
	
	
	
	
	
	// Bid order queue: sorted by descending price; if prices are equal, sorted by ascending timestamp (earlier orders come first)
	private PriorityBlockingQueue<LimitOrder> bidOrders = new PriorityBlockingQueue<>(1000, new Comparator<LimitOrder>() {
	    @Override
	    public int compare(LimitOrder o1, LimitOrder o2) {
	        // Compare price first: higher price comes first
	        int priceCompare = Integer.compare(o2.getLimitPrice(), o1.getLimitPrice());
	        if (priceCompare != 0) {
	            return priceCompare;
	        }
	        // If prices are equal, compare timestamp: earlier order (smaller timestamp) comes first
	        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
	    }
	});

	
	
	
	
	
	
	// Ask order queue: sorted by ascending price; if prices are equal, sorted by ascending timestamp
	private PriorityBlockingQueue<LimitOrder> askOrders = new PriorityBlockingQueue<>(1000, new Comparator<LimitOrder>() {
	    @Override
	    public int compare(LimitOrder o1, LimitOrder o2) {
	        // Compare price first: lower price comes first
	        int priceCompare = Integer.compare(o1.getLimitPrice(), o2.getLimitPrice());
	        if (priceCompare != 0) {
	            return priceCompare;
	        }
	        // If prices are equal, earlier order comes first
	        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
	    }
	});
	
	
	

	// Bid stop order queue: sorted by descending price; if prices are equal, sorted by ascending timestamp
	private PriorityBlockingQueue<StopOrder> bidStopOrders = new PriorityBlockingQueue<>(1000, new Comparator<StopOrder>() {
	    @Override
	    public int compare(StopOrder o1, StopOrder o2) {
	        // Higher stopPrice has higher priority to be triggered
	        int cmp = Integer.compare(o2.getStopPrice(), o1.getStopPrice());
	        if (cmp != 0) return cmp;
	        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
	    }
	});
	
	
	
	
	
	
	
	// Ask stop order queue: sorted by ascending price; if prices are equal, sorted by ascending timestamp
	private PriorityBlockingQueue<StopOrder> askStopOrders = new PriorityBlockingQueue<>(1000, new Comparator<StopOrder>() {
	    @Override
	    public int compare(StopOrder o1, StopOrder o2) {
	        // Lower stopPrice has higher priority to be triggered (sorted by ascending price)
	        int cmp = Integer.compare(o1.getStopPrice(), o2.getStopPrice());
	        if (cmp != 0) return cmp;
	        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
	    }
	});
	
	
	
	
	

	/**
	 * Singleton pattern implementation for OrderBook.
	 * 
	 * Use getInstance() to access the single shared instance.
	 * Constructor is private to prevent external instantiation.
	 */
	private OrderBook() {}
    public static OrderBook getInstance() {
    	return INSTANCE;
    }
	
    
    
    
    
    
    /**
     * Persists the current state of the order book to the JSON file ("orderbook_data.json").
     * 
     * 1. Saves limit orders, stop orders, and activeOrders.
     * 2. Ensures data consistency by locking the entire order book during the process.
     * 
     * Thread-safe: protected by the global fair lock (orderBookLock).
     */
	public void persist() {
		orderBookLock.lock();
	    try {
	    Map<String, Object> map = new HashMap<>();
	    map.put("bidOrders", new ArrayList<>(bidOrders));
	    map.put("askOrders", new ArrayList<>(askOrders));
	    map.put("bidStopOrders", new ArrayList<>(bidStopOrders));
	    map.put("askStopOrders", new ArrayList<>(askStopOrders));
	    map.put("activeOrders", new HashMap<>(activeOrders));

	    try (FileWriter writer = new FileWriter(FILE_PATH)) {
	        gson.toJson(map, writer); 
	    } catch (IOException e) {
	        System.err.println("[Main] Failed to persist Order Book:" + e.getMessage());
	    }
	    }finally {
	        orderBookLock.unlock();
	    }
	}
	
	
	
	

	/**
	 * Loads the order book from the JSON file ("orderbook_data.json").
	 * 
	 * 1. Rebuilds limit orders, stop orders, and activeOrders map from the saved file.
	 * 2. Protected by the global fair lock (orderBookLock) 
	 * 	  to prevent concurrent modifications during loading.
	 * 
	 * Safe to call on server startup for state recovery.
	 * 
	 */
	public void load() {
		orderBookLock.lock();
		try {
	    File file = new File(FILE_PATH);
	    if (!file.exists()) return;

	    try (FileReader reader = new FileReader(file)) {
	    	
	        Type mapType = new TypeToken<Map<String, JsonElement>>() {}.getType();
	        
	        Map<String, JsonElement> map = gson.fromJson(reader, mapType);

	        // limit orders
	        List<LimitOrder> bidList = gson.fromJson(map.get("bidOrders"), new TypeToken<List<LimitOrder>>(){}.getType());
	        List<LimitOrder> askList = gson.fromJson(map.get("askOrders"), new TypeToken<List<LimitOrder>>(){}.getType());
	        
	        bidOrders.addAll(bidList);
	        askOrders.addAll(askList);

	        // stop orders
	        List<StopOrder> bidStopList = gson.fromJson(map.get("bidStopOrders"), new TypeToken<List<StopOrder>>(){}.getType());
	        List<StopOrder> askStopList = gson.fromJson(map.get("askStopOrders"), new TypeToken<List<StopOrder>>(){}.getType());
	        
	        bidStopOrders.addAll(bidStopList);
	        askStopOrders.addAll(askStopList);

	        // active orders
	        JsonObject activeOrdersObj = map.get("activeOrders").getAsJsonObject();
	        
	        for (Map.Entry<String, JsonElement> entry : activeOrdersObj.entrySet()) {
	        	
	            int orderId = Integer.parseInt(entry.getKey());
	            JsonObject obj = entry.getValue().getAsJsonObject();
	            String orderType = obj.get("orderType").getAsString();

	            Order order;
	            
	            if ("limit".equalsIgnoreCase(orderType)) {
	                order = gson.fromJson(obj, LimitOrder.class);
	                
	            } else if ("stop".equalsIgnoreCase(orderType)) {
	                order = gson.fromJson(obj, StopOrder.class);
	                
	            } else {
	                System.err.println("[Main]️ Order book Unknown orderType: " + orderType + " skipping order ID " + orderId);
	          
	                continue;
	            }
	            
	            activeOrders.put(orderId, order);
	        }
	        System.out.println("[Main] Order Book data loaded successfully!");
	        
	        
	    } catch (IOException e) {
	        System.err.println("[Main] Failed to load Order Book:" + e.getMessage());
	    }
	    
		}finally {
	        orderBookLock.unlock();
	    }
	}

	
	
	
	
	
	/**
	 * Adds the given order to the activeOrders map.
	 * 
	 * 1. Supports both limit and stop orders.
	 * 2. Market orders are not stored in activeOrders.
	 * 
	 * @param order The order to be tracked.
	 */
	private void addActiveOrder(Order order) {
		
	    if (order == null) return;
	    int orderId = order.getOrderId();
	    activeOrders.put(orderId, order);
	    
	}
	
	
	
	
	/**
	 * Retrieves an active order by its order ID.
	 * 
	 * @param orderId The ID of the order.
	 * @return The corresponding active order, or null if not found.
	 */
	public Order getActiveOrder(int orderId) {
		
	    return activeOrders.get(orderId);
	    
	}
	
	
	
	
	/**
	 * Removes the given order from the order book.
	 * Supports both limit and stop orders (market orders are not stored).
	 * 
	 * Thread-safe: protected by the global fair lock (orderBookLock).
	 * 
	 * @param order The order to be removed.
	 * @return true if the order was successfully removed, false otherwise.
	 */
	public boolean removeOrderFromBook(Order order) {
		
	    orderBookLock.lock();
	    
	    try {
	    	
	        boolean removed = false;
	        
	        if (order instanceof LimitOrder) {
	            removed = "bid".equalsIgnoreCase(order.getType())
	                    ? bidOrders.remove(order)
	                    : askOrders.remove(order);
	            
	        } else if (order instanceof StopOrder) {
	            removed = "bid".equalsIgnoreCase(order.getType())
	                    ? bidStopOrders.remove(order)
	                    : askStopOrders.remove(order);
	            
	        }
	        if (removed) persist();
	        return removed;
	        
	    } finally {
	        orderBookLock.unlock();
	    }
	    
	}


	/**
	 * Retrieves the current best bid price (highest bid price).
	 * 
	 * @return Best bid price, or -1 if there are no bid orders.
	 */
    public Integer getBestBidPrice() {
    	
        LimitOrder best = bidOrders.peek();
        return best == null ? -1 : best.getLimitPrice();
        
    }

    
    
    
	/**
	 * Retrieves the current best ask price (lowest ask price).
	 * 
	 * @return Best ask price, or -1 if there are no ask orders.
	 */
    public Integer getBestAskPrice() {
    	
        LimitOrder best = askOrders.peek();
        return best == null ? -1 : best.getLimitPrice();
        
        
    }
    
    


    /**
     * Adds a new limit order to the order book and triggers matching.
     * 
     * 1. Stores the order in activeOrders.
     * 2. Matches against the opposite side (askOrders for bid, bidOrders for ask).
     * 3. Checks and triggers stop orders after matching.
     * 4. Persists the updated order book state.
     * 
     * Thread-safe: protected by the global fair lock (orderBookLock).
     * 
     * @param order The limit order to be added.
     * @return Trade results grouped by username.
     */
    public Map<String, List<TradeInfo>> addLimitOrder(LimitOrder order) {
    	
        orderBookLock.lock();
        
        try {
        	
        	addActiveOrder(order);
            Map<String, List<TradeInfo>> tradeMap = matchOrder(order, order.getType().equalsIgnoreCase("bid") ? askOrders : bidOrders, "limit");
            checkTriggeredStopOrders();
            persist();
            return tradeMap;
            
        } finally {
            orderBookLock.unlock();
        }
        
    }

    
    
    
    
    /**
     * Adds a new market order and executes immediate matching.
     * 
     * 1. Matches against the opposite side (askOrders for bid, bidOrders for ask).
     * 2. Market orders must fully match or are rejected.
     * 3. Persists the updated order book state after matching.
     * 
     * Thread-safe: protected by the global fair lock (orderBookLock).
     * 
     * @param order The market order to be added.
     * @return Trade results grouped by username.
     */
    public Map<String, List<TradeInfo>> addMarketOrder(MarketOrder order) {
    	
        orderBookLock.lock();
        
        try {
        	
            Map<String, List<TradeInfo>> tradeMap = matchOrder(order, order.getType().equalsIgnoreCase("bid") ? askOrders : bidOrders, "market");
            persist();
            return tradeMap;
        } finally {
            orderBookLock.unlock();
        }
        
    }

    
    
    
    /**
     * Adds a new stop order to the order book.
     * 
     * 1. Stores the order in activeOrders.
     * 2. Adds to the corresponding stop order queue (bid or ask).
     * 3. Persists the updated order book state if the order was successfully added.
     * 
     * Thread-safe: protected by the global fair lock (orderBookLock).
     * 
     * @param order The stop order to be added.
     * @return true if the order was successfully added, false otherwise.
     */
    public boolean addStopOrder(StopOrder order) {
    	
        orderBookLock.lock();
        
        try {
        	addActiveOrder(order);
            boolean added = order.getType().equalsIgnoreCase("bid")
                    ? bidStopOrders.offer(order)
                    : askStopOrders.offer(order);
            if (added) persist();
            return added;
        } finally {
            orderBookLock.unlock();
        }
        
    }
  

    
    /**
     * Checks and triggers stop orders based on the current market prices.
     * 
     * 1. Triggers ask stop orders when the best bid price ≤ stop price.
     * 2. Triggers bid stop orders when the best ask price ≥ stop price.
     * 3. Converts triggered stop orders into market orders for immediate matching
     *    (using same id but removing the stopPrice field).
     * 4. Notifies users after each trigger.
     * 
     * Thread-safe: protected by the global fair lock (orderBookLock).
     */

    public void checkTriggeredStopOrders() {
    	
    	orderBookLock.lock();
    	
        try {
        	
        int bestBid = getBestBidPrice(); 
        int bestAsk = getBestAskPrice(); 
        
        
        while (!askStopOrders.isEmpty()) {
            StopOrder stopOrder = askStopOrders.peek();
            if (bestBid == -1 || bestBid > stopOrder.getStopPrice()) break;
            int size=stopOrder.getSize();
            stopOrder.setSize(0); // triggerd orders are considered as 
            // "finalized" although they may be rejected later
            askStopOrders.poll();
            
            MarketOrder marketOrder = new MarketOrder(
                stopOrder.getOrderId(),
                stopOrder.getUsername(),
                stopOrder.getType(),
                size,
                System.currentTimeMillis()/1000
            );

            Map<String, List<TradeInfo>> tradeMap = addMarketOrder(marketOrder);
            fixOrderTypeToStop(tradeMap);
            TradeHistory.addTrades(tradeMap);
            notifyUsers(tradeMap);
        }
        
        
        
        
        while (!bidStopOrders.isEmpty()) {
        	
            StopOrder stopOrder = bidStopOrders.peek();
            if (bestAsk == -1 || bestAsk < stopOrder.getStopPrice()) break;
            int size=stopOrder.getSize();
            stopOrder.setSize(0); // triggerd orders are considered as 
            // "finalized" although they may be rejected later
            bidStopOrders.poll(); 
            
            
            MarketOrder marketOrder = new MarketOrder(
            	stopOrder.getOrderId(),
                stopOrder.getUsername(),
                stopOrder.getType(),
                size,
                System.currentTimeMillis()/1000
            );

            Map<String, List<TradeInfo>> tradeMap = addMarketOrder(marketOrder);
            fixOrderTypeToStop(tradeMap);
            TradeHistory.addTrades(tradeMap);
            notifyUsers(tradeMap);
        } 
        
        
        }finally {
            orderBookLock.unlock();
        }
        
        
    }
    
    
    
    
    
    /**
     * Updates the order type field in the trade results to "stop".
     * 
     * Used to correctly label trades generated by triggered stop orders
     * (they were converted into a market order to get matching) when sending UDP notifications.
     * 
     * @param tradeMap The trade results grouped by username.
     */
    private void fixOrderTypeToStop(Map<String, List<TradeInfo>> tradeMap) {
    	
        for (List<TradeInfo> tradeList : tradeMap.values()) {
            for (TradeInfo trade : tradeList) {
                trade.setOrderType("stop"); 
            }
        }
        
    }

    
    /**
     * Matches the given order against the opposite side of the order book.
     * 
     * 1. Supports both limit and market orders.
     * 2. Market orders must be fully matched; otherwise, they are rejected.
     * 3. For limit orders, matches only if prices are compatible.
     * 4. Generates trade information for both sides and updates order sizes.
     * 5. Persists the order book state after each partial or full match.
     * 6. Unmatched limit orders (if any remaining) are added back to the book.
     * 
     * @param order The incoming order to be matched.
     * @param counterBook The opposite side of the order book.
     * @param orderType "limit" or "market".
     * @return Trade results grouped by username.
     */
    private Map<String, List<TradeInfo>> matchOrder(Order order, PriorityBlockingQueue<LimitOrder> counterBook, String orderType) {
    	
        Map<String, List<TradeInfo>> tradeMap = new HashMap<>();
        int remaining = order.getSize();

        // Check MarketOrder: must be fully traded
        if ("market".equalsIgnoreCase(orderType)) {
            int totalAvailable = 0;
            for (LimitOrder l : counterBook) {
                if (!l.getUsername().equals(order.getUsername())) { //Only count the opponent's orders
                    totalAvailable += l.getSize();
                    if (totalAvailable >= remaining) break;
                }
            }
            if (totalAvailable < remaining) {
                return tradeMap;
            }
        }
        

        Iterator<LimitOrder> iterator = counterBook.iterator();
        
        while (remaining > 0 && iterator.hasNext()) {
            LimitOrder topCounter = iterator.next();

            // Skip your own orders 
            if (order.getUsername().equals(topCounter.getUsername())) {
                continue;
            }

            // Check if the price matches for limit orders
            if ("limit".equalsIgnoreCase(orderType)) {
                boolean priceMatch =
                    (order.getType().equalsIgnoreCase("bid") && ((LimitOrder) order).getLimitPrice() >= topCounter.getLimitPrice()) ||
                    (order.getType().equalsIgnoreCase("ask") && ((LimitOrder) order).getLimitPrice() <= topCounter.getLimitPrice());
                if (!priceMatch) break;
            }

            int tradedSize = Math.min(remaining, topCounter.getSize());
            int tradePrice = topCounter.getLimitPrice();
            long timestamp = System.currentTimeMillis()/1000;

            // Generate trade information
            TradeInfo myTrade = new TradeInfo(order.getOrderId(), order.getType(), orderType, tradedSize, tradePrice, timestamp);
            tradeMap.computeIfAbsent(order.getUsername(), k -> new ArrayList<>()).add(myTrade);

            TradeInfo counterTrade = new TradeInfo(topCounter.getOrderId(), topCounter.getType(), "limit", tradedSize, tradePrice, timestamp);
            tradeMap.computeIfAbsent(topCounter.getUsername(), k -> new ArrayList<>()).add(counterTrade);

            remaining -= tradedSize;
            order.reduceSize(tradedSize);
            topCounter.reduceSize(tradedSize);
            persist();

            if (topCounter.getSize() == 0) {
                iterator.remove(); // remove completed counterparty orders
            }
        }

        // If it is a limit order, put it back into the order book if there is any left
        if ("limit".equalsIgnoreCase(orderType) && remaining > 0) {
            ((LimitOrder) order).setSize(remaining);
            PriorityBlockingQueue<LimitOrder> myBook = order.getType().equalsIgnoreCase("bid") ? bidOrders : askOrders;
            myBook.offer((LimitOrder) order);
        }

        return tradeMap;
        
    }

    
    
    
    
    /**
     * Sends trades notifications to all involved users via UDP.
     * 
     * 1. Converts trade info into JSON notifications.
     * 2. Looks up each user's registered UDP address.
     * 3. Sends the notification asynchronously using UdpNotifier.
     * 
     * @param tradeMap Trade results grouped by username.
     */
    public void notifyUsers(Map<String, List<TradeInfo>> tradeMap) {
    	
    	if (tradeMap.isEmpty()) return;

        for (Map.Entry<String, List<TradeInfo>> entry : tradeMap.entrySet()) {
            String user = entry.getKey();
            List<TradeInfo> trades = entry.getValue();
            
            ClosedTradesNotification notification = new ClosedTradesNotification(trades);
            String json = gson.toJson(notification);
            InetSocketAddress addr = UserUdpRegistry.getAddress(user);
            
            if (addr != null) {
                UdpNotifier.sendNotification(addr.getAddress().getHostAddress(), addr.getPort(), json);
            }
        }
    }

}
