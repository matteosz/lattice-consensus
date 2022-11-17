package cs451.link;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Link {

    private static Map<Integer, Process> network = new HashMap<>();

    public static void populateNetwork(List<Host> hosts, int id) {
        for (Host host : hosts) {
            if (host.getId() != id) {
                network.put(host.getId(), new Process(host, hosts.size()));
            }
        }
    }
    public static Process getProcess(int id) {
        return network.get(id);
    }
    public static Map<Integer, Process> getNetwork() {
        return network;
    }

    private final PacketCallback packetCallback;

    protected Link(PacketCallback packetCallback) {
        this.packetCallback = packetCallback;
    }

    protected void callback(Packet packet) {
        packetCallback.apply(packet);
    }

}
