package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.link.PerfectLink;
import cs451.message.Message;
import cs451.process.Process;
import cs451.message.Packet;

import java.util.LinkedList;
import java.util.List;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;

    public BestEffortBroadcast(Process process, int myId, int numHosts, Listener listener) {
        super(listener, numHosts, myId);
        this.link = new PerfectLink(process, process.getHost().getPort(), listener);
    }

    public void broadcast(Packet packet) {
        for (int i = 1; i <= getNumHosts(); i++) {
            link.send(packet, i);
        }
    }

    public void stopThreads() {
        link.stopThreads();
    }

    public Process getProcess() {
        return link.getMyProcess();
    }

    private void send(List<Message> messages, int packetNumber) {
        Packet packet = Packet.createPacket(messages, packetNumber, getMyId());
        broadcast(packet);
    }

    public void start(int numMessages) {
        List<Message> packet = new LinkedList<>();
        int packetNumber = 1;

        for (int m = 1; m <= numMessages; m++) {
            Message message = Message.createMessage(getMyId(), m);
            packet.add(message);
            getProcess().sendEvent(message);

            if (packet.size() == Packet.MAX_COMPRESSION) {
                send(packet, packetNumber++);
                packet.clear();
            }
        }

        if (packet.size() > 0) {
            send(packet, packetNumber);
        }

    }
}
