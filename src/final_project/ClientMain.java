package final_project;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import com.google.gson.Gson;

import java.net.DatagramSocket;         
import java.net.DatagramPacket; 
import java.net.InetAddress;


public class ClientMain {
	// load server host address
    private static final String SERVER_HOST = ClientConfig.getSERVER_HOST();
    // the TCP port number that the server listens on
    private static final int SERVER_PORT_TCP = ClientConfig.getSERVER_PORT();
    // the UDP port number that the server listens on
    private static final int SERVER_PORT_UDP = ClientConfig.getSERVER_PORT_UDP();
    // use the GSON library for JSON serialization and deserialization
    private static final Gson gson = new Gson();
    
    
    /**
     * Main entry point for the interactive client end.
     * 
     * 1. Provides a simple text-based menu for user operations:
     *     1. Register
     *     2. Update credentials.
     *     3. Login and enter interactive mode.
     * 
     * 2. Uses standard input (Scanner) to read user choices.
     * 3. Continues prompting until the program is manually closed.
     */
    public static void main(String[] args) {
    	
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        
        // Main menu: register, update credentials, login (interactive mode), and exit
        while (!exit) {
        	System.out.println(
        		    "\nPlease choose an option:\n" +
        		    "1. Register\n" +
        		    "2. Update Credentials\n" +
        		    "3. Login\n" +
        		    "Enter your choice (int): "
        		);
            System.out.flush();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    register(scanner);
                    break;
                case "2":
                    updateCredentials(scanner);
                    break;
                case "3":
                    login(scanner);
                    break;
                default:
                	System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
        scanner.close();
        
    }//end main

    
    
    /**
     * Reads a non-empty string from the user input.
     * 
     * 1. Keeps prompting the user until a valid (non-empty) input is provided.
     * 2. Displays a custom prompt message before each input.
     * 
     * @param scanner The Scanner instance used for reading input.
     * @param prompt The prompt message to display to the user.
     * @return The validated non-empty string entered by the user.
     */
    private static String readNonEmptyString(Scanner scanner, String prompt) {
    	
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("❌ Input cannot be empty. Please enter a valid string.");
        }
        
    }
    
