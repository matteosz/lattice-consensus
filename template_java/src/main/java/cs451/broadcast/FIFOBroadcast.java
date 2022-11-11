package cs451.broadcast;

import cs451.message.Message;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FIFOBroadcast extends Broadcast {

    private UniformReliableBroadcast broadcast;
    private final BlockingQueue<Packet> pending = new LinkedBlockingQueue<>();
    private final int[] next;
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public FIFOBroadcast(Process process, int id, int numHosts) {
        super(id, numHosts);
        broadcast = new UniformReliableBroadcast(process, id, numHosts, this::deliver);

        this.next = new int[numHosts];
        for (int i = 0; i < numHosts; i++) {
            next[i] = 1;
        }

        worker.execute(this::processPending);
    }

    private void broadcast(List<Message> messages, int packetNumber) {
        Packet packet = new Packet(messages, packetNumber, getMyId());
        broadcast.broadcast(packet);
    }

    private void deliver(Packet p) {
        try {
            pending.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void processPending() {
        while (running.get()) {
            try {
                Packet p = pending.take();

                if (p.getPacketId() == next[getMyId()-1]) {
                    next[getMyId()-1]++;
                    broadcast.getProcess().deliverEvent(p);
                } else {
                    pending.put(p);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void start(int numMessages) {
        List<Message> packet = new LinkedList<>();
        int packetNumber = 1;

        for (int m = 1; m <= numMessages; m++) {
            Message message = new Message(getMyId(), m);
            packet.add(message);
            broadcast.getProcess().sendEvent(message);

            if (packet.size() == Packet.MAX_COMPRESSION) {
                broadcast(packet, packetNumber++);
                packet.clear();
            }
        }

        if (packet.size() > 0) {
            broadcast(packet, packetNumber);
        }

    }

    public void stopThreads() {
        broadcast.stopThreads();
    }

}
