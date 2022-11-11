package cs451.link;

import cs451.helper.Pair;
import cs451.callbacks.Callback;
import cs451.message.Packet;
import cs451.process.Process;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(Process process, Callback callback) {
        super(callback, process);
        link = new StubbornLink(process, this::deliver);
    }

    private void deliver(Packet packet) {

        if (!packet.isAck() && getMyProcess().deliver(packet)) {
            callback(packet);
        } else if (packet.isAck()){
            getMyProcess().ack(new Pair(packet.backFromAck(getId()), packet.getSenderId()));
        }
    }

    public void send(Packet packet, int target) {
        getMyProcess().addSendPacket(packet, target);
    }

    public void stopThreads() {
        link.stopThreads();
    }

}