    /**
     * Reads a positive integer from user input.
     * 
     * 1. Continues prompting until the user provides a valid positive integer (1 to Integer.MAX_VALUE).
     * 2. Displays an error message for inputs invalid or out of range.
     * 
     * @param scanner The Scanner instance used for reading user input.
     * @param prompt The prompt message shown to the user.
     * @return The validated positive integer.
     */
    private static int readPositiveInt(Scanner scanner, String prompt) {
    	
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(input); // Use long first to prevent overflow
                if (value > 0 && value <= Integer.MAX_VALUE) {
                    return (int) value;
                } else {
                    System.out.println("❌ Input must be a positive integer (1 to " + Integer.MAX_VALUE + ").");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please enter an integer.");
            }
        }
        
    }
    
    /**
     * Reads and validates the order type ("ask" or "bid") from user input.
     * 
     * 1. Continues prompting the user until a valid input is provided.
     * 2. Accepts only exact matches: "ask" or "bid" (case sensitive).
     * 
     * @param scanner The Scanner instance used for reading user input.
     * @return The valid input string: "ask" or "bid".
     */
    private static String readTipo(Scanner scanner) {
    	
        while (true) {
            System.out.print("Enter tipo (ask or bid): ");
            String tipo = scanner.nextLine().trim();
            if (tipo.equals("ask") || tipo.equals("bid")) {
                return tipo;
            } else {
                System.out.println("❌ Invalid input. Please enter 'ask' or 'bid'.");
            }
        }
        
    }
    
    
    
    /**
     * Reads and validates a month input from the user in the "MMYYYY" format.
     * 
     * 1. Ensures the format is correct: two digits for the month (01-12) followed by four digits for the year.
     * 2. Continues prompting until the input is valid.
     * 3. Displays an error message for invalid formats.
     * 
     * @param scanner The Scanner instance used for reading user input.
     * @return The validated month string in "MMYYYY" format.
     */
    private static String readMonth(Scanner scanner) {
    	
        while (true) {
            System.out.print("Enter month (MMYYYY): ");
            String input = scanner.nextLine().trim();
            if (input.matches("^(0[1-9]|1[0-2])\\d{4}$")) {
                return input;
            } else {
                System.out.println("❌ Invalid format. Please enter month in MMYYYY format (e.g., 092024).");
            }
        }
        
    }
    
    
    

    
    
    
    
   
    /**
     * Handles the user registration process via TCP connection.
     * 
     * 1. Reads the username and password from user input.
     * 2. Builds the registration request in JSON format and sends it to the server.
     * 3. Waits for and processes the server's response.
     * 4. Uses try-with-resources to manage the TCP socket and I/O streams safely.
     * 5. Displays appropriate messages based on the result of the registration attempt.
     * 
     * @param scanner The Scanner instance used for reading user input.
     */
    private static void register(Scanner scanner) {
    	
    	// read input
    	String username = readNonEmptyString(scanner, "Enter username: ");
    	String password = readNonEmptyString(scanner, "Enter password: ");
    	
    	// open a TCP socket, and two streams for input and output
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT_TCP);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) 
        {
        	// construct the register request and serialize
            RegisterRequest.Values values = new RegisterRequest.Values(username, password);
            RegisterRequest request = new RegisterRequest("register", values);
            String jsonRequest = gson.toJson(request);

            // send requesto to server
            try {
                out.write(jsonRequest);
                System.out.println("📧 Sent JSON: " + jsonRequest);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                System.out.println("❌ A write error occurred while sending the register request:" + e.getMessage());
                e.printStackTrace();
                return;
            }
            // read and handle the server response
            try {
                String jsonResponse = in.readLine();
                if (jsonResponse != null) {
                    OperationResponse1 opResponse = gson.fromJson(jsonResponse, OperationResponse1.class);
                    System.out.println("↩️ " + opResponse.getResponse() + " - " + opResponse.getErrorMessage());
                    return;
                } else {
                    System.out.println("❌ The server did not respond during the register process");
                    return;
                }
            } catch (IOException e) {
                System.out.println("❌ An error occurred while reading the server response: " + e.getMessage());
                return;
            }
            
        }//end try with sources
     catch (IOException e) {
        System.out.println("❌ Failed to connect to server: " + e.getMessage());
        return;
    } 
        
    }//end register
    
    
    

    /**
     * Handles the process of updating user credentials via TCP connection.
     * 
     * 1. Reads the username, current password, and new password from user input.
     * 2. Builds the updateCredentials request in JSON format and sends it to the server.
     * 3. Waits for and processes the server's response.
     * 4. Uses try-with-resources to safely manage the socket and I/O streams. 
     * 5. Displays the result of the operation based on the server response.
     * 
     * @param scanner The Scanner instance used for reading user input.
     */
    private static void updateCredentials(Scanner scanner) {
    	
    	// read input
    	String username = readNonEmptyString(scanner, "Enter username: ");
    	String currentPassword = readNonEmptyString(scanner, "Enter current password: ");
    	String newPassword = readNonEmptyString(scanner, "Enter new password: ");
    	
    	// open a TCP socket, and two streams for input and output
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT_TCP);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            /// construct the credential update request and serialize 
            UpdateCredentialsRequest.Values values = new UpdateCredentialsRequest.Values(username, currentPassword, newPassword);
            UpdateCredentialsRequest request = new UpdateCredentialsRequest("updateCredentials", values);
            String jsonRequest = gson.toJson(request);

            // send to server
            try {
                out.write(jsonRequest);
                System.out.println("📧 Sent JSON: " + jsonRequest);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                System.out.println("❌ A write error occurred while sending an updateCredentials request: " + e.getMessage());
                return;
            }

            // read and handle the response
            try {
                String jsonResponse = in.readLine();
                if (jsonResponse != null) {
                    OperationResponse1 opResponse = gson.fromJson(jsonResponse, OperationResponse1.class);
                    System.out.println("↩️ " + opResponse.getResponse() + " - " + opResponse.getErrorMessage());
                    return;
                } else {
                    System.out.println("❌ The server did not respond during the updateCredentials process");
                    return;
                }
            } catch (IOException e) {
                System.out.println("❌ An error occurred while reading the server response: " + e.getMessage());
                return;
            }
        }// end try-with-resources
        catch (IOException e) {
            System.out.println("❌ Failed to connect to server: " + e.getMessage());
            return;
        }
        
    }

    
    
    
    
    
    /**
     * Handles the user login process and establishes a persistent TCP connection.
     * 
     * 1. Performs login via TCP.
     * 2. On successful login, sends a UDP registration packet to the server 
     *    (used by the server to map the username to the client's UDP address for notifications).
     * 3. Starts a separate thread to listen for incoming UDP notifications from the server 
     *    using the same UDP socket.
     * 4. Enters interactive mode, allowing the user to send further requests.
     * 5. Uses try-with-resources to safely manage TCP/UDP sockets and I/O streams.
     * 
     * @param scanner The Scanner instance used for reading user input.
     */
    private static void login(Scanner scanner) {
    	
    	// establishes a persistent TCP connection,
    	// open a UDP socket to send UDP address registration to server after logged in
        try(Socket socket = new Socket(SERVER_HOST, SERVER_PORT_TCP);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        	DatagramSocket udpSocket = new DatagramSocket();) 
        {
        	
        	// login failed, return directly
        	LoginResult result = performLogin(scanner, in, out);
            if (!result.success) return;

            // login succeeded
            System.out.println("✅ Persistent TCP connection established! You may now send requests.");

            // send a UDP address registration using UDP socket, 
    		// the server will memorize an association username - UDP address,
    		// for sending UDP notifications later
            try {
                String udpRegistrationMsg = result.username;
                InetAddress serverAddr = InetAddress.getByName(SERVER_HOST);
                DatagramPacket registrationPacket = new DatagramPacket(
                    udpRegistrationMsg.getBytes(), udpRegistrationMsg.length(),
                    serverAddr, SERVER_PORT_UDP);
                udpSocket.send(registrationPacket);
                System.out.println("📧 Client UDP registration sent on port: " + udpSocket.getLocalPort());
            } catch (IOException e) {
                System.out.println("❌ Failed to send UDP registration message: " + e.getMessage());
                e.printStackTrace();
                return; 
            }

            // create another thread to receive UDP notification asynchronously from server
            Thread udpThread = new Thread(new UdpReceiverClient(udpSocket));
            udpThread.start();
            
            // enter in interactive mode
            runInteractiveSession(scanner, in, out); 
            
        }//end try-with-resources
        catch (SocketException e) {
            System.out.println("❌ Server is not on service. Returning to main menu...");
            return;
    } catch (IOException e) {
        System.out.println("❌ I/O exception when operating on TCP socket: " + e.getMessage());
        e.printStackTrace();
        return;
    }
        
    }//end login
   
    
    
    
    
    
    
    
    /**
     * Performs the login authentication process.
     * 
     * 1. Reads the username and password from user input.
     * 2. Builds the login request in JSON format and sends it to the server.
     * 3. Waits for and processes the server's response.
     * 4. Returns a LoginResult indicating whether the login was successful 
     *    along with the username if login succeeded.
     * 
     * @param scanner The Scanner instance used for reading user input.
     * @param in BufferedReader for receiving server responses over TCP.
     * @param out BufferedWriter for sending requests to the server over TCP.
     * @return LoginResult containing success status and the username.
     * 
     */
    private static class LoginResult {
    	
        boolean success;
        String username;

        public LoginResult(boolean success, String username) {
            this.success = success;
            this.username = username;
        }
        
    }
    private static LoginResult performLogin(Scanner scanner, BufferedReader in, BufferedWriter out) {
    	
    	try {
    	// read input
        String username = readNonEmptyString(scanner, "Enter username: ");
        String password = readNonEmptyString(scanner, "Enter password: ");
        
        // prepare login request and serialize it
        LoginRequest.Values loginValues = new LoginRequest.Values(username, password);
        LoginRequest loginRequest = new LoginRequest("login", loginValues);
        String loginJson = gson.toJson(loginRequest);
        
        // send request 
        try {
            out.write(loginJson);
            System.out.println("📧 Sent JSON: " + loginJson);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println("❌ An error occurred while sending the login request: " + e.getMessage());
            e.printStackTrace();
            return new LoginResult(false, null);
        }
        
        // receive login response, if null then return false
        String jsonResponse=null;
        try {
            jsonResponse = in.readLine();
            if (jsonResponse == null) {
                System.out.println("❌ The server did not respond during the login process");
                return new LoginResult(false, null);
            }
        } catch (IOException e) {
            System.out.println("❌ An error occurred while reading the login response: " + e.getMessage());
            e.printStackTrace();
            return new LoginResult(false, null);
        }
        OperationResponse1 loginResponse = gson.fromJson(jsonResponse, OperationResponse1.class);
        System.out.println("↩️ "+loginResponse.getResponse() + " - " + loginResponse.getErrorMessage());
        
        // if login success return true
        return new LoginResult(loginResponse.getResponse() == 100, username);
    }catch (Exception e) {
        System.out.println("❌ An unexpected error occurred while logging in: " + e.getMessage());
        return new LoginResult(false, null);
    }
    	
 }
    
  
    

    
    
    
    
    

    /**
     * Runs the interactive session after a successful login.
     * 
     * 1. Presents a menu of available operations:
     *   1. Logout
     *   2. Insert Limit Order
     *   3. Insert Market Order
     *   4. Insert Stop Order
     *   5. Cancel Order
     *   6. Get Price History
     * 
     * 2. Reads user input, prepares the corresponding JSON request, and sends it via TCP.
     * 3. Receives and handles server responses accordingly.
     * 4. Manages session state, exiting interactive mode on successful logout or disconnection.
     * 
     * @param scanner The Scanner instance for user input.
     * @param in BufferedReader for reading server responses over TCP.
     * @param out BufferedWriter for sending requests to the server over TCP.
     * 
     */
    private static void runInteractiveSession(Scanner scanner, BufferedReader in, BufferedWriter out) {
    	
    	// we keep in interavtive mode unless the user logged out 
    	boolean interactive =true;
        while (interactive) {
        	//
        	System.out.println(
        		    "\nPlease choose an option:\n" +
        		    "1. logout\n" +
        		    "2. insertLimitOrder\n" +
        		    "3. insertMarketOrder\n" +
        		    "4. insertStopOrder\n" +
        		    "5. cancelOrder\n" +
        		    "6. getPriceHistory\n" +
        		    "Enter your choice (int): "
        		);
        	System.out.flush();
            String opChoice = scanner.nextLine().trim();
            String jsonRequest = "";
            
            // switch the input, prepare the corrisponding request in format JSON
            switch (opChoice) {
                case "1": // logout
                    LogoutRequest.Values logoutValues = new LogoutRequest.Values();
                    LogoutRequest logoutRequest = new LogoutRequest("logout", logoutValues);
                    jsonRequest = gson.toJson(logoutRequest);
                    break;
                case "2": // insertLimitOrder
                	String limitTipo = readTipo(scanner);
                	int limitDimensione = readPositiveInt(scanner, "Enter dimensione (int): ");
                	int prezzoLimite = readPositiveInt(scanner, "Enter prezzoLimite (int): ");
                    InsertLimitOrderRequest.Values limitValues = new InsertLimitOrderRequest.Values(limitTipo, limitDimensione, prezzoLimite);
                    InsertLimitOrderRequest limitRequest = new InsertLimitOrderRequest("insertLimitOrder", limitValues);
                    jsonRequest = gson.toJson(limitRequest);
                    break;
                case "3": // insertMarketOrder
                	String marketTipo = readTipo(scanner);
                    int marketDimensione = readPositiveInt(scanner, "Enter dimensione (int): ");
                    InsertMarketOrderRequest.Values marketValues = new InsertMarketOrderRequest.Values(marketTipo, marketDimensione);
                    InsertMarketOrderRequest marketRequest = new InsertMarketOrderRequest("insertMarketOrder", marketValues);
                    jsonRequest = gson.toJson(marketRequest);
                    break;
                case "4": // insertStopOrder
                    String stopTipo = readTipo(scanner);
                    int stopDimensione = readPositiveInt(scanner, "Enter dimensione (int): ");
                    int stopPrice = readPositiveInt(scanner, "Enter stopPrice (int): ");
                    InsertStopOrderRequest.Values stopValues = new InsertStopOrderRequest.Values(stopTipo, stopDimensione, stopPrice);
                    InsertStopOrderRequest stopRequest = new InsertStopOrderRequest("insertStopOrder", stopValues);
                    jsonRequest = gson.toJson(stopRequest);
                    break;
                case "5": // cancelOrder
                    int orderID = readPositiveInt(scanner, "Enter orderID: ");
                    CancelOrderRequest.Values cancelValues = new CancelOrderRequest.Values(orderID);
                    CancelOrderRequest cancelRequest = new CancelOrderRequest("cancelOrder", cancelValues);
                    jsonRequest = gson.toJson(cancelRequest);
                    break;
                case "6": // getPriceHistory
                	String mese = readMonth(scanner);
                    GetPriceHistoryRequest.Values historyValues = new GetPriceHistoryRequest.Values(mese);
                    GetPriceHistoryRequest historyRequest = new GetPriceHistoryRequest("getPriceHistory", historyValues);
                    jsonRequest = gson.toJson(historyRequest);
                    break;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
                    continue;
            }
            
            // send JSON request
            try {
                out.write(jsonRequest);
                System.out.println("📧 Sent JSON: " + jsonRequest);
                out.newLine();
                out.flush();
            } catch (SocketException e) {
                    System.out.println("❌ Server has closed the TCP connection. Returning to main menu...");
                    return;
            } catch (IOException e) {
                System.out.println("❌ I/O exception when operating on TCP socket: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            
            // receive response
            String jsonResponse=null;
			try {
				jsonResponse = in.readLine();
			} catch (IOException e) {
				System.out.println("❌ Server has closed the TCP connection. Returning to main menu..."); 
				return; 
			}
			if (jsonResponse==null) {
				System.out.println("❌ Server has closed the TCP connection. Returning to main menu..."); 
				return; 
			}
            
            // if user has selected logout and logout is successful, we quit interactive mode
            if(opChoice.equals("1")) {
                    OperationResponse1 opResponse = gson.fromJson(jsonResponse, OperationResponse1.class);
                    System.out.println("↩️ "+ opResponse.getResponse() + " - " + opResponse.getErrorMessage());
                    if(opResponse.getResponse()==100 && opResponse.getErrorMessage().equals("OK")) 
                    	interactive=false;
            }
            
            // have the right JSON class to deserialize
            // cancel operation has response type OperationResponse1 ( code + message)
            else if(opChoice.equals("5")) {
            		OperationResponse1 opResponse = gson.fromJson(jsonResponse, OperationResponse1.class);
            		System.out.println("↩️ "+opResponse.getResponse() + " - " + opResponse.getErrorMessage());
            }
            
            // insert limit/market/stop order operations have response type OperationResponse2 ( orderId or -1)
            else if(opChoice.equals("2")||opChoice.equals("3")||opChoice.equals("4")){
            		OperationResponse2 opResponse = gson.fromJson(jsonResponse, OperationResponse2.class);
            		System.out.println("↩️ "+opResponse.getOrderId());
            }
            
            // getPriceHistory operation has its own response type GetPriceHistoryResponse (code + meassage + price data)
            else {
            		GetPriceHistoryResponse opResponse = gson.fromJson(jsonResponse, GetPriceHistoryResponse.class);
            		if(opResponse.getResponse()==100) {
            		System.out.println("↩️ "+opResponse.getResponse() + " - " + opResponse.getErrorMessage() + " - " + opResponse.getData());
            		}
            		else 
            			System.out.println("↩️ "+opResponse.getResponse() + " - " + opResponse.getErrorMessage());
            			
            }
      }//end while(interactive)
        
      return;
      
    }
    

}//end TCPClient


