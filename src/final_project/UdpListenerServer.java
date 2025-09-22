package final_project;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;


/**
 * UdpListenerServer is a server-side thread that listens for incoming UDP packets.
 * 
 * 
 * 1. After a client successfully logs in via TCP, it sends a UDP registration packet containing its username.
 * 2. This listener receives the packet, extracts the username and client's UDP address (IP + port),
 *    and registers this mapping in UserUdpRegistry.
 * 3. The mapping is later used by the server to send asynchronous UDP notifications (trade results) to the client.
 * 4. Listens on a fixed UDP port (54321). 
 * 5. Runs continuously in its own thread as long as the server is alive.
 * 6. Handles each incoming UDP packet individually, parsing and storing the client's information.
 * 
 */

public class UdpListenerServer implements Runnable {
	
    private static final int UDP_PORT = ServerConfig.getSERVER_PORT_UDP();  
    private static final int BUFFER_SIZE = ServerConfig.getBufSize();

    @Override
    public void run() {
    	
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
        	
            System.out.println("[UDP Lisener] UDP listening thread has been started on port: " + UDP_PORT);
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
            	
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                
                String username = new String(packet.getData(), 0, packet.getLength()).trim();
                InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());

                // Record to UserUdpRegistry
                UserUdpRegistry.register(username, clientAddress.getAddress().getHostAddress(), clientAddress.getPort());
                System.out.println("[UDP Lisener] Received UDP register from " + username + ", address: " + clientAddress);

                System.out.println("[UDP Lisener] UserUdpRegistry status：" + UserUdpRegistry.debugInfo());
            }

        } catch (Exception e) {
            System.err.println("[UDP Lisener] UDP listening exception: " + e.getMessage());
        }
    }
	
}
