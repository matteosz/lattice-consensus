package cs451.utilities;

import cs451.parser.HostsParser;

/**
 * Class containing some important parameters, tuned
 * heuristically depending on the number of hosts in the network.
 */
public class Parameters {

    public static final boolean DEBUG = true;

    /** Starting timeout for each host */
    public static int TIMEOUT = 16;

    /** Threshold for the maximum timeout for a host */
    public static int MAX_TIMEOUT = 512;

    /** Threshold to be arbitrary added to hosts' timeout to prevent collisions */
    public static int THRESHOLD = 10;

    /** Maximum number of packets to resend at a given time in the system */
    public static int LINK_BATCH = 1024;

    /** Maximum capacity for the queue containing the delivered proposals from the Perfect link */
    public static int BROADCAST_BATCH = 10000;

    /** Window size for the proposal to be processed */
    public static int PROPOSAL_BATCH = 16;

    /** Maximum number of allowed miss from retrieving an ack or nack proposal to send */
    public static byte MAX_MISS = 2;

    /**
     * Tune the hyperparams depending on the number of hosts.
     */
    public static void setParams() {
        int numHosts = HostsParser.hosts.size();
        // Since the ack/nack are individual for process, the link batch is intended as for the whole network
        LINK_BATCH /= numHosts;
        switch (numHosts / 10) {
            // From 0 to 39 hosts
            case 0: case 1: case 2: case 3:
                break;
            // From 40 to 79 hosts
            case 4: case 5: case 6: case 7:
                break;
            // From 80 to 128 hosts
            default:
        }
    }

}
