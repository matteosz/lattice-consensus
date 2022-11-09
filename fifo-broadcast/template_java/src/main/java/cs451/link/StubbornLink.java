package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StubbornLink extends Link {

    private final FairLossLink link;
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private AtomicBoolean running = new AtomicBoolean(true);

    public StubbornLink(Process process, int port, Listener listener) {

        super(listener, process);
        link = new FairLossLink(process, port, this::deliver);

        worker.execute(this::sendPackets);
    }

    public void deliver(Packet pck) {

        if (!pck.isAck()) {
            link.enqueuePacket(pck.convertToAck());
        }

        handleListener(pck);
    }

    private void sendPackets() {

        while (running.get()) {
            Packet p = myProcess.getNextPacket();
            if (myProcess.removeAck(p)) {
                return;
            }

            link.enqueuePacket(p);
            myProcess.addSendPacket(p);
        }

    }

    public void stopThreads() {
        running.set(false);
        link.stopThreads();
        worker.shutdownNow();
    }

}
