package cs451.link;

import cs451.interfaces.PackageListener;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class StubbornLink extends Link {
    private final FairLossLink link;

    public StubbornLink(int id, List<Host> hosts, int port, PackageListener listener, int targetId) {
        super(listener, id, hosts, targetId);
        link = new FairLossLink(id, hosts, port, this::deliver, targetId);

        Executors.newFixedThreadPool(1).execute(this::sendPackets);
    }

    @Override
    public void send(Message mex, int id) {
        Process p = getProcess(id);
        p.addMessageToProcess(mex);
    }

    @Override
    public void sendMany(int targetId, int sourceId, int numMessages) {
        Process p = getProcess(targetId);
        p.sendMany(sourceId, numMessages);
    }

    public void deliver(Packet pck) {
        if (!pck.isAck())
            link.enqueuePacket(pck.convertToAck(getId()), pck.getSenderId());
        handleListener(pck);
    }

    private void sendPackets() {
        for (;;)
            getNetwork().entrySet().stream()
                    .filter(x -> !x.getValue().isTarget())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    .forEach(this::processPacket);
    }

    private void processPacket(int id, Process process) {
        List<Packet> packets = process.getPacketsToSend();

        for (Packet p : packets) {
            link.enqueuePacket(p, id);
            process.flagEvent(p, false);
        }

    }
    /*
    private List<Message> getWaitingMessages(Process process) {
        List<Message> messages = new LinkedList<>();

        for (int i = 0; i < Packet.MAX_COMPRESSION; i++) {
            Message m = process.getWaitingMessage();
            if (m != null)
                messages.add(m);
        }
        return messages;
    }

    private void packAndSend(List<Message> messages, int hostId, Process process) {
        if (messages.isEmpty())
            return;
        Packet packet = Packet.createPacket(messages, process.getNextPacketId(), getId());
        link.enqueuePacket(packet, hostId);
        process.addPacketToConfirm(packet);
    }
    */
    public void closeSocket() {
        link.closeSocket();
    }

}
