package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Link {

    private static final Map<Integer, Process> network = new HashMap<>();

    private final int id;
    private Listener listener;

    public static void populateNetwork(List<Host> hosts) {

        if (!network.isEmpty()) {
            return;
        }

        for (Host host : hosts) {
            network.put(host.getId(), new Process(host, hosts.size()));
        }
    }

    protected Link(int id) {
        this.id = id;
    }

    protected Link(Listener listener, int id) {
        this(id);
        this.listener = listener;
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

}