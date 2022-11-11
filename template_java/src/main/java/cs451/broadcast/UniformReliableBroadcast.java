package cs451.broadcast;

import cs451.callbacks.Callback;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;
    private final ConcurrentHashMap<Packet, Integer> ack = new ConcurrentHashMap<>();
    private final Set<Packet> delivered = ConcurrentHashMap.newKeySet();
    private final Set<Packet> pending = ConcurrentHashMap.newKeySet();

    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public UniformReliableBroadcast(Process process, int id, int numHosts, Callback callback) {
        super(callback, id, numHosts);
        broadcast = new BestEffortBroadcast(process, id, numHosts, this::deliver);

        worker.execute(this::processPending);
    }

    public void broadcast(Packet packet) {
        pending.add(packet);
        broadcast.broadcast(packet);
    }

    private void deliver(Packet packet) {
        ack.put(packet, ack.getOrDefault(packet, 0) + 1);

        if (pending.add(packet)) {
            broadcast.broadcast(packet);
        }
    }

    private boolean canDeliver(Packet packet) {
        return ack.getOrDefault(packet, 0) > getNumHosts() / 2;
    }

    private void processPending() {
        while (running.get()) {

            Iterator<Packet> value = pending.iterator();
            while (value.hasNext()) {

                Packet packet = value.next();
                if (canDeliver(packet) && delivered.add(packet)) {
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
