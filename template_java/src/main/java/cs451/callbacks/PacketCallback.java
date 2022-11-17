package cs451.callbacks;

import cs451.message.Packet;

@FunctionalInterface
public interface PacketCallback {
    void apply (Packet p);
}
