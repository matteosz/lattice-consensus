package cs451.process;

import cs451.helper.GenericPair;
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
    private final BlockingQueue<GenericPair<Packet, Byte>> toSend = new LinkedBlockingQueue<>();
    private final Set<GenericPair<Packet, Byte>> ack = ConcurrentHashMap.newKeySet();

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
            events.append("d " + m.getSenderId() + " " + m.getMessageId() + "\n");
        }
    }

    public GenericPair<Packet, Byte> getNextPacket() {
        GenericPair<Packet, Byte> p = null;
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
            toSend.put(new GenericPair<Packet, Byte>(p, (byte) target));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void addResendPacket(GenericPair<Packet, Byte> p) {
        try {
            toSend.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void ack(GenericPair<Packet, Byte> p) {
        ack.add(p);
    }

    public boolean removeAck(GenericPair<Packet, Byte> p) {
        return ack.remove(p);
    }

    public boolean hasAcked(GenericPair<Packet, Byte> p) {
        return ack.contains(p);
    }

    public String logAllEvents() {
        synchronized (events) {
            return events.toString();
        }
    }
}
