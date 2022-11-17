package cs451.message;

import cs451.process.Process;

public class TimedPacket {
    private final Packet packet;
    private final Process process;
    private int timestamp, timeout;

    public TimedPacket(Process process, Packet packet) {
        this.packet = packet;
        this.process = process;
        this.timeout = process.getTimeout();
        this.timestamp = (int) System.currentTimeMillis();
    }

    public Packet getPacket() {
        return packet;
    }

    public boolean timeoutExpired() {
        if (System.currentTimeMillis() - timestamp >= timeout) {
            return true;
        }
        return false;
    }

    public void update() {
        this.packet.updateTimestamp();
        this.timestamp = (int) System.currentTimeMillis();
        this.timeout = this.process.getTimeout();
    }

}
