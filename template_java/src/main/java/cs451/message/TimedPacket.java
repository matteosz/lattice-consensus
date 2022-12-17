package cs451.message;

/**
 * It associates a timestamp and timeout to
 * a certain packet to manage the resending.
 */
public class TimedPacket {

    /** Encapsulated packet. */
    private final Packet packet;

    /** Timestamp of the creation of the timed packet. */
    private long timestamp;

    /** Timeout of the host to which this packet is directed. */
    private long timeout;

    /** Whether the underlying packet is a collection of ACK proposals. */
    private final boolean ack;

    /**
     * Create a TimedPacket from parameters.
     * @param timeout host's timeout.
     * @param packet encapsulated packet.
     */
    public TimedPacket(long timeout, Packet packet, boolean ack) {
        this.packet = packet;
        this.timeout = timeout;
        // Set timestamp to current time in ms
        this.timestamp = System.currentTimeMillis();
        this.ack = ack;
    }

    /**
     * @return encapsulated packet.
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * @return true if host's timeout has expired.
     */
    public boolean timeoutExpired() {
        return System.currentTimeMillis() - timestamp > timeout;
    }

    /**
     * Update the timestamp and timeout.
     * @param timeout new host's timeout.
     */
    public void update(long timeout) {
        packet.updateTimestamp();
        timestamp = packet.getTimestamp();
        this.timeout = timeout;
    }

    /**
     * @return true if the packet is a collection of ack, false otherwise.
     */
    public boolean isAck() {
        return ack;
    }

}
