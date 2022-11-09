package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.link.PerfectLink;
import cs451.process.Process;
import cs451.message.Packet;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;

    public BestEffortBroadcast(Process process, int port, int myId, int numHosts, Listener listener) {
        super(listener, numHosts, myId);
        this.link = new PerfectLink(process, port, this::deliver);
    }

    private void deliver(Packet p) {
        handleListener(p);
    }

    public void broadcast(Packet packet) {
        for (int i = 1; i <= getNumHosts(); i++)
            if (i != getMyId())
                link.send(packet);
    }

    public void stopThreads() {
        link.stopThreads();
    }
}
