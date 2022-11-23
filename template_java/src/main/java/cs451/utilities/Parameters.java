package cs451.utilities;

public class Parameters {


    public static int BASE_TIMEOUT = 256;
    public static int MAX_TIMEOUT = 16384;
    public static int RANDOM_MAX = 128;
    public static byte MAX_ATTEMPTS = 3;
    public static int LINK_BATCH;
    public static int BROADCAST_BATCH = 131072;

    public static void setParams(int numHosts) {
        LINK_BATCH = 1024 / numHosts;
    }

    private Parameters() {}

}
