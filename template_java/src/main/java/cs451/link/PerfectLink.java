package cs451.link;

import cs451.message.Packet;
import cs451.parser.Host;

import java.util.List;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(int id, int port, List<Host> hosts) {
        super(id, hosts);
        link = new StubbornLink(id, hosts, port, this::deliver);
    }

    private void deliver(Packet packet) {
        Process process = getProcess(this.getId());

        if (process.isTarget() && !process.hasDelivered(packet)) {
            process.deliver(packet);
            process.flagEvent(packet, packet.getSenderId(), true);
        } else if (!process.isTarget() && packet.isAck()){
            process.stopSending(packet);
        }
    }

    public void closeSocket() {
        link.closeSocket();
    }
}
