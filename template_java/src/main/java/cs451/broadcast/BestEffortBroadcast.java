package cs451.broadcast;

import cs451.callbacks.Callback;
import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.process.Process;
import cs451.message.Packet;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;

    public BestEffortBroadcast(Process process, int myId, int numHosts, Callback callback) {
        super(callback, myId, numHosts);
        this.link = new PerfectLink(process, callback);
    }

    public void broadcast(Packet packet) {
        for (int i = 1; i <= getNumHosts(); i++) {
            if (i != getMyId()) {
                link.send(packet, i);
            } else {
                getProcess().deliverEvent(packet);
            }

        }
    }

    public void stopThreads() {
        link.stopThreads();
    }

    public Process getProcess() {
        return Link.getProcess(link.getId());
    }

}
