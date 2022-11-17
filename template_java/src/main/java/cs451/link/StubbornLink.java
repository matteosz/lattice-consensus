package cs451.link;

import cs451.callbacks.PacketCallback;
import cs451.message.Packet;
import cs451.message.TimedPacket;
import cs451.parser.Host;
import cs451.process.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StubbornLink extends Link {

    private final FairLossLink link;
    private final ExecutorService worker;

    public StubbornLink(Host host, PacketCallback packetCallback) {
        super(packetCallback);
        link = new FairLossLink(host, this::deliver);

        worker = Executors.newFixedThreadPool(1);

        worker.execute(this::sendPackets);
    }

    private void deliver(Packet packet) {

        if (!packet.isAck()) {
            link.enqueuePacket(packet.convertToAck(link.getId()), packet.getLastSenderId());
            callback(packet);
        } else {
            getProcess(packet.getLastSenderId()).ack(packet);
        }

    }

    private void sendPackets() {

        for (;;) {

            for (Process process : getNetwork().values()) {

                for (TimedPacket timedPacket : process.nextPacketsToAck()) {

                    if (!process.hasAcked(timedPacket.getPacket())) {

                        if (timedPacket.timeoutExpired()) {

                            process.expBackOff();
                            timedPacket.update();

                            link.enqueuePacket(timedPacket.getPacket(), process.getId());

                        }

                        process.addPacketToAck(timedPacket);
                    }
                }

                if (!process.hasSpace()) {
                    continue;
                }

                Packet packet = process.getNextPacket();

                if (packet != null) {
                    process.addPacketToAck(new TimedPacket(process, packet));
                    link.enqueuePacket(packet, process.getId());
                }

            }

        }

    }

    public void stopThreads() {
        worker.shutdownNow();
        link.stopThreads();
    }

}
