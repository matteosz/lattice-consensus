package cs451.parser;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static cs451.helper.Constants.PORT_MAX;
import static cs451.helper.Constants.PORT_MIN;

public class Host {

    private static final String IP_START_REGEX = "/";

    private int id;
    private InetAddress ip;
    private int port = -1;

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = InetAddress.getByName(ipTest.substring(1));
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]);
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
            if (port < PORT_MIN || port > PORT_MAX) {
                System.err.println("Port in the hosts file must be in the correct interval");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip.getHostAddress();
    }

    public InetAddress getIpAsAddress() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
