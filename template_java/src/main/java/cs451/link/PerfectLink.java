package cs451.link;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.parser.Host;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(Host host, PacketCallback packetCallback) {
        super(packetCallback);
        link = new StubbornLink(host, this::deliver);
    }

    private void deliver(Packet packet) {

        if ((!packet.isAck() && !getProcess(packet.getLastSenderId()).hasDelivered(packet)) ||
                (packet.isAck() && getProcess(packet.getLastSenderId()).ack(packet))) {
            getProcess(packet.getLastSenderId()).deliver(packet);
            callback(packet);
        }

    }
    public void load(int numMessages, int targetId) {
        getProcess(targetId).load(numMessages);
    }
    public void send(Packet packet, int target) {
        getProcess(target).addPacket(packet);
    }

    public void stopThreads() {
        link.stopThreads();
    }
}
