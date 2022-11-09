package cs451.process;

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
    private final BlockingQueue<Packet> toSend = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Packet, Byte> ack = new ConcurrentHashMap<>();
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

    public Packet getNextPacket() {
        Packet p = null;
        try {
           p = toSend.take();
        } catch (InterruptedException e) {
            // e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return p;
    }

    public void addSendPacket(Packet p) {
        try {
            toSend.put(p);
        } catch (InterruptedException e) {
            // e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void ack(Packet p) {
        ack.put(p, null);
    }

    public boolean removeAck(Packet p) {
        if (!ack.containsKey(p))
            return false;

        ack.remove(p);
        return true;
    }

    public String logAllEvents() {
        synchronized (events) {
            return events.toString();
        }
    }
}
