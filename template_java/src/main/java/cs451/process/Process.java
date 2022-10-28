package cs451.process;

import cs451.helper.Event;
import cs451.message.Compressor;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Process {

    private final Host host;
    private final boolean isTarget;
    private final AtomicInteger packetNumber = new AtomicInteger(0);

    private final Map<Integer, Set<Integer>> delivered = new TreeMap<>();
    private final Set<Integer> sent = new TreeSet<>();
    private final Map<Integer, Packet> toSend = new TreeMap<>();

    private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();

    public Process(Host host, int numHosts, int targetId) {
        this.host = host;
        this.isTarget = targetId == host.getId();

        for (int i = 1; i <= numHosts; i++) {
            delivered.put(i, new HashSet<>());
        }
    }

    public Host getHost() {
        return host;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void deliver(Packet p) {
        delivered.get(p.getSenderId()).add(p.getPacketId());
    }

    public boolean hasDelivered(Packet p) {

        Set<Integer> x = delivered.get(p.getSenderId());

        if (x == null)
            return false;

        return x.contains(p.getPacketId());

    }

    public void flagEvent(Packet p, int id, boolean deliver) {

        if (deliver) {

            p.getMessages().forEach(m -> {
                try {
                    events.put(new Event('d', m.getMessageId(), id));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });

        } else {

            synchronized (sent) {
                if (!sent.add(p.getPacketId())) {
                    return;
                }
            }

            p.getMessages().forEach(m -> {
                try {
                    events.put(new Event('b', m.getMessageId()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public List<Packet> getPacketsToSend() {

        return toSend.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

    }

    public boolean isSending(Packet p) {
        return toSend.containsKey(p.getPacketId());
    }

    public void stopSending(Packet p) {
        toSend.remove(p.getPacketId());
    }

    public String logAllEvents() {

        StringBuilder sb = new StringBuilder();

        events.forEach(e -> sb.append(e.toString()));

        return sb.toString();
    }

    public void run(int numMessages) {

        if (isTarget) {
            return;
        }

        List<List<Message>> packets = Compressor.compress(numMessages, host.getId());
        
        packets.forEach(x ->
                    toSend.put(packetNumber.incrementAndGet(),
                            Packet.createPacket(x,packetNumber.get(), host.getId())));
        }

}
