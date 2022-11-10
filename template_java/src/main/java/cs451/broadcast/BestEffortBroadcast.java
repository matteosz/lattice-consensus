package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.link.PerfectLink;
import cs451.process.Process;
import cs451.message.Packet;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;

    public BestEffortBroadcast(Process process, int myId, int numHosts, Listener listener) {
        super(listener, numHosts, myId);
        this.link = new PerfectLink(process, process.getHost().getPort(), listener);
    }

    public void broadcast(Packet packet) {
        for (int i = 1; i <= getNumHosts(); i++) {
            link.send(packet, i);
        }
    }

    public void stopThreads() {
        link.stopThreads();
    }

    public Process getProcess() {
        return link.getMyProcess();
    }
}
