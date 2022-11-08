package cs451.broadcast;

import cs451.link.PerfectLink;
import cs451.message.Message;
import cs451.message.Packet;

import java.util.LinkedList;
import java.util.List;

public class BestEffortBroadcast {

    private final PerfectLink link;

    public BestEffortBroadcast(PerfectLink link) {
        this.link = link;
    }

    public void broadcast() {
    /*
        process = perfectLink.getProcess(myId);

        if (process.isTarget()) {
            return;
        }

        List<Message> packet = new LinkedList<>();

        for (int i = 1; i <= numMessages; i++) {

            Message m = Message.createMessage(targetId, i);

            process.sendEvent(m);

            packet.add(m);

            if (packet.size() == Packet.MAX_COMPRESSION) {
                process.load(packet);
                packet = new LinkedList<>();
            }

        }

        if (packet.size() > 0)
            process.load(packet);

     */
    }

    public void stopThreads() {
        link.stopThreads();
    }

}
