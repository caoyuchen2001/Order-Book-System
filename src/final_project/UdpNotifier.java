package final_project;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UdpNotifier is responsible for sending asynchronous UDP notifications to clients.
 * 
 * Purpose:
 * 1. Used by the server to notify users trade results via UDP.
 * 2. Sends messages (formatted as JSON strings) to the client’s registered IP and port.
 * 3. Utilizes a thread pool (ExecutorService) to perform non-blocking asynchronous sending.
 *
 * Features:
 * 1. Each notification is handled in a separate thread from the cached thread pool.
 * 2. Ensures that UDP notification sending does not block the main server operations.
 */


public class UdpNotifier {
    
    private static final ExecutorService udpExecutor = Executors.newCachedThreadPool();

    
    public static void sendNotification(final String userIp, final int userPort, final String message) {
        udpExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buf = message.getBytes(StandardCharsets.UTF_8);
                    InetAddress address = InetAddress.getByName(userIp);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, userPort);
                    socket.send(packet);
                    socket.close();
                    System.out.println("[UDP Notifier] Sending UDP notification to " + userIp + ":" + userPort);
                } catch (Exception e) {
                    System.err.println("[UDP Notifier] UDP notification sending failed: " + e.getMessage());
                }
            }
        });
    }


	public static void shutdown() {
		udpExecutor.shutdown();
		
	}
}
