package cs451;

import cs451.broadcast.FIFOBroadcast;
import cs451.link.Link;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommunicationService {

    private static FIFOBroadcast broadcast;
    private static BufferedWriter writer;

    public static void start(Parser parser) {

        List<Host> hosts = parser.hosts();
        int myId = parser.myId(),
                numMessages = parser.getConfig().getMessages(),
                numHosts = hosts.size();

        Host myHost = hosts.get(myId - 1);

        Link.populateNetwork(hosts, myId);
        Process.setMyHost(myId);

        try {
            writer = new BufferedWriter(new FileWriter(parser.output()), 32768);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        broadcast = new FIFOBroadcast(myHost, numHosts, CommunicationService::broadcast, CommunicationService::deliver);

        broadcast.load(numMessages);
    }

    public static void logAndTerminate() {

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        interruptThreads();

    }

    private static void deliver(Packet packet) {
        packet.applyToMessages(CommunicationService::deliverMessage);
    }
    private static void broadcast(Packet packet) {
        packet.applyToMessages(CommunicationService::broadcastMessage);
    }

    private static void deliverMessage(Message m) {
        synchronized (writer) {
            try {
                writer.write(String.format("d %d %d\n", m.getFirstSender(), m.getMessageId()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void broadcastMessage(Message m) {
        synchronized (writer) {
            try {
                writer.write(String.format("b %d\n", m.getMessageId()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void interruptThreads() {
        broadcast.stopThreads();
    }

    private CommunicationService() {}

}
