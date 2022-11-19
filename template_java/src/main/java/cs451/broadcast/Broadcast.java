package cs451.broadcast;

import cs451.message.Message;

import java.util.function.Consumer;

public abstract class Broadcast {

    private final Consumer<Message> packetCallback;

    protected Broadcast(Consumer<Message> packetCallback) {
        this.packetCallback = packetCallback;
    }

    protected void callback(Message message) {
        packetCallback.accept(message);
    }

}
