package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.link.Link;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;

    private final Map<Byte, Set<Integer>> delivered = new HashMap<>();
    private final Map<Byte, Map<Byte, Set<Integer>>> acked = new HashMap<>();

    public UniformReliableBroadcast(Host host, int numHosts, PacketCallback packetCallback) {
        super(packetCallback, host.getId(), numHosts);
        broadcast = new BestEffortBroadcast(host, numHosts, this::deliver);

        for (int h = 0; h < numHosts; h++) {
            delivered.put((byte) h, new HashSet<>());
            if (h != getMyId() - 1) {
                acked.put((byte) h, Link.getProcess(h+1).getDelivered());
            } else {
                Map<Byte, Set<Integer>> localAck = new HashMap<>();
                for (int l = 0; l < numHosts; l++) {
                    if (l != getMyId() - 1) {
                        localAck.put((byte) l, new HashSet<>());
                    }
                }
                acked.put((byte) h, localAck);
            }
        }
    }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    public void broadcast(Packet packet) {
        broadcast.broadcast(packet);
    }

    private void deliver(Packet packet) {
        int senderId = packet.getOriginId(), packetId = packet.getPacketId();
        if (!delivered.get((byte) (senderId - 1)).contains(packetId)) {

            if (!hasBroadcast(packet)) {

                acked.get((byte) (getMyId() - 1)).get((byte) (senderId - 1)).add(packetId);
                broadcast(packet);

            } else if (canDeliver(packet)) {

                delivered.get((byte) (senderId - 1)).add(packetId);
                callback(packet);

            }
        }
    }

    private boolean hasBroadcast(Packet packet) {
        if (packet.getOriginId() == getMyId())
            return true;

        return acked.get((byte) (getMyId() - 1)).get((byte) (packet.getOriginId() - 1))
                .contains(packet.getPacketId());
    }

    public boolean canDeliver(Packet packet) {
        return acked.values().stream()
                .filter(x -> x.get((byte) (packet.getOriginId() - 1)).contains(packet.getPacketId()))
                .count() > getNumHosts() / 2;
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
