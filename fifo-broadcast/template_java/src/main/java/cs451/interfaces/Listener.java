package cs451.interfaces;

import cs451.message.Packet;

@FunctionalInterface
public interface Listener {
    void apply (Packet p);

}
