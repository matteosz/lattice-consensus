package cs451.broadcast;

import cs451.callbacks.Callback;
import cs451.message.Packet;

public abstract class Broadcast {

    private Callback callback;
    private final int numHosts, myId;

    protected Broadcast(int myId, int numHosts) {
        this.numHosts = numHosts;
        this.myId = myId;
    }

    protected Broadcast(Callback callback, int myId, int numHosts) {
        this(myId, numHosts);
        this.callback = callback;
    }


    public int getNumHosts() {
        return numHosts;
    }

    public int getMyId() {
        return myId;
    }

    protected void callback(Packet packet) {
        callback.apply(packet);
    }

}
