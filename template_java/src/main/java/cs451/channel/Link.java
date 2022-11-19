package cs451.channel;

import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Link {

    private static Map<Byte, Process> network = new HashMap<>();

    public static void populateNetwork(List<Host> hosts) {
        int numHosts = hosts.size();
        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            if (h != Process.getMyHost()) {
                network.put(h, new Process(hosts.get(h), numHosts));
            }
        }
    }
    public static Process getProcess(byte id) {
        return network.get(id);
    }
    public static Map<Byte, Process> getNetwork() {
        return network;
    }

    private final Consumer<Packet> packetCallback;

    protected Link(Consumer<Packet> packetCallback) {
        this.packetCallback = packetCallback;
    }

    protected void callback(Packet packet) {
        packetCallback.accept(packet);
    }

}
