package cs451.link;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.parser.Host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FairLossLink extends Link {

    private DatagramSocket socket;
    private final Host myHost;
    private final BlockingQueue<DatagramPacket> datagramsToSend = new LinkedBlockingQueue<>();
    private ExecutorService workers;

    public FairLossLink(Host myHost, PacketCallback packetCallback) {
        super(packetCallback);
        this.myHost = myHost;

        try {
            socket = new DatagramSocket(myHost.getPort());
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        workers = Executors.newFixedThreadPool(2);
        workers.execute(this::sendPacketsInQueue);
        workers.execute(this::receivePackets);
    }

    public Host getHost() {
        return myHost;
    }
    public int getId() {
        return myHost.getId();
    }

    public void enqueuePacket(Packet pck, int target) {

        byte[] buffer = pck.setLastSenderId(myHost.getId()).getBytes();
        Host host = getProcess(target).getHost();
        DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, host.getIpAsAddress(), host.getPort());

        try {
            datagramsToSend.put(dataPacket);
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

    private void receivePackets() {
         for (;;) {

            byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(datagramPacket);
                callback(new Packet(datagramPacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }

         }
    }

    public void stopThreads() {
        workers.shutdownNow();
    }

}

