package cs451.callbacks;

import cs451.message.Packet;

@FunctionalInterface
public interface Callback {
    void apply (Packet p);

}
