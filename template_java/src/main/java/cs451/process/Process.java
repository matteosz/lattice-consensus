package cs451.process;

import cs451.message.Compressor;
import cs451.message.Packet;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.message.Packet.MEX_OS;
import static cs451.utilities.Parameters.*;
import static cs451.message.Packet.MAX_COMPRESSION;

public class Process {
    
    private static byte myHost;
    private final Host host;
    private final AtomicInteger timeout;
    private final int numHosts;
    private int packetNumber;

    private final TreeSet<Proposal> toSend;
    private final Set<Proposal> proposalsDelivered;
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

        timeout = new AtomicInteger(TIMEOUT);
        toSend = new TreeSet<>();
        toAck = new LinkedList<>();
        packetsAcked = new Compressor();
        packetsDelivered = new Compressor();
        proposalsDelivered = new HashSet<>();
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

    public void load(List<Proposal> proposals) {
        synchronized (toSend) {
            for (Proposal proposal : proposals) {
                toSend.add(Proposal.createProposal(proposal));
            }
        }
    }
    public void addProposal(Proposal proposal) {
        synchronized (toSend) {
            toSend.add(proposal);
        }
    }

    public boolean hasSpace() {
        return toAck.size() < LINK_BATCH;
    }

    private Proposal loadProposal(int budget) {
        synchronized (toSend) {
            for (byte h = 0; h >= 0 && h < numHosts; h++) {
                Proposal proposal = toSend.pollFirst();
                if (proposal != null) {
                    if (proposal.getBytes() <= budget) {
                        return proposal;
                    } else {
                        toSend.add(proposal);
                    }
                }
            }
        }
        return null;
    }
    public Packet getNextPacket() {

        List<Proposal> proposals = new LinkedList<>();
        byte count = 0;
        int len = MEX_OS;
        while (count < EMPTY_CYCLES && proposals.size() < MAX_COMPRESSION) {
            Proposal proposal = loadProposal(MAX_PACKET_SIZE - len);
            if (proposal == null) {
                count++;
            } else {
                proposals.add(proposal);
                len += proposal.getBytes();
            }
        }
        if (!proposals.isEmpty()) {
            return new Packet(proposals, ++packetNumber, myHost, len);
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

    public boolean deliver(Proposal proposal) {
        return proposalsDelivered.add(proposal);
    }

}
