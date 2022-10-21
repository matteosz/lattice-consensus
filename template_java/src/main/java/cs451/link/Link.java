package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Link {

    private Listener listener;

    private static final Map<Integer, Process> network = new HashMap<>();
    protected static int targetId;
    private final int id;

    protected Link(int id, List<Host> hosts) {
        this.id = id;
    }

    protected Link(Listener listener, int id, List<Host> hosts) {
        this(id, hosts);
        this.listener = listener;
    }

    public static void populateNetwork(List<Host> hosts, int targetId) {
        if (!network.isEmpty()) {
            return;
        }
        Link.targetId = targetId;
        for (Host host : hosts) {
            network.put(host.getId(), new Process(host, hosts.size(), targetId));
        }
    }

    protected void handleListener(Packet packet) {
        listener.apply(packet);
    }

    protected int getId() {
        return id;
    }

    public static Process getProcess(int id) {
        return network.get(id);
    }

    public static Map<Integer, Process> getNetwork() {
        return network;
    }
}
