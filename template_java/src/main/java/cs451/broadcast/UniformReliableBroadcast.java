package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.link.Link;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;

    private final Map<Byte, Set<Integer>> urbDelivered = new HashMap<>();
    private final Map<Byte, Map<Byte, Set<Integer>>> bebDelivered = new HashMap<>();

    public UniformReliableBroadcast(Host host, int numHosts, PacketCallback packetCallback) {
        super(packetCallback, host.getId(), numHosts);
        broadcast = new BestEffortBroadcast(host, numHosts, this::deliver);

        for (byte h = 0; h < numHosts; h++) {

            urbDelivered.put(h, new HashSet<>());

            if (h != (byte) (getMyId() - 1)) {
                bebDelivered.put(h, Link.getProcess(h+1).getDelivered());
            } else {
                Map<Byte, Set<Integer>> localAck = new HashMap<>();
                for (byte l = 0; l < numHosts; l++) {
                    localAck.put(l, new HashSet<>());
                }
                bebDelivered.put(h, localAck);
            }
        }
    }

    private void deliver(Packet packet) {
        byte senderId = packet.getBOriginId();
        int packetId = packet.getPacketId();

        if (!urbDelivered.get(senderId).contains(packetId)) {

            if (!hasBroadcast(packetId, senderId)) {

                bebDelivered.get((byte) (getMyId() - 1)).get(senderId).add(packetId);
                broadcast(packet);

            } else if (canDeliver(packetId, senderId)) {

                urbDelivered.get(senderId).add(packetId);
                callback(packet);

            }
        }
    }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    public void broadcast(Packet packet) {
        broadcast.broadcast(packet);
    }

    private boolean hasBroadcast(int packetId, byte senderId) {
        if (senderId == (byte) (getMyId() - 1)) {
            return true;
        }

        return bebDelivered.get((byte) (getMyId() - 1)).get(senderId)
                .contains(packetId);
    }

    public boolean canDeliver(int packetId, byte senderId) {
        int offset = 0;
        if (senderId == (byte) (getMyId() - 1)) {
            offset = 1;
        }

        return bebDelivered.values().stream()
                .filter(x -> x.get(senderId).contains(packetId))
                .count() + offset > getNumHosts() / 2;
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
