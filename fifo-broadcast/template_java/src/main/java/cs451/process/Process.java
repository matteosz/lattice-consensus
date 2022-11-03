package cs451.process;

import cs451.helper.Event;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Process {

    private final Host host;
    private final boolean isTarget;
    private int packetNumber = 0;

    private final HashMap<Integer, Set<Integer>> delivered = new HashMap<>();
    private final List<Event> events = new LinkedList<>();
    private final ConcurrentHashMap<Integer, Packet> toSend = new ConcurrentHashMap<>();

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

        deliverEvent(p, p.getSenderId());
    }

    public boolean hasDelivered(Packet p) {
        Set<Integer> x = delivered.get(p.getSenderId());
        if (x == null)
            return false;

        return x.contains(p.getPacketId());
    }

    public void sendEvent(Message m) {
        events.add(new Event('b', m.getMessageId()));
    }

    private void deliverEvent(Packet p, int id) {
        p.getMessages().forEach(m -> events.add(new Event('d', m.getMessageId(), id)));
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

        synchronized (events) {
            events.forEach(e -> sb.append(e.toString()));
        }

        return sb.toString();
    }

    public void load(List<Message> messages) {
        packetNumber++;
        toSend.put(packetNumber, Packet.createPacket(messages,packetNumber, host.getId()));
    }
}
