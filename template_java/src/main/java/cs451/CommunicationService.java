package cs451;

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

        if (process.isTarget()) {
            return;
        }

        List<Message> packet = new LinkedList<>();

        for (int i = 1; i <= numMessages; i++) {

            Message m = Message.createMessage(targetId, i);

            process.sendEvent(m);

            packet.add(m);

            if (packet.size() == Packet.MAX_COMPRESSION) {
                process.load(packet);
                packet = new LinkedList<>();
            }

        }

        if (packet.size() > 0)
            process.load(packet);
    }

    public static void log() {

        interruptThreads();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parser.output()), 32768)) {
            bw.write(process.logAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Thread.currentThread().interrupt();
    }

    private static void interruptThreads() {
        perfectLink.stopThreads();
    }

    private CommunicationService() {}

}
