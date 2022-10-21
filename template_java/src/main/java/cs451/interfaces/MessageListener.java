package cs451.interfaces;

import cs451.message.Message;

@FunctionalInterface
public interface MessageListener {
    void apply(Message m);
}
