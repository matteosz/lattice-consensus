package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;

public abstract class Broadcast {

    private final PacketCallback packetCallback;
    private final int numHosts, myId;

    protected Broadcast(PacketCallback packetCallback, int myId, int numHosts) {
        this.numHosts = numHosts;
        this.myId = myId;
        this.packetCallback = packetCallback;
    }

    protected Broadcast(int myId, int numHosts) {
        this.numHosts = numHosts;
        this.myId = myId;
        this.packetCallback = null;
    }

    public int getNumHosts() {
        return numHosts;
    }

    public int getMyId() {
        return myId;
    }

    protected void callback(Packet packet) {
        packetCallback.apply(packet);
    }

}
