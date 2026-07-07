package org.example;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Utility to check whether the machine has an active internet connection.
 */
public class NetworkUtils {

    private static final String CHECK_HOST = "8.8.8.8"; // Google DNS
    private static final int CHECK_PORT = 53;
    private static final int TIMEOUT_MS = 1500;

    /**
     * Returns true if the machine can reach the internet.
     */
    public static boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(CHECK_HOST, CHECK_PORT), TIMEOUT_MS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
