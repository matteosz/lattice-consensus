package cs451.service;

import cs451.channel.Link;
import cs451.consensus.LatticeConsensus;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import cs451.utilities.Parameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CommunicationService {

    private static LatticeConsensus consensus;
    private static BufferedWriter writer;

    public static void start(Parser parser) {

        Map<Byte, Host> hosts = parser.hosts();
        byte myId = parser.myId();

        Parameters.setParams(hosts.size());
        Process.setMyHost(myId);
        Link.populateNetwork(hosts);

        try {
            writer = new BufferedWriter(new FileWriter(parser.output()), 32768);
            consensus = new LatticeConsensus(hosts.get(myId).getPort(), hosts.size(), CommunicationService::deliver, parser.getProposals().size());
            consensus.start(parser.getProposals());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logAndTerminate() {
        interruptThreads();
        synchronized (writer) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void deliver(Set<Integer> proposals, int id) {
        synchronized (writer) {
            try {
                writer.write(proposals.toString().replaceAll(",", "").replaceAll("\\[|\\]", ""));
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void interruptThreads() {
        if (consensus != null) {
            consensus.stopThreads();
        }
    }

    private CommunicationService() {}

}
