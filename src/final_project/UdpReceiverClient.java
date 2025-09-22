package final_project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * UdpReceiverClient listens for UDP notifications from the server 
 * and prints trade execution results received asynchronously.
 */
public class UdpReceiverClient implements Runnable {
	
    private final DatagramSocket udpSocket;
    private static final int BUFFER_SIZE = ClientConfig.getBufSize();

    public UdpReceiverClient(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    @Override
    public void run() {
    	
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            while (true) {
            	
                DatagramPacket notifyPacket = new DatagramPacket(buf, buf.length);
                udpSocket.receive(notifyPacket);// throw SocketException if socket is closed
                String udpMsg = new String(notifyPacket.getData(), 0, notifyPacket.getLength());

                // Parse JSON
                JsonObject json = JsonParser.parseString(udpMsg).getAsJsonObject();
                JsonArray trades = json.getAsJsonArray("trades");
             
                for (JsonElement elem : trades) {
                		JsonObject trade = elem.getAsJsonObject();
                		int orderId = trade.get("orderId").getAsInt();
                		String type = trade.get("type").getAsString();
                		String orderType = trade.get("orderType").getAsString();
                		int size = trade.get("size").getAsInt();
                		int price = trade.get("price").getAsInt();

                		System.out.println("📮 UDP Notification: "+"Your "+type+" "+orderType+" order " + orderId + " has been finalized: " + size + " units at price " + price + ".");
                }
                
            }
        } catch (IOException e) {
        	
            if (e.getMessage() != null && e.getMessage().contains("Socket closed")) {
                System.out.println("✅ UDP Receiver socket has been closed since you have logged out.");
            } else {
                System.err.println("❌ Error in UDP Receiver: " + e.getMessage());
            }
            
    }
}
}
