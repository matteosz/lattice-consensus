package cs451;

import cs451.link.PerfectLink;
import cs451.link.Process;
import cs451.parser.Config;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommunicationService {

    private static PerfectLink pl;
    private static Process process;
    private static Parser parser;

    private CommunicationService() {}

    public static void start(Parser parser, Config config) {
        CommunicationService.parser = parser;

        List<Host> hosts = parser.hosts();

        int myId = parser.myId(), targetId = config.getTarget(), numMessages = config.getMessages();

        Host myHost = hosts.get(myId-1);


        process = new Process(myHost, hosts.size(), targetId == myId);
        process.run(numMessages, targetId);
        pl = new PerfectLink(myId, myHost.getPort(), hosts, targetId);

    }

    public static void log() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parser.config()), 32768)) {
            bw.write(process.logAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        pl.closeSocket();
    }

}
