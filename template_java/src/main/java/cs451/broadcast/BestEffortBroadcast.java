package cs451.broadcast;

import cs451.callbacks.PacketCallback;
import cs451.link.PerfectLink;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;
    private final BlockingQueue<Packet> linkDelivered = new LinkedBlockingQueue<>(4096);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public BestEffortBroadcast(Host host, int numHosts, PacketCallback packetCallback) {
        super(packetCallback, host.getId(), numHosts);
        this.link = new PerfectLink(host, this::deliver);
    }

    private void deliver(Packet packet) {
        try {
            linkDelivered.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void startDelivering() {

        while (running.get()) {
            try {
                Packet packet = linkDelivered.take();
                callback(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

    }

    public void load(int numMessages) {

        for (int i = 1; i <= getNumHosts(); i++) {
            if (i != getMyId()) {
                link.load(numMessages, i);
            }
        }
        startDelivering();
    }

    public void broadcast(Packet packet) {
        for (int i = 1; i <= getNumHosts(); i++) {
            if (i == packet.getLastSenderId() && packet.getLastSenderId() != getMyId()) {
                continue;
            }
            if (i == getMyId()) {
                callback(packet);
            } else {
                link.send(packet, i);
            }
        }
    }

    public void stopThreads() {
        running.set(false);
        link.stopThreads();
    }
}
