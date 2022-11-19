package cs451.channel;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.process.Process.getMyHost;

public class FairLossLink extends Link {

    private final DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> datagramsToSend;
    private final ExecutorService workers;
    private final AtomicBoolean running;

    public FairLossLink(int port, Consumer<Packet> packetCallback) throws SocketException {
        super(packetCallback);

        socket = new DatagramSocket(port);

        datagramsToSend = new LinkedBlockingQueue<>();
        running = new AtomicBoolean(true);

        workers = Executors.newFixedThreadPool(2);
        workers.execute(this::dequeuePacket);
        workers.execute(this::receivePackets);
    }

    public void enqueuePacket(Packet packet, byte target) {

        packet.setLastSenderId(getMyHost());
        Host targetHost = getProcess(target).getHost();
        byte[] buffer = packet.getBytes();
        DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, targetHost.getIpAsAddress(), targetHost.getPort());

        try {
            datagramsToSend.put(dataPacket);
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void dequeuePacket() {
        while (running.get()) {

            try {
                socket.send(datagramsToSend.take());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void receivePackets() {
         while (running.get()) {

            byte[] buffer = new byte[MAX_PACKET_SIZE];
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
        running.set(false);
        workers.shutdownNow();
    }

}

