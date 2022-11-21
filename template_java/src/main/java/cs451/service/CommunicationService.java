package cs451.service;

import cs451.broadcast.FIFOBroadcast;
import cs451.channel.Link;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CommunicationService {

    private static FIFOBroadcast broadcast;
    private static BufferedWriter writer;

    public static void start(Parser parser) {

        Map<Byte, Host> hosts = parser.hosts();
        byte myId = parser.myId();
        int numMessages = parser.messages(), port = hosts.get(myId).getPort();

        Process.setMyHost(myId);
        Link.populateNetwork(hosts);

        try {
            // Write on fly: this saves memory while having the same throughput of writing at the end
            writer = new BufferedWriter(new FileWriter(parser.output()), 32768);
            broadcast = new FIFOBroadcast(port, hosts.size(), CommunicationService::broadcast, CommunicationService::deliver);
            broadcast.load(numMessages);

        } catch (IOException ignored) {}
    }

    public static void logAndTerminate() {
        interruptThreads();
        try {
            writer.close();
        } catch(IOException ignored) {}
    }

    private static void deliver(byte originId, int id) {
        synchronized (writer) {
            try {
                writer.write(String.format("d %d %d\n", originId + 1, id));
            } catch (IOException ignored) {}
        }
    }

    private static void broadcast(int id) {
        synchronized (writer) {
            try {
                writer.write(String.format("b %d\n", id));
            } catch (IOException ignored) {}
        }
    }

    private static void interruptThreads() {
        if (broadcast != null) {
            broadcast.stopThreads();
        }
    }

    private CommunicationService() {}

}
