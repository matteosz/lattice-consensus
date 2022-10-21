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

    private final int id;
    protected final int targetId;

    protected Link(int id, List<Host> hosts, int targetId) {
        this.id = id;
        this.targetId = targetId;
        populateNetwork(hosts, targetId);
    }

    protected Link(Listener listener, int id, List<Host> hosts, int targetId) {
        this(id, hosts, targetId);
        this.listener = listener;
    }

    private void populateNetwork(List<Host> hosts, int targetId) {
        if (!network.isEmpty())
            return;
        for (Host host : hosts) {
            network.put(host.getId(), new Process(host, hosts.size(), targetId==id));
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
