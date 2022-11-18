package cs451.process;

import cs451.message.Message;
import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cs451.message.Packet.MAX_COMPRESSION;

public class Process {

    private static final int MAX_PROCESSES = 128;
    private static final int BASE_TIMEOUT = 512;
    private static final int MAX_TIMEOUT = 16384;
    private static final int BATCH = 4096;
    private static byte myHost;
    private static final Random random = new Random();

    private final Host host;
    private final AtomicInteger timeout = new AtomicInteger(BASE_TIMEOUT);
    private final int batchSize, numHosts;
    private int totalMessages, sentMessages = 0, packetNumber = 0, next;

    private final Map<Byte, ConcurrentSkipListSet<Packet>> toSend = new HashMap<>();
    private final Map<Byte, Set<Integer>> acked = new HashMap<>();
    private final Map<Byte, Set<Integer>> delivered = new HashMap<>();
    private final BlockingQueue<TimedPacket> toAck = new LinkedBlockingQueue<>();

    public static int getMyHost() {
        return myHost + 1;
    }
    public static void setMyHost(int id) {
        myHost = (byte) (id - 1);
    }

    public Process(Host host, int numHosts) {
        this.host = host;
        this.numHosts = numHosts;
        this.batchSize = numHosts <= 1 ? BATCH : BATCH / numHosts;
        this.next = myHost + 1;

        for (byte i = 0; i < numHosts; i++) {
            toSend.put(i, new ConcurrentSkipListSet<>(Comparator.comparingInt(Packet::getPacketId)));
            acked.put(i, ConcurrentHashMap.newKeySet());
            delivered.put(i, ConcurrentHashMap.newKeySet());
        }
    }

    public Host getHost() {
        return host;
    }
    public int getId() {
        return host.getId();
    }

    public int getTimeout() {
        return timeout.get();
    }
    public void expBackOff() {
        timeout.set(Math.min(2 * timeout.get(), MAX_TIMEOUT) + random.nextInt(BASE_TIMEOUT / 2));
    }
    public void notify(int lastTime) {
        timeout.set((lastTime + BASE_TIMEOUT) / 2 + random.nextInt(BASE_TIMEOUT / 2));
    }

    public void load(int numMessages) {
        this.totalMessages = numMessages;
    }
    public boolean hasSpace() {
        return toAck.size() < batchSize;
    }

    public Packet getNextPacket() {

        for (int h = 0; h < numHosts; h++) {
            byte curr = (byte) (next - 1);
            next = Math.max(1, (next + 1) % (numHosts + 1));

            if (curr == myHost) {
                loadLocalMessages();
            }

            if (!toSend.get(curr).isEmpty()) {
                return toSend.get(curr).pollFirst();
            }
        }

        return null;
    }
    private void loadLocalMessages() {

        List<Message> messages = new LinkedList<>();
        while (sentMessages < totalMessages && messages.size() < MAX_COMPRESSION) {
            messages.add(new Message(myHost, ++sentMessages));
        }
        if (!messages.isEmpty()) {
            toSend.get(myHost).add(new Packet(messages, ++packetNumber, myHost, myHost));
        }
    }

    public List<TimedPacket> nextPacketsToAck() {
        List<TimedPacket> packets = new LinkedList<>();
        toAck.drainTo(packets);
        return packets;
    }
    public void addPacketToAck(TimedPacket packet) {
        try {
            toAck.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public boolean ack(Packet packet) {
        if (acked.get(packet.getBOriginId()).contains(packet.getPacketId())) {
            return false;
        }
        return acked.get(packet.getBOriginId()).add(packet.getPacketId());
    }
    public boolean hasAcked(Packet packet) {
        return acked.get(packet.getBOriginId()).contains(packet.getPacketId());
    }

    public void addPacket(Packet packet) {
        toSend.get(packet.getBOriginId()).add(packet);
    }

    public boolean hasDelivered(Packet packet) {
        return delivered.get(packet.getBOriginId()).contains(packet.getPacketId());
    }

    public void deliver(Packet packet) {
        delivered.get(packet.getBOriginId()).add(packet.getPacketId());
    }

    public Map<Byte, Set<Integer>> getDelivered() {
        return delivered;
    }

}
