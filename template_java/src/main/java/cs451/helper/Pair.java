package cs451.helper;

import cs451.message.Packet;

import java.util.Objects;

public class Pair {
    private Packet packet;
    private int target;

    public Pair(Packet packet, int target) {
        this.packet = packet;
        this.target = target;
    }

    public Packet getPacket() {
        return packet;
    }

    public int getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return target == pair.target && packet.equals(pair.packet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packet, target);
    }
}
