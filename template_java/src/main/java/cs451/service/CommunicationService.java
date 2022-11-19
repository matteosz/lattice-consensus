package cs451.service;

import cs451.broadcast.FIFOBroadcast;
import cs451.channel.Link;
import cs451.process.Process;
import cs451.utilities.Utilities;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

public class CommunicationService {

    private static FIFOBroadcast broadcast;
    private static StringBuilder sb = new StringBuilder();
    private static String output;

    public static void start(Parser parser) {

        List<Host> hosts = parser.hosts();
        int myId = parser.myId(), numMessages = parser.getConfig().getMessages(),
                port = hosts.get(myId - 1).getPort();

        output = parser.output();
        Process.setMyHost(Utilities.fromIntegerToByte(myId));
        Link.populateNetwork(hosts);

        try {

            broadcast = new FIFOBroadcast(port, hosts.size(), CommunicationService::broadcast, CommunicationService::deliver);
            broadcast.load(numMessages);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logAndTerminate() {
        interruptThreads();
        synchronized (sb) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output), 32768)) {
                writer.write(sb.toString());
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private static void deliver(byte originId, int id) {
        synchronized (sb) {
            sb.append(String.format("d %d %d\n", originId + 1, id));
        }
    }

    private static void broadcast(int id) {
        synchronized (sb) {
            sb.append(String.format("b %d\n", id));
        }
    }

    private static void interruptThreads() {
        if (broadcast != null) {
            broadcast.stopThreads();
        }
    }

    private CommunicationService() {}

}
