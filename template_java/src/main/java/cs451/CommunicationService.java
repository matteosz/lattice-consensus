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

    private static PerfectLink perfectLink;
    private static Parser parser;
    private static Process process;

    public static void start(Parser parse) {

        parser = parse;

        List<Host> hosts = parser.hosts();

        int myId = parser.myId(), targetId = parser.getConfig().getTarget();
        int numMessages = parser.getConfig().getMessages();

        Link.populateNetwork(hosts, targetId);

        process = perfectLink.getProcess(myId);
        process.run(numMessages);

        perfectLink = new PerfectLink(myId, hosts.get(myId-1).getPort());

    }

    public static void log() {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parser.output()), 32768)) {
            bw.write(process.logAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private CommunicationService() {}

}
