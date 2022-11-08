package cs451.process;

import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Process {

    private final Host host;

    private final HashMap<Integer, Set<Integer>> delivered = new HashMap<>();
    private final StringBuilder events = new StringBuilder();
    private final BlockingQueue<Packet> toSend = new LinkedBlockingQueue<>();
    private final Set<Packet> acks = new HashSet<>();

    public Process(Host host, int numHosts) {
        this.host = host;

        for (int i = 1; i <= numHosts; i++) {
            delivered.put(i, new HashSet<>());
        }
    }

    public Host getHost() {
        return host;
    }

    public void deliver(Packet p) {

        delivered.get(p.getSenderId()).add(p.getPacketId());

        deliverEvent(p, p.getSenderId());
    }

    public boolean hasDelivered(Packet p) {
        Set<Integer> x = delivered.get(p.getSenderId());
        if (x == null)
            return false;

        return x.contains(p.getPacketId());
    }

    public void sendEvent(Message m) {
        synchronized (events) {
            events.append("b " + m.getMessageId() + "\n");
        }
    }

    private void deliverEvent(Packet p, int id) {
        synchronized (events) {
            p.getMessages().forEach(m -> events.append("d " + m.getMessageId() + " " + id + "\n"));
        }
    }

    public BlockingQueue<Packet> getPacketsToSend() {
        return toSend;
    }

    public Packet getNextPacket() {
        Packet p = null;
        try {
           p = toSend.take();
        } catch (InterruptedException e) {
            /* e.printStackTrace();
            Thread.currentThread().interrupt(); */
        }
        return p;
    }

    public void addResendPacket(Packet p) {
        try {
            toSend.put(p);
        } catch (InterruptedException e) {
            /* e.printStackTrace();
            Thread.currentThread().interrupt(); */
        }
    }

    public boolean hasAcked(Packet p) {
        return acks.contains(p);
    }

    public void stopSending(Packet p) {
        toSend.remove(p.getPacketId());
    }

    public String logAllEvents() {
        synchronized (events) {
            return events.toString();
        }
    }

    public void load(int packetNumber, List<Message> messages, int targetId) {

        try {
            toSend.put(Packet.createPacket(messages, packetNumber, host.getId(), targetId));
        } catch (InterruptedException e) {
            //e.printStackTrace();
            //Thread.currentThread().interrupt();
        }
    }
}
