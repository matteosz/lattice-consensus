package cs451.utilities;

public class Parameters {

    public static int TIMEOUT, MAX_TIMEOUT = 32768, LINK_BATCH = 128, BROADCAST_BATCH = 10000, PROPOSAL_BATCH = 16;
    public static byte EMPTY_CYCLES = 3, THRESHOLD;

    public static void setParams(int numHosts) {
        LINK_BATCH /= numHosts;
        switch (numHosts / 10) {
            case 0: case 1: case 2: case 3:
                TIMEOUT = 64;
                THRESHOLD = 30;
                break;
            case 4: case 5: case 6: case 7:
                TIMEOUT = 256;
                THRESHOLD = 40;
                break;
            default:
                TIMEOUT = 300;
                THRESHOLD = 50;
        }
    }

    private Parameters() {}

}
