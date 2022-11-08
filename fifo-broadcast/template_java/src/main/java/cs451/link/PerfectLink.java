package cs451.link;

import cs451.message.Packet;
import cs451.process.Process;

public class PerfectLink extends Link {

    private final StubbornLink link;

    public PerfectLink(int id, int port, int numHosts) {
        super(id);
        link = new StubbornLink(id, port, this::deliver, numHosts);
    }

    public void stopThreads() {
        link.stopThreads();
    }

    private void deliver(Packet packet) {

        Process process = getProcess(getId());

        if (!process.hasDelivered(packet)) {

            process.deliver(packet);

        }
        if (process.isSending(packet)){

            process.stopSending(packet);

        }
    }
}