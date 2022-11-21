package cs451.utilities;

public class Parameters {

    // Timeout used to initialize the hosts (ms)
    public static final int BASE_TIMEOUT = 1024;

    // Maximum timeout a host can get due to exponential backoff (ms)
    public static final int MAX_TIMEOUT = 32768;

    // Maximum random timeout to add variability to resend (ms)
    public static final int MAX_RANDOM = 128;

    // Maximum number of tries to retrieve a message while a null value is returned
    public static final byte MAX_ATTEMPTS = 3;

    // Maximum size of the queue of the messages to ack -> don't load other messages if the size of the queue is greater
    public static final int LINK_BATCH = 3000;

    private Parameters() {}

}
