package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.parser.Host;
import cs451.process.Process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FairLossLink extends Link {

    private DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> datagramsToSend = new LinkedBlockingQueue<>();
    private final BlockingQueue<DatagramPacket> datagramsToReceive = new LinkedBlockingQueue<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);
    private AtomicBoolean running = new AtomicBoolean(true);

    public FairLossLink(Process process, Listener listener) {

        super(listener, process);

        try {
            socket = new DatagramSocket(process.getHost().getPort());
        } catch (SocketException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        workers.execute(this::sendPacketsInQueue);
        workers.execute(this::sendPacketsInReceiveQueue);
        workers.execute(this::receivePackets);
    }

    public void enqueuePacket(Packet pck, int target) {

        byte[] buffer = pck.getBytes();
        Host host = getProcess(target).getHost();

        try {
            datagramsToSend.put(new DatagramPacket(buffer, buffer.length, host.getIpAsAddress(), host.getPort()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void sendPacketsInQueue() {
        while (running.get()) {

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
         while (running.get()) {

            byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(datagramPacket);
                datagramsToReceive.put(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }
    }

    private void receivePackets() {
        while (running.get()) {

            try {
                DatagramPacket datagramPacket = datagramsToReceive.take();
                Packet packet = Packet.getPacket(datagramPacket.getData());
                handleListener(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }
    }

    public void stopThreads() {
        running.set(false);
        workers.shutdownNow();
    }

}

