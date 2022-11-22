package cs451.utilities;

public class Parameters {

    public static int NUM_HOSTS, MAJORITY;
    // Timeout used to initialize the hosts (ms)
    public static int BASE_TIMEOUT;

    // Maximum timeout a host can get due to exponential backoff (ms)
    public static int MAX_TIMEOUT;

    // Maximum random timeout to add variability to resend (ms)
    public static int THRESHOLD_TIMEOUT;

    // Maximum number of tries to retrieve a message while a null value is returned
    public static byte MAX_ATTEMPTS;

    // Maximum size of the queue of the packets to ack -> don't load other messages if the size of the queue is greater
    public static int LINK_BATCH;

    // Maximum size of the queue of the messages to reach the broadcast layer
    public static int BROADCAST_BATCH = 131072;;

    public static void setParams(int numHosts) {
        NUM_HOSTS = numHosts;
        MAJORITY = numHosts / 2 + 1;
        LINK_BATCH = 1024 / numHosts;
        switch (numHosts / 10) {
            case 0: case 1: case 2:
                BASE_TIMEOUT = 256;
                MAX_TIMEOUT = 16384;
                THRESHOLD_TIMEOUT = 16;
                MAX_ATTEMPTS = 2;
                break;
            case 3: case 4: case 5: case 6: case 7:
                BASE_TIMEOUT = 512;
                MAX_TIMEOUT = 32768;
                THRESHOLD_TIMEOUT = 32;
                MAX_ATTEMPTS = 3;
                break;
            default:
                BASE_TIMEOUT = 1024;
                MAX_TIMEOUT = 32768;
                THRESHOLD_TIMEOUT = 128;
                MAX_ATTEMPTS = 3;
        }
    }

    private Parameters() {}

}
