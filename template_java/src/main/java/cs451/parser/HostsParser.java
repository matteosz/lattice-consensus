package cs451.parser;

import static cs451.process.Process.myHost;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse the hosts file by creating
 * the hosts and populating the network.
 */
public class HostsParser {

    /** Mapping the hosts info to their ids */
    public static final Map<Byte, Host> hosts = new HashMap<>();

    /**
     * Parse the hosts file.
     * @param key argument in the command line (--arg)
     * @param filename value after the key
     * @return true if correctly parsed, false otherwise
     */
    public static boolean populate(String key, String filename) {
        if (!key.equals("--hosts")) {
            System.err.println("Wrong arg used for hosts");
            return false;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                if (line.isBlank()) {
                    continue;
                }
                String[] splits = line.split("\\s+");
                if (splits.length != 3) {
                    System.err.println("File hosts: wrong formatted line");
                    return false;
                }
                Host newHost = new Host();
                if (!newHost.populate(splits[0], splits[1], splits[2])) {
                    return false;
                }
                hosts.put(newHost.getId(), newHost);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (!checkIdRange()) {
            return false;
        }
        // Check if local id is coherent with the parsed hosts
        if (myHost >= hosts.size()) {
            System.err.println("Wrong local id range");
            return false;
        }
        return true;
    }

    /**
     * Check if all hosts' ids are in a correct range.
     * @return true if all correct, false otherwise
     */
    private static boolean checkIdRange() {
        int num = hosts.size();
        for (Host host : hosts.values()) {
            if (host.getId() >= num) {
                System.err.println("Wrong id range");
                return false;
            }
        }
        return true;
    }

}
