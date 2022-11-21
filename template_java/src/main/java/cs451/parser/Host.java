package cs451.parser;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static cs451.utilities.Utilities.fromIntegerToByte;

public class Host {

    private static final String IP_START_REGEX = "/";

    private byte id;
    private InetAddress ip;
    private int port = -1;

    public boolean populate(String idString, String ipString, String portString) {
        try {
            int id = Integer.parseInt(idString);

            if (id <= 0 || id > 128) {
                return false;
            }
            this.id = fromIntegerToByte(id);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = InetAddress.getByName(ipTest.substring(1));
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]);
            }

            port = Integer.parseInt(portString);
            if (port < 11000 || port > 11999) {
                return false;
            }
        } catch (NumberFormatException | UnknownHostException e) {
            return false;
        }

        return true;
    }

    public byte getId() {
        return id;
    }

    public InetAddress getIpAsAddress() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
