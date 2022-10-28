package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

public class FairLossLink extends Link {

    private DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> datagramsToSend = new LinkedBlockingQueue<>();
    private final BlockingQueue<DatagramPacket> datagramsToReceive = new LinkedBlockingQueue<>();

    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    public FairLossLink(int id, int port, Listener listener) {

        super(listener, id);

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        workers.execute(this::sendPacketsInQueue);
        workers.execute(this::sendPacketsInReceiveQueue);
        workers.execute(this::receivePackets);
    }

    public void enqueuePacket(Packet pck, int id) {

        byte[] buffer = pck.getBytes();
        Host host = getProcess(id).getHost();

        try {
            datagramsToSend.put(new DatagramPacket(buffer, buffer.length, host.getIpAsAddress(), host.getPort()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void sendPacketsInQueue() {
        for (;;) {

            try {
                socket.send(datagramsToSend.take());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendPacketsInReceiveQueue() {
        for (;;) {

            byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(datagramPacket);
                datagramsToReceive.put(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void receivePackets() {
        for (;;) {

            try {
                DatagramPacket datagramPacket = datagramsToReceive.take();
                Packet packet = Packet.getPacket(datagramPacket.getData());
                handleListener(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void stopThreads() {
        workers.shutdownNow();
    }

}

