package cs451;

import cs451.broadcast.FIFOBroadcast;
import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.message.Message;
import cs451.message.Packet;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CommunicationService {

    private static FIFOBroadcast broadcast;
    private static Parser parser;
    private static Process process;

    public static void start(Parser parse) {

        parser = parse;

        List<Host> hosts = parser.hosts();

        Link.populateNetwork(hosts);

        int myId = parser.myId();
        process = Link.getProcess(myId);

        int numMessages = parser.getConfig().getMessages();
        int numHosts = hosts.size();

        broadcast = new FIFOBroadcast(process, myId, numHosts);

        broadcast.start(numMessages);
    }

    public static void log() {

        interruptThreads();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parser.output()), 32768)) {
            bw.write(process.logAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void interruptThreads() {
        broadcast.stopThreads();
    }

    private CommunicationService() {}

}
