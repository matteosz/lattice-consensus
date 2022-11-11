package cs451.broadcast;

import cs451.callbacks.Callback;
import cs451.helper.GenericPair;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;

    private final ConcurrentHashMap<GenericPair<Byte, Integer>, Set<Byte>> ack = new ConcurrentHashMap<>();
    private final Set<GenericPair<Byte, Integer>> delivered = ConcurrentHashMap.newKeySet();
    private final Map<GenericPair<Byte, Integer>, Packet> pending = new ConcurrentHashMap<>();

    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public UniformReliableBroadcast(Process process, int id, int numHosts, Callback callback) {
        super(callback, id, numHosts);
        broadcast = new BestEffortBroadcast(process, id, numHosts, this::deliver);

        worker.execute(this::processPending);
    }

    public void broadcast(Packet packet) {
        pending.put(new GenericPair<>((byte) packet.getOriginId(), packet.getPacketId()), packet);
        broadcast.broadcast(packet);
    }

    private void deliver(Packet packet) {
        GenericPair<Byte, Integer> packetInfo = new GenericPair<>((byte) packet.getOriginId(), packet.getPacketId());
        if (!ack.containsKey(packetInfo))
            ack.put(packetInfo, new HashSet<>());

        ack.get(packetInfo).add((byte) packet.getSenderId());

        if (!pending.containsKey(packetInfo)) {
            pending.put(packetInfo, packet);
            broadcast.broadcast(packet.setSenderId(getMyId()));
        }
    }

    private boolean canDeliver(GenericPair<Byte, Integer> packetInfo) {
        return ack.getOrDefault(packetInfo, new HashSet<>()).size() > getNumHosts() / 2;
    }

    private void processPending() {
        while (running.get()) {

            Iterator<GenericPair<Byte, Integer>> value = pending.keySet().iterator();
            while (value.hasNext()) {

                GenericPair<Byte, Integer> packetInfo = value.next();
                if (canDeliver(packetInfo) && delivered.add(packetInfo)) {
                    Packet packet = pending.get(packetInfo);
                    value.remove();
                    callback(packet);
                }

            }

        }
    }

    public Process getProcess() {
        return broadcast.getProcess();
    }

    public void stopThreads() {
        running.set(false);
        broadcast.stopThreads();
        worker.shutdownNow();
    }
}
