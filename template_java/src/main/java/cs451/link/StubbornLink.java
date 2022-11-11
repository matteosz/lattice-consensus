package cs451.link;

import cs451.helper.Pair;
import cs451.callbacks.Callback;
import cs451.message.Packet;
import cs451.process.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StubbornLink extends Link {

    private final FairLossLink link;

    private final ExecutorService worker = Executors.newFixedThreadPool(1);
    private AtomicBoolean running = new AtomicBoolean(true);

    public StubbornLink(Process process, Callback callback) {
        super(callback, process);
        link = new FairLossLink(process, this::deliver);

        worker.execute(this::sendPackets);
    }

    private void deliver(Packet pck) {

        if (!pck.isAck()) {
            link.enqueuePacket(pck.convertToAck(getMyProcessId()), pck.getSenderId());
        } else {
            //getProcess(pck.getSenderId()).notify(pck);
        }

        callback(pck);
    }

    private void sendPackets() {

        while (running.get()) {
            Pair p = getMyProcess().getNextPacket();

            if (p == null) {
                continue;
            }

            if (getMyProcess().removeAck(p)) {
                return;
            }

            link.enqueuePacket(p.getPacket(), p.getTarget());
            getMyProcess().addResendPacket(p);
        }

    }

    public void stopThreads() {
        running.set(false);
        link.stopThreads();
        worker.shutdownNow();
    }

}
