package cs451.utilities;

import static cs451.message.Packet.MAX_COMPRESSION;
import static cs451.parser.ConfigParser.maxDistinctValues;
import static cs451.process.Process.NUM_HOSTS;

import cs451.message.Packet;

/**
 * Class containing some important parameters, tuned
 * heuristically depending on the number of hosts in the network.
 */
public class Parameters {

    /** Starting timeout for each host */
    public static int TIMEOUT = 1024;

    /** Threshold for the maximum timeout for a host */
    public static int MAX_TIMEOUT = 32768;

    /** Threshold to be arbitrary added to hosts' timeout to prevent collisions */
    public static int THRESHOLD = 15;

    /** Maximum number of packets to resend at a given time in the system */
    public static int LINK_BATCH = 2048;

    /** Window size for the proposal to be processed */
    public static int PROPOSAL_BATCH;

    /**
     * Tune the hyper-params depending on the number of hosts.
     */
    public static void setParams() {
        // Since the ack/nack are individual for process, the link batch is intended as for the whole network
        LINK_BATCH /= NUM_HOSTS;
        // Set global maximum packet size
        Packet.MAX_PACKET_SIZE = Packet.HEADER + MAX_COMPRESSION * (maxDistinctValues + 4) * Integer.BYTES;
        switch (NUM_HOSTS / 10) {
            // From 0 to 39 hosts
            case 0: case 1: case 2: case 3:
                PROPOSAL_BATCH = 64;
                break;
            // From 40 to 79 hosts
            case 4: case 5: case 6: case 7:
                PROPOSAL_BATCH = 32;
                break;
            // From 80 to 128 hosts
            default:
                PROPOSAL_BATCH = 16;
        }
        // Set the batch accordingly with ds
        if (maxDistinctValues > 150 && maxDistinctValues < 300) {
            PROPOSAL_BATCH >>= 1;
        } else if (maxDistinctValues >= 300 && maxDistinctValues < 600) {
            PROPOSAL_BATCH >>= 2;
            LINK_BATCH >>= 1;
        } else if (maxDistinctValues >= 600 && maxDistinctValues < 900) {
            PROPOSAL_BATCH >>= 3;
            LINK_BATCH >>= 1;
        } else if (maxDistinctValues >= 900) {
            PROPOSAL_BATCH >>= 4;
            LINK_BATCH >>= 1;
        }
    }

}
