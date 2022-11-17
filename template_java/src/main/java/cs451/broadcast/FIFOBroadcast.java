package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FIFOBroadcast extends Broadcast {

    private UniformReliableBroadcast broadcast;

    private final BlockingQueue<Packet> pending = new LinkedBlockingQueue<>();
    private final int[] next;
    private PacketCallback broadcastCallback;

    private final ExecutorService worker = Executors.newFixedThreadPool(1);

    public FIFOBroadcast(Host host, int numHosts, PacketCallback broadcastCallback, PacketCallback deliverCallback) {
        super(deliverCallback, host.getId(), numHosts);
        this.broadcastCallback = broadcastCallback;

        this.broadcast = new UniformReliableBroadcast(host, numHosts, this::deliver);

        this.next = new int[numHosts + 1];
        for (int i = 1; i <= numHosts; i++) {
            next[i] = 1;
        }

        worker.execute(this::processPending);
    }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    private void deliver(Packet packet) {
        try {
            pending.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void processPending() {
        for (;;) {
            try {
                Packet p = pending.take();

                if (p.getPacketId() == next[getMyId()]) {
                    next[getMyId()]++;
                    callback(p);
                } else {
                    pending.put(p);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopThreads() {
        worker.shutdownNow();
        broadcast.stopThreads();
    }

}
