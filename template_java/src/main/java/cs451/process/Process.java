package cs451.process;

import cs451.message.Compressor;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cs451.utilities.Utilities.fromByteToInteger;
import static cs451.utilities.Utilities.fromIntegerToByte;
import static cs451.message.Packet.MAX_COMPRESSION;

public class Process {

    private static final int BASE_TIMEOUT = 256;
    private static final int MAX_TIMEOUT = 16384;
    private static final byte MAX_ATTEMPTS = 2;
    private static final int BATCH = 4096;
    private static byte myHost;
    private static final Random random = new Random();

    private final Host host;
    private final AtomicInteger timeout;
    private int numHosts, batch, packetNumber, next;

    private final Map<Byte, Compressor> toSend, messagesDelivered;
    private final BlockingQueue<TimedPacket> toAck;
    private final Compressor packetsAcked, packetsDelivered;

    public static byte getMyHost() {
        return myHost;
    }
    public static void setMyHost(byte id) {
        myHost = id;
    }

    public Process(Host host, int numHosts) {
        this.host = host;
        this.numHosts = numHosts;
        batch = numHosts <= 1 ? BATCH : BATCH / numHosts;
        packetNumber = 0;
        next = fromByteToInteger(myHost);

        timeout = new AtomicInteger(BASE_TIMEOUT);
        toSend = new HashMap<>();
        toAck = new LinkedBlockingQueue<>();
        packetsAcked = new Compressor();
        packetsDelivered = new Compressor();
        messagesDelivered = new HashMap<>();

        for (byte i = 0; i >= 0 && i < numHosts; i++) {
            toSend.put(i, new Compressor());
            messagesDelivered.put(i, new Compressor());
        }
    }

    public Host getHost() {
        return host;
    }
    public byte getId() {
        return host.getId();
    }

    public int getTimeout() {
        return timeout.get();
    }
    public void expBackOff() {
        timeout.set(Math.min(2 * timeout.get(), MAX_TIMEOUT) + random.nextInt(BASE_TIMEOUT / 2));
    }
    public void notify(int lastTime) {
        timeout.set(lastTime + random.nextInt(BASE_TIMEOUT / 2));
    }

    public void load(int numMessages) {
        toSend.get(myHost).setHead(1, numMessages);
    }
    public void addMessage(Message message) {
        toSend.get(message.getOrigin()).add(message.getPayload());
    }

    public boolean hasSpace() {
        return toAck.size() < batch;
    }

    private Message loadMessage() {

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            byte curr = fromIntegerToByte(next);
            next = Math.max(1, (next + 1) % (numHosts + 1));

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
        try {
            toAck.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public boolean hasAcked(int id) {
        return packetsAcked.contains(id);
    }

    public boolean hasDelivered(int id) {
        return packetsDelivered.contains(id);
    }

    public void deliverPacket(Packet packet) {
        if (!packet.isAck()) {
            packetsDelivered.add(packet.getPacketId());
        } else {
            packetsAcked.add(packet.getPacketId());
        }
    }

    public boolean deliver(Message message) {
        return messagesDelivered.get(message.getOrigin()).add(message.getPayload());
    }

    public Map<Byte, Compressor> getDelivered() {
        return messagesDelivered;
    }

}
