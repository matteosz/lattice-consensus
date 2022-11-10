package cs451.link;

import cs451.helper.Pair;
import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.process.Process;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(Process process, Listener listener) {
        super(listener, process);
        link = new StubbornLink(process, this::deliver);
    }

    private void deliver(Packet packet) {

        if (!packet.isAck() && myProcess.deliver(packet)) {
            handleListener(packet);
        } else if (packet.isAck()){
            myProcess.ack(new Pair(packet, packet.getSenderId()));
        }
    }

    public void send(Packet packet, int target) {
        myProcess.addSendPacket(packet, target);
    }

    public void stopThreads() {
        link.stopThreads();
    }

}
