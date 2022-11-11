package cs451.link;

import cs451.callbacks.Callback;
import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Link {

    private static final Map<Integer, Process> network = new HashMap<>();
    public static final int MEMORY_LIMITER = 1 << 9;
    public static final int TIMEOUT_LIMIT = 1000;

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

    private final Process myProcess;
    private final Callback callback;

    protected Link(Callback callback, Process process) {
        this.myProcess = process;
        this.callback = callback;
    }

    protected Process getMyProcess() {
        return myProcess;
    }
    protected int getMyProcessId() {
        return myProcess.getHost().getId();
    }
    protected void callback(Packet packet) {
        callback.apply(packet);
    }

    public int getId() {
        return getMyProcessId();
    }

}
