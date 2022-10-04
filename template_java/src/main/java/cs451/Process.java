package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static cs451.Constants.MAX_COMPRESSION;
import static cs451.Constants.PACKET_SIZE;

public class Process {

    private Host sender;
    private final Host receiver;
    private List<Message> messages;
    private final List<Event> events;
    private int m, nProcess;
    private final DatagramSocket socket;
    private byte[] buffer = new byte[PACKET_SIZE];

    public Process(Host sender, Host receiver, int m) throws SocketException {
        this.sender = sender;
        this.receiver = receiver;
        this.m = m;
        events = new ArrayList<>(m);
        socket = new DatagramSocket();
    }

    public Process(Host receiver, int m, int n) throws SocketException {
        this.receiver = receiver;
        this.m = m;
        nProcess = n;
        events = new ArrayList<>(m*n);
        socket = new DatagramSocket();
    }

    private void send() throws IOException { // Send a packet
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(receiver.getIp()), receiver.getPort());
        socket.send(packet);
    }

    /*private void listen(byte[] buffer) { // Receive a packet
        DatagramPacket packet = new DatagramPacket()
    }*/

    public void sendAll() throws IOException { // Send all packets
        int processed = 1;
        while (processed <= m) {
            List<Message> toSend = Message.getIntList(processed, Math.min(processed + MAX_COMPRESSION - 1, m));
            buffer = Message.getIntBytes(toSend);
            send();
            processed += MAX_COMPRESSION;
        }
    }

    public void listenAll() throws IOException { // Receive all packets
        boolean running = true;
        HashSet<Message> delivered = new HashSet<>(m);
        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            Message x = new Message(packet.getData());
            InetAddress srcAddress = packet.getAddress();
            delivered.add(x);

            if (delivered.size() == m)
                running = false;
        }
    }

    public Host getSender() {
        return sender;
    }

    public Host getReceiver() {
        return receiver;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<Event> getEvents() {
        return events;
    }
}
