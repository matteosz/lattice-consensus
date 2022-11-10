package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.message.Packet;

public abstract class Broadcast {

    private Listener listener;
    private final int numHosts, myId;

    protected Broadcast(int myId, int numHosts) {
        this.numHosts = numHosts;
        this.myId = myId;
    }

    protected Broadcast(Listener listener, int myId, int numHosts) {
        this(myId, numHosts);
        this.listener = listener;
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
