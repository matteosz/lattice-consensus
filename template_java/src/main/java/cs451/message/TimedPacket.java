package cs451.message;

public class TimedPacket implements Comparable<TimedPacket> {
    private final Packet packet;
    private long timestamp, timeout;

    public TimedPacket(long timeout, Packet packet) {
        this.packet = packet;
        this.timeout = timeout;
        this.timestamp = System.currentTimeMillis();
    }

    public Packet getPacket() {
        return packet;
    }

    public boolean timeoutExpired() {
        return System.currentTimeMillis() > timeout + timestamp;
    }

    public void update(long timeout) {
        packet.updateTimestamp();
        timestamp = System.currentTimeMillis();
        this.timeout = timeout;
    }

    @Override
    public int compareTo(TimedPacket o) {
        long time = System.currentTimeMillis();
        return Long.compare(time - this.timestamp, time - o.timestamp);
    }
}
