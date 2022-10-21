package cs451.link;

import cs451.interfaces.MessageListener;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.List;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(int id, int port, List<Host> hosts, MessageListener listener, int targetId) {
        super(listener,id, hosts, targetId);
        link = new StubbornLink(id, hosts, port, this::deliver, targetId);
    }

    private void deliver(Packet packet) {
        Process process = getProcess(this.getId());

        if (process.isTarget() && !process.hasDelivered(packet)) {
            process.deliver(packet);
            process.flagEvent(packet, true);
        } else if (!process.isTarget() && packet.isAck()){
            process.stopSending(packet);
        }
    }


    @Override
    public void send(Message m, int targetId) {
        link.send(m, targetId);
    }

    @Override
    public void sendMany(int targetId, int sourceId, int numMessages) {
        link.sendMany(targetId, sourceId, numMessages);
    }

    public void closeSocket() {
        link.closeSocket();
    }
}
