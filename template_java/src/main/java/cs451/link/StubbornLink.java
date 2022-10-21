package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;

import java.util.List;
import java.util.concurrent.Executors;

public class StubbornLink extends Link {

    private final FairLossLink link;

    public StubbornLink(int id, int port, Listener listener) {

        super(listener, id);
        link = new FairLossLink(id, port, this::deliver);

        Executors.newFixedThreadPool(1).execute(this::sendPackets);
    }

    public void deliver(Packet pck) {

        if (!pck.isAck()) {
            link.enqueuePacket(pck.convertToAck(getId()), pck.getSenderId());
        }

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
            link.enqueuePacket(p, Link.targetId);
            process.flagEvent(p, Link.targetId, false);
        }

    }

    public void closeSocket() {
        link.closeSocket();
    }

}
