package cs451.channel;

import cs451.message.Message;
import cs451.message.Packet;
import cs451.process.Process;

import java.net.SocketException;
import java.util.function.Consumer;

import static cs451.channel.Link.getProcess;

public class PerfectLink {

    private final StubbornLink link;
    private final Consumer<Message> messageCallback;

    public PerfectLink(int port, Consumer<Message> messageCallback) throws SocketException {
        this.messageCallback = messageCallback;
        link = new StubbornLink(port, this::perfectDeliver);
    }

    private void perfectDeliver(Packet packet) {
        Process sender = getProcess(packet.getSenderId());

        if (sender.deliverPacket(packet)) {
            packet.applyToMessages(m -> callback(m, sender));
        }
    }

    private void callback(Message message, Process sender) {
        if (sender.deliver(message)) {
            messageCallback.accept(message);
        }
    }

    public void load(int numMessages, byte targetId) {
        getProcess(targetId).load(numMessages);
    }
    public void send(Message message, byte targetId) {
        getProcess(targetId).addMessage(message);
    }

    public void stopThreads() {
        link.stopThreads();
    }
}
