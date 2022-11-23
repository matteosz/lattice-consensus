package cs451.channel;

import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.process.Process;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.process.Process.getMyHost;

public class StubbornLink extends Link {

    private final FairLossLink link;
    private final ExecutorService worker;
    private final AtomicBoolean running;

    public StubbornLink(int port, Consumer<Packet> packetCallback) throws SocketException {
        super(packetCallback);

        link = new FairLossLink(port, this::stubbornDeliver);

        running = new AtomicBoolean(true);
        worker = Executors.newFixedThreadPool(1);
        worker.execute(this::sendPackets);
    }

    private void stubbornDeliver(Packet packet) {

        if (packet.isAck()) {

            getProcess(packet.getSenderId()).notify(packet.getEmissionTime());

        } else if (link != null){

            link.enqueuePacket(packet.convertToAck(getMyHost()), packet.getSenderId());

        }
        callback(packet);
    }

    private void sendPackets() {

        while (running.get()) {

            for (Process process : getNetwork().values()) {
                TimedPacket timedPacket = process.nextPacketToAck();
                if (timedPacket != null && !process.hasAcked(timedPacket.getPacket().getPacketId())) {

                    if (timedPacket.timeoutExpired()) {

                        process.expBackOff();
                        timedPacket.update(process.getTimeout());

                        link.enqueuePacket(timedPacket.getPacket(), process.getId());
                    }
                    process.addPacketToAck(timedPacket);
                }

                if (!process.hasSpace()) {
                    continue;
                }
                Packet packet = process.getNextPacket();
                if (packet != null) {
                    process.addPacketToAck(new TimedPacket(process.getTimeout(), packet));
                    link.enqueuePacket(packet, process.getId());
                }
            }
        }

    }

    public void stopThreads() {
        running.set(false);
        worker.shutdownNow();
        link.stopThreads();
    }

}
