package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.List;
import java.util.concurrent.Executors;

public class StubbornLink extends Link {
    private final FairLossLink link;

    public StubbornLink(int id, List<Host> hosts, int port, Listener listener, int targetId) {
        super(listener, id, hosts, targetId);
        link = new FairLossLink(id, hosts, port, this::deliver, targetId);

        Executors.newFixedThreadPool(1).execute(this::sendPackets);
    }

    public void deliver(Packet pck) {
        if (!pck.isAck())
            link.enqueuePacket(pck.convertToAck(getId()), pck.getSenderId());
        handleListener(pck);
    }

    private void sendPackets() {
        for (;;) {
            processPacket(getProcess(getId()));
        }
    }

    private void processPacket(Process process) {
        List<Packet> packets = process.getPacketsToSend();

        for (Packet p : packets) {
            link.enqueuePacket(p, targetId);
            process.flagEvent(p, targetId, false);
        }
    }

    public void closeSocket() {
        link.closeSocket();
    }

}
