package cs451.message;

public class TimedPacket {
    private final Packet packet;
    private int timestamp, timeout;

    public TimedPacket(int timeout, Packet packet) {
        this.packet = packet;
        this.timeout = timeout;
        this.timestamp = (int) System.currentTimeMillis();
    }

    public Packet getPacket() {
        return packet;
    }

    public boolean timeoutExpired() {
        return (int) System.currentTimeMillis() >= timeout + timestamp;
    }

    public void update(int timeout) {
        packet.updateTimestamp();
        timestamp = (int) System.currentTimeMillis();
        this.timeout = timeout;
    }

}
