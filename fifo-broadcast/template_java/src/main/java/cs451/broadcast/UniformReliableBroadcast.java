package cs451.broadcast;

import cs451.link.PerfectLink;

public class UniformReliableBroadcast {

    private BestEffortBroadcast broadcast;

    public UniformReliableBroadcast(PerfectLink link) {
        broadcast = new BestEffortBroadcast(link);
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }

}
