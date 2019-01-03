package com.alipay.sofa.ark.common.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author qilong.zql
 * @author khotyn
 * @since 0.5.0
 */
public class PortSelectUtils {

    /**
     * The minimum candidate port number of IPv4
     */
    public static final int MIN_PORT_NUMBER = 1100;

    /**
     * The maximum candidate port number of IPv4
     */
    public static final int MAX_PORT_NUMBER = 65535;

    /**
     * Select appropriate port among specify interval
     *
     * @param defaultPort specify the starting port
     * @param maxLength specify the size of interval
     * @return return available port
     */
    public synchronized static int selectAvailablePort(int defaultPort, int maxLength) {
        for (int i = defaultPort; i < defaultPort + maxLength; i++) {
            try {
                if (available(i)) {
                    return i;
                }
            } catch (IllegalArgumentException e) {
                // Ignore and continue
            }
        }

        return -1;
    }

    private static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        }
        return false;
    }

}