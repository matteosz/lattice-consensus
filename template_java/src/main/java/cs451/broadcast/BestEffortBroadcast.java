package cs451.broadcast;

import cs451.channel.PerfectLink;
import cs451.message.Message;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.process.Process.getMyHost;
import static cs451.utilities.Parameters.BROADCAST_BATCH;
import static cs451.utilities.Parameters.NUM_HOSTS;

public class BestEffortBroadcast extends Broadcast {

    private final PerfectLink link;
    private final BlockingQueue<Message> linkDelivered;
    private final AtomicBoolean running;

    public BestEffortBroadcast(int port, Consumer<Message> packetCallback) throws SocketException {
        super(packetCallback);

        this.running = new AtomicBoolean(true);
        this.linkDelivered = new LinkedBlockingQueue<>(BROADCAST_BATCH);

        this.link = new PerfectLink(port, this::bebDeliver);
    }

    private void bebDeliver(Message message) {
        try {
            linkDelivered.put(message);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void load(int numMessages) {

        for (byte h = 0; h >= 0 && h < NUM_HOSTS; h++) {
            if (h != getMyHost()) {
                link.load(numMessages, h);
            }
        }

        while (running.get()) {
            try {
                callback(linkDelivered.take());
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void bebBroadcast(Message message) {

        for (byte h = 0; h >= 0 && h < NUM_HOSTS; h++) {
            if (h == message.getSender() && h != getMyHost()) {
                continue;
            }
            if (h == getMyHost()) {
                callback(message);
            } else {
                link.send(message, h);
            }
        }
    }

    public void stopThreads() {
        running.set(false);
        link.stopThreads();
    }
}
