package final_project;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;



public class ServerMain {
	
    // The port number that the server listens on
    private static final int SERVER_PORT = ServerConfig.getSERVER_PORT();
    // Maximum user inactivity time threshold
    private static final int INACTIVITY_THRESHOLD = ServerConfig.getINACTIVITY_THRESHOLD(); 
    // Use the GSON library for JSON serialization and deserialization
    private static final Gson gson = new Gson();
    
    
    /**
     * Main entry point for the trading server.
     * 
     * 1. Loads user data, order history, trade history, order book and 
     *    the order ID generator from JSON files.
     * 2. Starts a UDP listener thread for handling user UDP registrations.
     * 3. Uses a cached thread pool to manage TCP client connections.
     * 4. Listens for incoming TCP connections and handles each client in a separate thread.
     * 5. Server remains active, continuously accepting and processing client requests.
     * 
     * 
     */
    public static void main(String[] args) {
    	
    	// Load registered user data
    	RegisteredUsers.load();
        // Load order history
        OrderHistory.load();
        // Loading persistent trade history
        TradeHistory.load();
        // Load the order book 
        OrderBook.getInstance().load();
        // Load order ID generator
        OrderIdGenerator.load();

        // Use a cached thread pool to handle client threads.
        // This executor reuses previously constructed threads when available,
        // and terminates idle threads that have not been used for 60 seconds.
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("[Main] Server is on service, listening on port: " + SERVER_PORT);
        
        // Start the UDP registration listening thread. 
        // After the user successfully logs in, a UDP packet with the user name is sent 
        // to the server so that the server can record the user's UDP port 
        // for sending UDP notifications.
        Thread udpThread = new Thread(new UdpListenerServer());
        udpThread.setDaemon(true); // Automatically exit when the main program is closed
        udpThread.start();
        
        // Register a shutdown hook to gracefully terminate resources when the JVM exits.
        // This ensures that the thread pool and UDP notifier are properly shut down.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        	System.out.println("[Main] Shutdown hook triggered. Cleaning up...");
        	executor.shutdown();
        	UdpNotifier.shutdown();
        }));


        // Accept TCP connection request
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while (true) {
                // Waiting for client connection request
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Main] Accepting TCP connection from " + clientSocket.getInetAddress().getHostAddress() 
                	    + ":" + clientSocket.getPort());
                
                // Submit each client connection to the thread pool for processing
                executor.execute(new Runnable() { @Override public void run() { handleClient(clientSocket);}});
            }
        } catch (IOException e) {
            System.err.println("[Main] Server out of service: " + e.getMessage());
        }
        

    }//end main
    
    

    /**
     * Handles a single TCP client connection.
     * 
     * 1. Supports user registration, credential updates, login and other operations.
     * 2. Maintains user login state per thread using currentUser.
     * 3. Uses JSON over TCP for request and response communication.
     * 4. Enforces inactivity timeout to automatically disconnect users.
     * 5. After successful login, allows access to additional operations.
     * 6. Sends appropriate responses back to the client and ensures cleanup on disconnection.
     * 7. Thread-safe per connection: each client is handled in a separate thread.
     *
     * @param clientSocket The client's TCP socket connection.
     * 
     */
    private static void handleClient(Socket clientSocket) {
    	// Set the current user ID. Each thread handles 
    	// only one user's connection at a time.
    	MutableString currentUser = new MutableString(null);
    	
    	// Get current thread name for log
    	String threadName = "[" + Thread.currentThread().getName() + "] ";
    	System.out.println(threadName+"Handling connection from "
    		    + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
    	
    	// Set the socket read timeout. If the user is inactive for a period of time, 
    	// an exception will be thrown and the connection will be disconnected.
    	try {
			clientSocket.setSoTimeout(INACTIVITY_THRESHOLD);
		} catch (SocketException e) {
			e.printStackTrace();
		}
    	
    	// Open I/O streams to handle client communication.
    	// Automatically closes streams when done or on error.
        try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())))
        {
        	// Read the JSON format request sent by the client 
        	// assuming each request ends with a newline character
            String jsonRequest;
            
            while((jsonRequest = in.readLine()) != null) {
            	System.out.println(threadName+"Received JSON: " + jsonRequest); 
            	
                // Deserialize JSON into a jsonObject object
            	JsonObject jsonObject = JsonParser.parseString(jsonRequest).getAsJsonObject();
            	String operation = jsonObject.get("operation").getAsString();
       
                // Determine what operation to perform based on "operation"
            	if (currentUser.getValue() == null) {
                    // Not logged in: only register, updateCredentials, login operations are allowed
                    if ("register".equalsIgnoreCase(operation)) {
                    	// Deserialize
                        RegisterRequest regReq = gson.fromJson(jsonRequest, RegisterRequest.class);
                        // Process register
                        OperationResponse1 regResp = processRegister(regReq.getValues().getUsername(), 
                                                                    regReq.getValues().getPassword());
                        // Send back the response through TCP connection
                        try {
                            out.write(gson.toJson(regResp));
                            out.newLine();
                            out.flush();
                        } catch (IOException e) {
                            System.err.println(threadName+"Error sending response: " + e.getMessage());
                        }
                        break;// Jump out of while
                        
                    } else if ("updateCredentials".equalsIgnoreCase(operation)) {
                    	// Deserialize
                        UpdateCredentialsRequest updReq = gson.fromJson(jsonRequest, UpdateCredentialsRequest.class);
                        // Process updateCredentials
                        OperationResponse1 updResp = processUpdateCredentials(
                                updReq.getValues().getUsername(), 
                                updReq.getValues().getCurrentPassword(), 
                                updReq.getValues().getNewPassword());
                        // Send back the response through TCP connection
                        try {
                            out.write(gson.toJson(updResp));
                            out.newLine();
                            out.flush();
                        } catch (IOException e) {
                            System.err.println(threadName+"Error sending response: " + e.getMessage());
                        }
                        break;// Jump out of while
                        
                    } else if ("login".equalsIgnoreCase(operation)) {
                    	// Deserialize
                        LoginRequest loginReq = gson.fromJson(jsonRequest, LoginRequest.class);
                        // Process login
                        OperationResponse1 loginResp = processLogin(loginReq.getValues().getUsername(), 
                                                                  loginReq.getValues().getPassword());
                        // Send back the response through TCP connection
                        try {
                            out.write(gson.toJson(loginResp));
                            out.newLine();
                            out.flush();
                        } catch (IOException e) {
                            System.err.println(threadName+"Error sending response: " + e.getMessage());
                        }
                        
                        // If login succeded, set currentUser, so other operations are allowed 
                        if (loginResp.getResponse() == 100) {
                            currentUser.setValue(loginReq.getValues().getUsername());
                        }
                        else 
                        	break;// Jump out of while
                    } 
                }//end if (currentUser == null)
            	else {
            		// Process 6 operations in the logged-in state
                    Object respObj = processOperation(operation, jsonRequest, currentUser, threadName);
                    try {
                        out.write(gson.toJson(respObj));
                        out.newLine();
                        out.flush();
                    } catch (IOException e) {
                        System.err.println(threadName+"Error sending response: " + e.getMessage());
                    }
                  
                    // If the operation is logout and succeeds, exit the loop
                    if ("logout".equalsIgnoreCase(operation)&&((OperationResponse1)respObj).getResponse()==100) {
                    	break;// Jump out of while
                    }    
                }// end if else (currentUser == null)
            }//end while(jsonRequest != null)
        }//end try
        catch (SocketTimeoutException e) {
        	System.out.println(threadName+"Automatic logout: User inactivity for a long time");
        }
        catch (IOException e) {
            System.err.println(threadName+"I/O Exception: " + e.getMessage());
        }
        catch (Exception e) { 
            System.err.println(threadName+"Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
        		// Close the client socket, set user to not logged in.
        		try {
        			System.out.println(threadName+"Disconnecting the TCP connection from " + clientSocket.getInetAddress());
            		clientSocket.close();
            	} 
        		catch (IOException e) {
        			System.err.println(threadName+"Error closing socket: " + e.getMessage());
            	}
        		if (currentUser.getValue() != null) {
                    User user = RegisteredUsers.get(currentUser.getValue());
                    if (user != null && user.isLoggedIn()) {
                        user.setLoggedIn(false);
                        RegisteredUsers.persistUsers();
                        System.out.println(threadName+"Logout completed: user " + currentUser.getValue() + " set to not logged in.");
                    }
                }
         }
        
    }//handleClient
    
    
    /**
     * Handles user registration logic.
     * 
     * 1. Rejects empty or null passwords.
     * 2. Fails if the username is already used by others.
     * 3. Adds the new user to the registered user map on success.
     * 
     * @param username The desired username.
     * @param password The password for the new account.
     * @return OperationResponse1 indicating success or failure.
     */
    private static OperationResponse1 processRegister(String username, String password) {
    	
        if (password == null || password.trim().isEmpty()) {
            return new OperationResponse1(101, "Invalid password");
        }
        if (RegisteredUsers.contains(username)) {
            return new OperationResponse1(102, "Username not available");
        }
        RegisteredUsers.add(username, password);
        return new OperationResponse1(100, "OK");
        
    }
    
    
    
    /**
     * Handles the user credential update process.
     * 
     * 1. Verifies that the user exists and is not currently logged in.
     * 2. Checks that the current password matches.
     * 3. Rejects invalid or unchanged new passwords.
     * 4. Updates the password and persists the user data on success.
     * 
     * @param username The username of the account.
     * @param currentPassword The current password.
     * @param newPassword The new password to be set.
     * @return OperationResponse1 indicating success or specific failure reason.
     */
    private static OperationResponse1 processUpdateCredentials(String username, String currentPassword, String newPassword) {
    	
        if (!RegisteredUsers.contains(username)) {
            return new OperationResponse1(102, "User not found");
        }
        User user = RegisteredUsers.get(username);
        
        if (user.isLoggedIn())
            return new OperationResponse1(104, "User currently logged in");
        else if (!user.getPassword().equals(currentPassword)) 
            return new OperationResponse1(102, "Username/old password mismatch");
        else if (newPassword == null || newPassword.trim().isEmpty()) {
            return new OperationResponse1(101, "Invalid new password");
        }
        else if(currentPassword.equals(newPassword))
        	 return new OperationResponse1(103, "New password equal to old one");
        
        user.setPassword(newPassword);
        RegisteredUsers.persistUsers();
        return new OperationResponse1(100, "OK");
        
    }
    
    
    /**
     * Handles the user login process.
     * 
     * 1. Verifies that the user exists and the password matches.
     * 2. Prevents multiple concurrent logins by the same user.
     * 3. Sets the user's login status to true and persists the change.
     * 
     * @param username The username of the account.
     * @param password The password provided for login.
     * @return OperationResponse1 indicating success or specific failure reason.
     */
    private static OperationResponse1 processLogin(String username, String password) {
    	
        if (!RegisteredUsers.contains(username)) {
        	
            return new OperationResponse1(101, "User doesnot exist");
        }
        
        User user = RegisteredUsers.get(username);
        if (!user.getPassword().equals(password)) {
            return new OperationResponse1(101, "Username/password mismatch");
        }
        
        if (user.isLoggedIn()) {
            return new OperationResponse1(102, "User already logged in");
        }
        
        user.setLoggedIn(true);
        RegisteredUsers.persistUsers();
        return new OperationResponse1(100, "OK");
    }
    
  
    
    /**
     * Dispatches the requested operation to the corresponding handler based on the operation name.
     * 
     * 1. Supports logout, insert limit/market/stop order, cancel order, and get price history.
     * 2. Deserializes the incoming JSON request into the appropriate request object.
     * 3. Ensures that the correct handler is called for each operation type.
     * 
     * @param operation The operation name specified by the client.
     * @param jsonRequest The JSON request string.
     * @param currentUser The currently logged-in user (thread local).
     * @param threadName The name of the current thread (for logout) to log event.
     * @return The operation response object.
     */
    private static Object processOperation(String operation, String jsonRequest, MutableString currentUser, String threadName) {
    	
        switch (operation.toLowerCase()) {
            case "logout":
                return processLogout(currentUser, threadName);
                
            case "insertlimitorder":
                InsertLimitOrderRequest limit = gson.fromJson(jsonRequest, InsertLimitOrderRequest.class);
                return processInsertLimitOrder(currentUser, limit);
                
            case "insertmarketorder":
                InsertMarketOrderRequest market = gson.fromJson(jsonRequest, InsertMarketOrderRequest.class);
                return processInsertMarketOrder(currentUser, market);
                
            case "insertstoporder":
                InsertStopOrderRequest stop = gson.fromJson(jsonRequest, InsertStopOrderRequest.class);
                return processInsertStopOrder(currentUser, stop);
                
            case "cancelorder":
                CancelOrderRequest cancel = gson.fromJson(jsonRequest, CancelOrderRequest.class);
                return processCancelOrder(currentUser, cancel);
                
            case "getpricehistory":
                GetPriceHistoryRequest price = gson.fromJson(jsonRequest, GetPriceHistoryRequest.class);
                return processGetPriceHistory(price);
                
            default:
                return new OperationResponse1(101, "Unsupported operation in interactive mode");
        }
        
    }
    
    
    
    
    
    /**
     * Handles the user logout process.
     * 
     * 1. Verifies that the user is currently logged in.
     * 2. Sets the user's login status to false and persists the change.
     * 
     * @param currentUser The currently logged-in user (thread-local).
     * @param threadName The name of the current thread (for logging purposes).
     * @return OperationResponse1 indicating success or failure.
     */
    private static OperationResponse1 processLogout(MutableString currentUser, String threadName) {
    	
        User user = RegisteredUsers.get(currentUser.getValue());
        if (!user.isLoggedIn()) {
            return new OperationResponse1(101, "User not logged in");
        }
        user.setLoggedIn(false);
        RegisteredUsers.persistUsers();
        System.out.println(threadName+"Logout completed: user " + currentUser.getValue() + " set to not logged in.");
        return new OperationResponse1(100, "OK");
        
    }
    
    
    
    
    
    /**
     * Processes the insertion of a new limit order from the logged-in user.
     * 
     * 1. Constructs a LimitOrder object using the provided request parameters.
     * 2. Records a copy of the order in OrderHistory.
     * 3. Adds the order to the order book and performs matching.
     * 4. Updates trade history and notifies involved users of any trades.
     * 
     * @param currentUser The currently logged-in user (thread-local).
     * @param req The limit order request containing order details.
     * @return OperationResponse2 containing the generated order ID.
     */
    private static OperationResponse2 processInsertLimitOrder(MutableString currentUser, InsertLimitOrderRequest req) {
        
        String type = req.getValues().getType();
        int size = req.getValues().getSize();
        int limitPrice = req.getValues().getlimitPrice();
        long timestamp = System.currentTimeMillis()/1000;
        
        //Construct a limit order object
        int orderId = OrderIdGenerator.getNextOrderId();
        LimitOrder order = new LimitOrder(orderId, currentUser.getValue(), type, size, timestamp, limitPrice);
        
        // Write a copy to OrderHistory
        OrderHistory.addOrder(OrderUtils.copyOrder(order));
        
        // Add limit order to order book, match orders and obtain each user's trades record Map
        Map<String, List<TradeInfo>> tradeMap = OrderBook.getInstance().addLimitOrder(order);

        // Add to trade history and send notification
        TradeHistory.addTrades(tradeMap);
        OrderBook.getInstance().notifyUsers(tradeMap);
        
        return new OperationResponse2(orderId);
        
    }


    
    /**
     * Processes the insertion of a new market order from the logged-in user.
     * 
     * 1. Constructs a MarketOrder object using the request parameters.
     * 2. Records a copy of the order in OrderHistory.
     * 3. Adds the order to the order book and performs immediate matching.
     * 4. Rejects the order if it cannot be fully matched (market orders must fully execute).
     * 5. Updates trade history and notifies involved users of any trades.
     * 
     * @param currentUser The currently logged-in user (thread-local).
     * @param req The market order request containing order details.
     * @return OperationResponse2 with the generated order ID, or -1 if matching failed.
     */
    private static OperationResponse2 processInsertMarketOrder(MutableString currentUser, InsertMarketOrderRequest req) {
      
        String type = req.getValues().getType();
        int size = req.getValues().getSize();
        int orderId = OrderIdGenerator.getNextOrderId();
        long timestamp = System.currentTimeMillis()/1000;
        
        // Construct order
        MarketOrder order = new MarketOrder(orderId, currentUser.getValue(), type, size, timestamp);
        // Write to OrderHistory 
        OrderHistory.addOrder(OrderUtils.copyOrder(order));

        // Add market order to order book and match orders, and obtain each user's trades record Map
        Map<String, List<TradeInfo>> tradeMap = OrderBook.getInstance().addMarketOrder(order);
        
        // Matching failed (unable to trade market order completely), so reject order
        if (tradeMap.isEmpty()) {
            return new OperationResponse2(-1);
        }

        // Add to trade history and send notification
        TradeHistory.addTrades(tradeMap);
        OrderBook.getInstance().notifyUsers(tradeMap);

        return new OperationResponse2(orderId);
        
    }



    /**
     * Processes the insertion of a new stop order from the logged-in user.
     * 
     * 1. Constructs a StopOrder object using the request parameters.
     * 2. Records a copy of the order in OrderHistory.
     * 3. Adds the stop order to the order book for future triggering.
     * 4. Returns failure if the order could not be added.
     * 
     * @param currentUser The currently logged-in user (thread-local).
     * @param req The stop order request containing order details.
     * @return OperationResponse2 with the generated order ID, or -1 if adding failed.
     */
    private static OperationResponse2 processInsertStopOrder(MutableString currentUser, InsertStopOrderRequest req) {
        
        String type = req.getValues().getType(); 
        int size = req.getValues().getSize();
        int stopPrice = req.getValues().getStopPrice();


        // Construct StopOrder
        int orderId = OrderIdGenerator.getNextOrderId();
        long timestamp = System.currentTimeMillis()/1000;
        StopOrder order = new StopOrder(orderId, currentUser.getValue(), type, size, timestamp, stopPrice);

        // // Write to OrderHistory
        OrderHistory.addOrder(OrderUtils.copyOrder(order));

        // Try to add into the order book
        boolean success = OrderBook.getInstance().addStopOrder(order);
        if (!success) {
            return new OperationResponse2(-1); // failed
        }

        return new OperationResponse2(orderId);
        
    }


    /**
     * Handles the user's request to cancel an order.
     *
     * The cancellation logic follows these steps:
     *
     * 1. The method first looks for the order in activeOrders (a map in OrderBook 
     *    which contains all LimitOrder and StopOrder, regardless traded or not).
     *
     * 2. If the order is found:
     *      1. It checks whether the order belongs to the requesting user;
     *      2. If the order has already been fully executed (size = 0), it cannot be canceled;
     *      3. If the order is not traded fully, it attempts to remove it from the order book;
     *      4. If the removal fails, the order has likely already been canceled.
     *
     * 3. If the order is not found in activeOrders:
     *      1. The method looks for it in OrderHistory:
     *        According to the current design, an order present in OrderHistory 
     *        but missing from activeOrders is **necessarily a MarketOrder**, because:
     *          1. All LimitOrder and StopOrder are added to activeOrders at creation time;
     *          2. The system **never removes** orders from activeOrders, even after full execution;
     *          3. MarketOrders are **never added** to activeOrders, but only recorded in OrderHistory;
     *
     *        => Therefore, if the order is found only in OrderHistory, the system directly returns a message
     *           indicating that MarketOrders cannot be canceled.
     *
     * 4. If the order is not found in either activeOrders or OrderHistory, 
     *    the method concludes that the order does not exist.
     */
    private static OperationResponse1 processCancelOrder(MutableString currentUser, CancelOrderRequest req) {
    	
        int orderId = req.getValues().getOrderId();

        // Check if the order exists in activeOrders
        Order order = OrderBook.getInstance().getActiveOrder(orderId);
        
        if (order != null) {
        	// Check if order belongs to the current user
            if (!order.getUsername().equals(currentUser.getValue())) {
                return new OperationResponse1(101, "Order belongs to a different user");
            }

            // Check if the order is fully traded(limit and stop order), or fully rejected(stop order)
            // (every time a stop order is triggerd, we set its size to zero)
            if (order.getSize() == 0) {
                return new OperationResponse1(101, "Order has already been finalized");
            }

            // Remove from order book
            boolean removed = OrderBook.getInstance().removeOrderFromBook(order);
            if (removed) {
                return new OperationResponse1(100, "OK");
            } else {
                return new OperationResponse1(101, "Order has been cancelled already");
            }
        }

        // Not found in activeOrders, check the order history 
        Order historyOrder = OrderHistory.getOrder(orderId);
        if (historyOrder != null) {
            return new OperationResponse1(101, "Order is a Market Order cannot be cancelled");
        }

        // This order does not exist at all
        return new OperationResponse1(101, "Order does not exist");
        
    }


    /**
     * Processes the request to retrieve price history for a given month.
     * 
     * 1. Attempts to read trade data from our trade history file.
     * 2. If no data is found, falls back to reading "storicoOrdini.json".
     * 3. Filters trades by the requested month (format: "MMYYYY") and aggregates daily price data.
     * 
     * @param req The request containing the target month.
     * @return GetPriceHistoryResponse with the aggregated data or an error message.
     */
    private static GetPriceHistoryResponse processGetPriceHistory(GetPriceHistoryRequest req) {
    	
        String month = req.getValues().getMonth(); // e.g. "042025"
        Map<String, DailyPriceData> resultMap = extractTradesFromFile(ServerConfig.getTradeHistoryFile(), month);
        
        if (resultMap.isEmpty()) {
            resultMap = extractTradesFromFile("storicoOrdini.json", month);
        }

        if (resultMap.isEmpty()) {
            return new GetPriceHistoryResponse(101, "We don't have data related to the month you have indicated", null);
        }

        return new GetPriceHistoryResponse(100, "OK", resultMap);
        
    }
    
    
    
    /**
     * Extracts trade data from a JSON file and groups it by day for a specific month.
     * Only trades whose timestamp falls within the specified month (format MMYYYY) are included.
     *
     * @param filename the path to the JSON file containing trade data
     * @param month    the target month in MMYYYY format (e.g., "042025" for April 2025)
     * @return a map where each key is a day of the month (two-digit string),
     *         and the value is a DailyPriceData object containing trades for that day
     */
    private static Map<String, DailyPriceData> extractTradesFromFile(String filename, String month) {
    	
        Map<String, DailyPriceData> resultMap = new TreeMap<>();
        
        try (JsonReader reader = new JsonReader(new FileReader(filename))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                
                // Look for the "trades" array in the JSON structure
                if ("trades".equals(name)) {
                    reader.beginArray();
                    
                    // Parse each trade object inside the "trades" array
                    while (reader.hasNext()) {
                        long ts = 0;
                        int price = 0;
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String field = reader.nextName();
                            if ("timestamp".equals(field)) {
                                ts = reader.nextLong();// timestamp in seconds
                            } else if ("price".equals(field)) {
                                price = reader.nextInt();// trade price
                            } else {
                                reader.skipValue();// ignore other fields
                            }
                        }
                        reader.endObject();
                        
                        // Convert timestamp to LocalDateTime	
                        LocalDateTime time = Instant.ofEpochSecond(ts)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        // Format day and month for grouping and filtering
                        String day = String.format("%02d", time.getDayOfMonth());
                        String tradeMonth = String.format("%02d%04d", time.getMonthValue(), time.getYear());
                        
                        // Only include trades in the specified month
                        if (!month.equals(tradeMonth)) continue;
                        
                        // Add the trade to the appropriate day entry
                        resultMap.computeIfAbsent(day, d -> new DailyPriceData()).addTrade(price, ts);
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();// Skip unrelated fields outside "trades"
                }
            }
            reader.endObject();
        } catch (IOException e) {
            System.err.println("❌ Error reading file " + filename + ": " + e.getMessage());
        }
        return resultMap;
        
    }


}//end TCPServer




