package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FairLossLink extends Link {

    private DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> packetsToSend = new LinkedBlockingQueue<>();

    public FairLossLink(int id, List<Host> hosts, int port, Listener listener) {
        super(listener, id, hosts);

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Executor threads = Executors.newFixedThreadPool(2);
        threads.execute(this::sendPacketsInQueue);
        threads.execute(this::receive);
    }

    public void enqueuePacket(Packet pck, int id) {
        byte[] buffer = pck.getBytes();
        Host host = getProcess(id).getHost();
        try {
            packetsToSend.put(new DatagramPacket(buffer, buffer.length, host.getIpAsAddress(), host.getPort()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendPacketsInQueue() {
        for (;;) {
            try {
                DatagramPacket datagramPacket = packetsToSend.take();
                socket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void receive() {
        for (;;) {
            byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Packet packet = Packet.getPacket(datagramPacket.getData());
            handleListener(packet);
        }
    }

    public void closeSocket() {
        socket.close();
    }
}

