package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;

public class FIFOBroadcast extends Broadcast {

    private UniformReliableBroadcast broadcast;

    private final Map<Byte, Set<Packet>> fifoDelivered = new HashMap<>();
    private final Map<Byte, Integer> fifoOrder = new HashMap<>();
    private PacketCallback broadcastCallback;

    public FIFOBroadcast(Host host, int numHosts, PacketCallback broadcastCallback, PacketCallback deliverCallback) {
        super(deliverCallback, host.getId(), numHosts);
        this.broadcastCallback = broadcastCallback;

        this.broadcast = new UniformReliableBroadcast(host, numHosts, this::deliver);

        for (byte h = 0; h < numHosts; h++) {
            fifoDelivered.put(h, new TreeSet<>(Comparator.comparingInt(Packet::getPacketId)));
            fifoOrder.put(h, 0);
        }
     }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    private void deliver(Packet packet) {
        byte senderId = packet.getBOriginId();
        int packetId = packet.getPacketId();

        fifoDelivered.get(senderId).add(packet);

        int lsn = fifoOrder.get(senderId);
        Iterator<Packet> iterator = fifoDelivered.get(senderId).iterator();

        while (iterator.hasNext()) {

            Packet p = iterator.next();
            if (p.getPacketId() == lsn + 1) {

                if (senderId == (byte) (getMyId() - 1)) {
                    broadcastCallback.apply(p);
                }
                callback(p);

                iterator.remove();
                lsn++;
            } else {
                fifoOrder.put(senderId, lsn);
                break;
            }

        }

    }

    public void stopThreads() {
        broadcast.stopThreads();
    }

}
