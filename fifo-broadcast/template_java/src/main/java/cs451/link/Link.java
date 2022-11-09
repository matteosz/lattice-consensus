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

    protected final Process myProcess;
    private Listener listener;

    public static void populateNetwork(List<Host> hosts) {

        if (!network.isEmpty()) {
            return;
        }

        for (Host host : hosts) {
            network.put(host.getId(), new Process(host));
        }
    }

    public static Process getProcess(int id) {
        return network.get(id);
    }

    protected Link(Process process) {
        this.myProcess = process;
    }

    protected Link(Listener listener, Process process) {
        this(process);
        this.listener = listener;
    }

    public Process getMyProcess() {
        return myProcess;
    }

    protected void handleListener(Packet packet) {
        listener.apply(packet);
    }

}
