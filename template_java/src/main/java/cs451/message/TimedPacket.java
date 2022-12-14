package cs451.message;

import cs451.utilities.Parameters;

/**
 * It associates a timestamp and timeout to
 * a certain packet to manage the resending.
 */
public class TimedPacket {

    /** Encapsulated packet */
    private final Packet packet;

    /** Timestamp of the creation of the timed packet */
    private long timestamp;

    /** Timeout of the host to which this packet is directed */
    private long timeout;

    /**
     * Create a TimedPacket from parameters.
     * @param timeout host's timeout
     * @param packet encapsulated packet
     */
    public TimedPacket(long timeout, Packet packet) {
        this.packet = packet;
        this.timeout = timeout;
        // Set timestamp to current time in ms
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return encapsulated packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * @return true if host's timeout has expired
     */
    public boolean timeoutExpired() {
        long time = System.currentTimeMillis();
        if (time - timestamp > timeout) {
            if (Parameters.DEBUG) {
                System.out.println(
                    "Timeout was: " + timeout + ", time alive is: " + (time - timestamp));
            }
            return true;
        }
        return false;
    }

    /**
     * Update the timestamp and timeout.
     * @param timeout new host's timeout
     */
    public void update(long timeout) {
        packet.updateTimestamp();
        timestamp = packet.getTimestamp();
        this.timeout = timeout;
    }

}
