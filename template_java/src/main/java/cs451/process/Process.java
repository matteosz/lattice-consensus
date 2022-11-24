package cs451.process;

import cs451.message.Compressor;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cs451.utilities.Parameters.*;
import static cs451.utilities.Utilities.fromByteToInteger;
import static cs451.utilities.Utilities.fromIntegerToByte;
import static cs451.message.Packet.MAX_COMPRESSION;

public class Process {

    private static byte myHost;
    private final Host host;
    private final AtomicInteger timeout;
    private final int numHosts;
    private int packetNumber, next;

    private final Map<Byte, Compressor> toSend, messagesDelivered;
    private final Queue<TimedPacket> toAck;
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
        packetNumber = 0;
        next = fromByteToInteger(myHost);

        timeout = new AtomicInteger(TIMEOUT);
        toSend = new HashMap<>();
        toAck = new LinkedList<>();
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
        timeout.set(Math.min(2 * timeout.get(), MAX_TIMEOUT) + THRESHOLD);
    }
    public void notify(int lastTime) {
        timeout.set(lastTime + THRESHOLD);
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

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            byte curr = fromIntegerToByte(next);
            next = next == numHosts? 1 : next + 1;
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
        while (count < EMPTY_CYCLES && messages.size() < MAX_COMPRESSION) {
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

    public TimedPacket nextPacketToAck() {
        return toAck.poll();
    }
    public void addPacketToAck(TimedPacket packet) {
        toAck.add(packet);
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
