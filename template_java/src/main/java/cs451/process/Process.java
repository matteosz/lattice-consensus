package cs451.process;

import cs451.helper.Pair;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Process {

    private final Host host;

    private final Set<Packet> delivered = new HashSet<>();
    private final BlockingQueue<Pair> toSend = new LinkedBlockingQueue<>();
    private final Set<Pair> ack = ConcurrentHashMap.newKeySet();

    private final StringBuilder events = new StringBuilder();

    public Process(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public boolean deliver(Packet p) {
        return delivered.add(p);
    }

    public void sendEvent(Message m) {
        synchronized (events) {
            events.append("b " + m.getMessageId() + "\n");
        }
    }

    public void deliverEvent(Packet p) {
        p.getMessages().forEach(this::deliverEvent);
    }

    public void deliverEvent(Message m) {
        synchronized (events) {
            events.append("d " + m.getMessageId() + " " + m.getSenderId() + "\n");
        }
    }

    public Pair getNextPacket() {
        Pair p = null;
        try {
           p = toSend.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return p;
    }

    public void addSendPacket(Packet p, int target) {
        try {
            toSend.put(new Pair(p, target));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void addResendPacket(Pair p) {
        try {
            toSend.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void ack(Pair p) {
        ack.add(p);
    }

    public boolean removeAck(Pair p) {
        return ack.remove(p);
    }

    public String logAllEvents() {
        synchronized (events) {
            return events.toString();
        }
    }
}
