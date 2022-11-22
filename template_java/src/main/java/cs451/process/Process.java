package cs451.process;

import cs451.message.Compressor;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cs451.utilities.Parameters.*;
import static cs451.message.Packet.MAX_COMPRESSION;

public class Process {

    private static byte myHost;
    private final Host host;
    private final AtomicInteger timeout;
    private int packetNumber;
    private byte next;

    private final Map<Byte, Compressor> toSend, messagesDelivered;
    private final PriorityBlockingQueue<TimedPacket> toAck;
    private final Compressor packetsAcked, packetsDelivered;
    private final Queue<Integer> lastTimes;

    public static byte getMyHost() {
        return myHost;
    }
    public static void setMyHost(byte id) {
        myHost = id;
    }

    public Process(Host host) {
        this.host = host;
        packetNumber = 0;
        next = myHost;

        lastTimes = new LinkedList<>();
        timeout = new AtomicInteger(BASE_TIMEOUT);
        toSend = new HashMap<>();
        toAck = new PriorityBlockingQueue<>();
        packetsAcked = new Compressor();
        packetsDelivered = new Compressor();
        messagesDelivered = new HashMap<>();

        for (byte i = 0; i >= 0 && i < NUM_HOSTS; i++) {
            toSend.put(i, new Compressor());
            messagesDelivered.put(i, new Compressor());
        }
        for (byte i = 0; i < TIMES; i++) {
            lastTimes.add(BASE_TIMEOUT);
        }
    }

    public Host getHost() {
        return host;
    }
    public byte getId() {
        return host.getId();
    }

    private void addTimeout(int lastTimeout) {
        synchronized (lastTimes) {
            lastTimes.poll();
            lastTimes.add(lastTimeout);
            long average = 0;
            for (int l : lastTimes) {
                average += l;
            }
            average /= lastTimes.size();
            timeout.set((int) average + THRESHOLD);
        }
    }

    public int getTimeout() {
        return timeout.get();
    }
    public void expBackOff() {
        addTimeout(Math.min(2 * timeout.get(), MAX_TIMEOUT));
    }
    public void notify(int lastTime) {
        addTimeout(lastTime);
    }

    public void load(int numMessages) {
        toSend.get(myHost).setHead(1, numMessages);
    }
    public void addMessage(Message message) {
        toSend.get(message.getOrigin()).add(message.getPayload());
    }

    public boolean hasSpace() {
        return toAck.size() < LINK_BATCH;
    }

    private Message loadMessage() {
        // Round-robin starting from current local host
        for (byte h = 0; h >= 0 && h < NUM_HOSTS; h++) {
            byte curr = next;
            if (next == NUM_HOSTS - 1) {
                next = 0;
            } else {
                next++;
            }

            int messageId = toSend.get(curr).takeFirst();
            if (messageId != -1) {
                return new Message(curr, messageId);
            }
        }

        return null;
    }
    public Packet getNextPacket() {

        List<Message> messages = new LinkedList<>();
        byte count = 0;
        while (count < MAX_ATTEMPTS && messages.size() < MAX_COMPRESSION) {
            Message message = loadMessage();
            if (message == null) {
                count++;
            } else {
                messages.add(message);
            }
        }
        if (!messages.isEmpty()) {
            return new Packet(messages, ++packetNumber, myHost);
        }
        return null;
    }

    public List<TimedPacket> nextPacketsToAck() {
        List<TimedPacket> packets = new LinkedList<>();
        toAck.drainTo(packets);
        return packets;
    }
    public void addPacketToAck(TimedPacket packet) {
        toAck.put(packet);
    }

    public boolean hasAcked(int id) {
        return packetsAcked.contains(id);
    }

    public boolean deliverPacket(Packet packet) {
        if (!packet.isAck()) {
            return packetsDelivered.add(packet.getPacketId());
        } else {
            return packetsAcked.add(packet.getPacketId());
        }
    }

    public boolean deliver(Message message) {
        return messagesDelivered.get(message.getOrigin()).add(message.getPayload());
    }

    public Map<Byte, Compressor> getDelivered() {
        return messagesDelivered;
    }

}
