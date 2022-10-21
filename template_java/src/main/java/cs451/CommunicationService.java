package cs451;

import cs451.link.Link;
import cs451.link.PerfectLink;
import cs451.link.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommunicationService {

    private static PerfectLink pl;
    private static Parser parser;
    private static Process process;

    private CommunicationService() {}

    public static void start(Parser parser) {
        CommunicationService.parser = parser;

        List<Host> hosts = parser.hosts();

        int myId = parser.myId(), targetId = parser.getConfig().getTarget();
        int numMessages = parser.getConfig().getMessages();

        Link.populateNetwork(hosts, targetId);

        process = pl.getNetwork().get(myId);
        process.run(numMessages);

        pl = new PerfectLink(myId, hosts.get(myId-1).getPort(), hosts);
    }

    public static void log() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parser.output()), 32768)) {
            bw.write(process.logAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
