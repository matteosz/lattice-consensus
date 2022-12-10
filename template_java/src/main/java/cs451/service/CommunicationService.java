package cs451.service;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.FairLossLink;
import cs451.channel.Network;
import cs451.channel.StubbornLink;
import cs451.consensus.LatticeConsensus;
import cs451.message.Proposal;
import cs451.parser.ConfigParser;
import cs451.parser.HostsParser;
import cs451.parser.IdParser;
import cs451.parser.OutputParser;
import cs451.process.Process;
import cs451.parser.Host;
import cs451.parser.Parser;

import cs451.utilities.Parameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Manager class to start the consensus and
 * deliver proposals by writing the output
 *
 * Main functions:
 *  1) Load all the classes
 *  2) Write the logs
 */
public class CommunicationService {

    /** BufferedWriter to write efficiently the output file */
    private static BufferedWriter writer;

    /**
     * Start the consensus and initialize all static fields
     */
    public static void start() {
        // Retrieves the information from the parser
        Map<Byte, Host> hosts = HostsParser.getHosts();
        byte myId = IdParser.getId();
        LinkedList<Proposal> proposals = ConfigParser.getProposals();

        // Static initialization
        Parameters.setParams(hosts.size());
        Process.initialize(myId, proposals);
        Network.populateNetwork(hosts);

        // Initialize the BufferedWriter and start the consensus
        try {
            writer = new BufferedWriter(new FileWriter(OutputParser.getPath()), 32768);
            LatticeConsensus.start(hosts.get(myId).getPort(), hosts.size(), CommunicationService::deliver, proposals);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After the SIGINT or SIGTERM, stop all threads
     * and close the output file
     */
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

    /**
     * Deliver a proposal by writing it into the file as new line
     * @param proposals (simply set of integers) to deliver
     */
    private static void deliver(Set<Integer> proposals) {
        synchronized (writer) {
            try {
                Iterator<Integer> iterator = proposals.iterator();
                while (iterator.hasNext()) {
                    int num = iterator.next();
                    // Check if it's last number
                    if (!iterator.hasNext()) {
                        writer.write(String.format("%d\n", num));
                    } else {
                        writer.write(String.format("%d ", num));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Interrupt all running threads
     * of underlying instances
     */
    private static void interruptThreads() {
        FairLossLink.stopThreads();
        StubbornLink.stopThreads();
        BestEffortBroadcast.stopThreads();
    }

}
