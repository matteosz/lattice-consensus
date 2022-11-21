package cs451.message;

public class TimedPacket implements Comparable<TimedPacket> {
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

    @Override
    public int compareTo(TimedPacket o) {
        int time = (int) System.currentTimeMillis();
        return Integer.compare(time - this.timestamp, time - o.timestamp);
    }
}
