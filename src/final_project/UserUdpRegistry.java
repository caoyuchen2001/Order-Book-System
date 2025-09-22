package final_project;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;


/**
 * UserUdpRegistry maintains the mapping between usernames and their corresponding 
 * UDP addresses (IP and port) for sending notification messages.
 *
 * 1. Uses ConcurrentHashMap to ensure thread-safe access and updates.
 * 2. The register() method adds or updates a user's UDP address.
 * 3. The getAddress() method retrieves the registered UDP address for a given username.
 */

public class UserUdpRegistry {
    private static final ConcurrentHashMap<String, InetSocketAddress> userUdpMap = new ConcurrentHashMap<>();

    public static void register(String username, String ip, int port) {
        userUdpMap.put(username, new InetSocketAddress(ip, port));
    }

    public static InetSocketAddress getAddress(String username) {
        return userUdpMap.get(username);
    }

    public static ConcurrentHashMap<String, InetSocketAddress> debugInfo(){
    	return userUdpMap;
    }
}
