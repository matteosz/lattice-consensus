package cs451.link;

import cs451.interfaces.Listener;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StubbornLink extends Link {

    private final int BATCH = 10;
    private final FairLossLink link;
    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private final int sleep;
    private boolean running;

    public StubbornLink(int id, int port, Listener listener, int numHosts) {

        super(listener, id);
        link = new FairLossLink(id, port, this::deliver);

        if (numHosts > 2 * BATCH) {
            sleep = 6;
        } else if (numHosts > BATCH){
            sleep = 5;
        } else if (numHosts > BATCH/2) {
            sleep = 4;
        } else if (numHosts > 2) {
            sleep = 2;
        } else {
            sleep = 1;
        }

        worker.execute(this::sendPackets);
        running = true;
    }

    public void deliver(Packet pck) {

        if (!pck.isAck()) {
            link.enqueuePacket(pck.convertToAck(getId()), pck.getSenderId());
        }

        handleListener(pck);
    }

    private void sendPackets() {

        while (running) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            processPacket(getProcess(getId()));
        }

    }

    private void processPacket(Process process) {

        List<Packet> packets = process.getPacketsToSend();

        for (Packet p : packets) {
            link.enqueuePacket(p, Link.targetId);
        }

    }

    public void stopThreads() {
        running = false;
        link.stopThreads();
        worker.shutdownNow();
    }

}
