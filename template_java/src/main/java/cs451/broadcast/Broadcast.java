package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.message.Packet;

public abstract class Broadcast {

    private final Listener listener;
    private final int numHosts, myId;

    protected Broadcast(Listener listener, int myId, int numHosts) {
        this.listener = listener;
        this.numHosts = numHosts;
        this.myId = myId;
    }
    protected Broadcast(int myId, int numHosts) {
        this.listener = null;
        this.numHosts = numHosts;
        this.myId = myId;
    }

    public int getNumHosts() {
        return numHosts;
    }

    public int getMyId() {
        return myId;
    }

    protected void handleListener(Packet packet) {
        listener.apply(packet);
    }

}
