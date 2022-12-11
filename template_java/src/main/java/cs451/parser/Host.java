package cs451.parser;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static cs451.utilities.Utilities.fromIntegerToByte;

/**
 * Host class:
 * It represents a host in the network.
 */
public class Host {

    /** ID of the host in the system */
    private byte id;

    /** IP address of the host */
    private InetAddress ip;

    /** Port to use for the socket by the host */
    private int port;

    /**
     * Creates a host by parsing id, ip and port
     * @param idString host's id as string to parse
     * @param ipString host's ip address
     * @param portString host's socket port
     * @return true if correctly parsed, false otherwise
     */
    public boolean populate(String idString, String ipString, String portString) {
        try {
            int id = Integer.parseInt(idString);
            if (id <= 0 || id > 128) {
                System.err.println("Host id not valid");
                return false;
            }
            // Safely convert the integer to byte since it's in the interval [1, 128]
            this.id = fromIntegerToByte(id);
            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith("/")) {
                ip = InetAddress.getByName(ipTest.substring(1));
            } else {
                ip = InetAddress.getByName(ipTest.split("/")[0]);
            }
            port = Integer.parseInt(portString);
            if (port < 11000 || port > 11999) {
                System.err.println("Port value not correct");
                return false;
            }
        } catch (NumberFormatException | UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @return id of the host
     */
    public byte getId() {
        return id;
    }

    /**
     * @return ip address as InetAddress
     */
    public InetAddress getIpAsAddress() {
        return ip;
    }

    /**
      * @return host's port
     */
    public int getPort() {
        return port;
    }

}

