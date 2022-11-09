package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.process.Process;

public class UniformReliableBroadcast extends Broadcast {

    private BestEffortBroadcast broadcast;

    public UniformReliableBroadcast(Process process, int port, int id, int numHosts, Listener listener) {
        super(listener, id, numHosts);
        broadcast = new BestEffortBroadcast(process, port, id, numHosts, this::deliver);
    }

    private void deliver(Packet p) {

        handleListener(p);
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
