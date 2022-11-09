package cs451.broadcast;

import cs451.message.Packet;
import cs451.process.Process;

public class FIFOBroadcast extends Broadcast {

    private UniformReliableBroadcast broadcast;

    public FIFOBroadcast(Process process, int port, int id, int numHosts) {
        super(id, numHosts);
        broadcast = new UniformReliableBroadcast(process, port, id, numHosts, this::deliver);
    }

    private void deliver(Packet p) {

    }

    public void start(int numMessages) {
        //Packet packet = Packet.createPacket(messages, packetNumber, myProcess.getHost().getId(), targetId);
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
        broadcast.stopThreads();
    }

}
