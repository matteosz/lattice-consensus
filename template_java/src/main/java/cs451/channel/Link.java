package cs451.channel;

import static cs451.process.Process.getMyHost;

import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Link {

    private static final Map<Byte, Process> network = new HashMap<>();

    public static void populateNetwork(Map<Byte, Host> hosts) {
        int numHosts = hosts.size();
        for (Map.Entry<Byte, Host> entry : hosts.entrySet()) {
            if (entry.getKey() != getMyHost()) {
                network.put(entry.getKey(), new Process(entry.getValue(), numHosts));
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
