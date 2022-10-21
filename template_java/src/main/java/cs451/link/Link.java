package cs451.link;

import cs451.interfaces.LinkInterface;
import cs451.interfaces.MessageListener;
import cs451.interfaces.PackageListener;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Link implements LinkInterface {

    private MessageListener messageListener;
    private PackageListener packageListener;

    private static final Map<Integer, Process> network = new HashMap<>();

    private final int id;

    protected Link(int id, List<Host> hosts, int targetId) {
        this.id = id;
        populateNetwork(hosts, targetId);
    }

    protected Link(MessageListener listener, int id, List<Host> hosts, int targetId) {
        this(id, hosts, targetId);
        messageListener = listener;
        packageListener = null;
    }

    protected Link(PackageListener listener, int id, List<Host> hosts, int targetId) {
        this(id, hosts, targetId);
        messageListener = null;
        packageListener = listener;
    }

    private void populateNetwork(List<Host> hosts, int targetId) {
        if (!network.isEmpty())
            return;
        for (Host host : hosts) {
            int i = host.getId();
            if (i != id) {
                Process p;
                p = new Process(host, hosts.size(), targetId==id);
                network.put(i, p);
            }
        }
    }

    protected void handleListener(Message message) {
        messageListener.apply(message);
    }

    protected void handleListener(Packet packet) {
        packageListener.apply(packet);
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
