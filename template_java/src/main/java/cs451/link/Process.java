package cs451.link;

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
    private final int numHosts;
    private final Map<Integer, Set<Integer>> delivered = new HashMap<>();
    private final Map<Integer, Packet> toSend = new HashMap<>();
    private final Set<>
    private final AtomicInteger packetNumber = new AtomicInteger(0);
    private final boolean isTarget;
    private final List<Event> events = new LinkedList<>();

    public Process(Host host, int numHosts, boolean isTarget) {
        this.host = host;
        this.numHosts = numHosts;
        this.isTarget = isTarget;

        for (int i = 0; i < numHosts; i++) {
            delivered.put(i+1, new HashSet<>());
        }
    }

    public Host getHost() {
        return host;
    }

    public int getNumHosts() {
        return numHosts;
    }
    /*
    public void addMessageToProcess(Message mex) {
        try {
            toProcess.get(mex.getSenderId()).put(mex);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<Packet> getWaitingPackets() {
        List<Packet> tmp = new LinkedList<>();
        toAck.drainTo(tmp);
        return tmp;
    }

    public boolean alreadyReceived(Message m) {
        return delivered.contains(m);
    }

    public void addPacketToConfirm(Packet p) {
        try {
            toAck.put(p);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Message getWaitingMessage() {
        for (int i = 0; i < numHosts; i++) {
            Message m = toProcess.get(i+1).poll();
            if (m != null) {
                return m;
            }
        }
        return null;
    }
    */
    public int getNextPacketId() {
        return packetNumber.incrementAndGet();
    }

    public void sendMany(int sourceId, int numMessages) {
    }

    public boolean isTarget() {
        return isTarget;
    }

    public boolean hasDelivered(Packet p) {

    }
    public boolean hasSent(Packet p) {

    }

    public void flagEvent(Packet p, boolean deliver) {

        // attention to not flag a duplicate events, check before

        if (deliver) {
            //mark as delivered all messages in the packet
        } else {
            //mark as sent all messages in the packet
        }
    }

    public List<Packet> getPacketsToSend() {
        synchronized (toSend) {
            return toSend.entrySet().stream()
                    .sorted(Comparator.comparing(x -> x.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }
    }

    public void stopSending(Packet p) {
        synchronized (toSend) {
            toSend.remove(p.getPacketId());
        }
    }

    public String logAllEvents() {
        StringBuilder sb = new StringBuilder();
        events.forEach(x -> sb.append(x.toString()));
        return sb.toString();
    }
}
