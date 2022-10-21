package cs451.interfaces;

import cs451.message.Packet;

@FunctionalInterface
public interface PackageListener {
    void apply (Packet p);
}
