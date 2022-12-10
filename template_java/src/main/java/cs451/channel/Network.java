package cs451.channel;

import static cs451.process.Process.getMyHost;

import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.Map;

/**
 * NETWORK
 *
 * It just populates the network given the information of the hosts
 */
public class Network {

    /** Mapping host id with the relative process class */
    private static final Map<Byte, Process> network = new HashMap<>();

    /**
     *
     * @param hosts information retrieved by the parser of the
     */
    public static void populateNetwork(Map<Byte, Host> hosts) {
        for (Map.Entry<Byte, Host> entry : hosts.entrySet()) {
            // No need to map my current host, since I won't send packets to myself
            if (entry.getKey() != getMyHost()) {
                network.put(entry.getKey(), new Process(entry.getValue()));
            }
        }
    }

    /**
     * Retrieves a process given its id
     * @param id of the host
     * @return Process of the given host
     */
    public static Process getProcess(byte id) {
        return network.get(id);
    }

    /**
     * @return the whole network map
     */
    public static Map<Byte, Process> getNetwork() {
        return network;
    }
}
