package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.List;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(Process process, int port, Listener listener) {
        super(listener, process);
        link = new StubbornLink(process, port, this::deliver);
    }

    private void deliver(Packet packet) {

        if (!packet.isAck() && !myProcess.deliver(packet)) {
            handleListener(packet);
        } else if (packet.isAck()){
            myProcess.ack(packet);
        }
    }

    public void send(Packet packet) {
        myProcess.addSendPacket(packet);
    }

    public void stopThreads() {
        link.stopThreads();
    }

}
