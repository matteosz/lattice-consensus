package cs451;

import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommunicationService {

    private static final int BATCH_SIZE = 100000;

    private static PerfectLink perfectLink;
    private static Parser parser;
    private static Process process;

    public static void start(Parser parse) {

        parser = parse;

        List<Host> hosts = parser.hosts();

        int myId = parser.myId(), targetId = parser.getConfig().getTarget();
        int numMessages = parser.getConfig().getMessages();

        Link.populateNetwork(hosts, targetId);

        perfectLink = new PerfectLink(myId, hosts.get(myId-1).getPort());
        process = perfectLink.getProcess(myId);

        process.run(0, numMessages);

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
        perfectLink.stopThreads();
    }

    private CommunicationService() {}

}
