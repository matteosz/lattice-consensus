package cs451.broadcast;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;
    private final ConcurrentHashMap<Packet, Set<Integer>> ack = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Packet, Objects> delivered = new ConcurrentHashMap<>();
    private final BlockingQueue<Packet> pending = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public UniformReliableBroadcast(Process process, int port, int id, int numHosts, Listener listener) {
        super(listener, id, numHosts);
        broadcast = new BestEffortBroadcast(process, port, id, numHosts, this::deliver);
        worker.execute(this::processPending);
    }

    public void broadcast(Packet packet) {
        try {
            pending.put(packet);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        broadcast.broadcast(packet);
    }

    private void deliver(Packet packet) {
        ack.computeIfAbsent(packet, k -> new HashSet<>());
        ack.get(packet).add(getMyId());

        if (pending.stream().filter(p -> p.equals(packet)).findAny().isEmpty()) {
            try {
                pending.put(packet);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            broadcast.broadcast(packet);
        }
    }

    private boolean canDeliver(Packet packet) {
        return ack.getOrDefault(packet, new HashSet<>()).size() > getNumHosts() / 2;
    }

    private void processPending() {
        while (running.get()) {
            try {
                Packet packet = pending.take();
                if (canDeliver(packet) && !delivered.containsKey(packet)) {
                    delivered.put(packet, null);
                    handleListener(packet);
                } else {
                    pending.put(packet);
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Thread.currentThread().interrupt();
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
