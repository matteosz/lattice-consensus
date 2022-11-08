package cs451.broadcast;

import cs451.link.PerfectLink;

public class FIFOBroadcast {

    private UniformReliableBroadcast broadcast;

    public FIFOBroadcast(PerfectLink link, int numMessages) {
        broadcast = new UniformReliableBroadcast(link);
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }

}
