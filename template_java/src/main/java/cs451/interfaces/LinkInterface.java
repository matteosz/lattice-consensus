package cs451.interfaces;

import cs451.message.Message;

public interface LinkInterface {

    void send(Message m, int targetId);

    void sendMany(int targetId, int sourceId, int numMessages);

}
