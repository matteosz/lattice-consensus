package cs451.service;

import static cs451.process.Process.myHost;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.FairLossLink;
import cs451.channel.Network;
import cs451.channel.StubbornLink;
import cs451.consensus.LatticeConsensus;
import cs451.parser.HostsParser;
import cs451.parser.OutputParser;
import cs451.process.Process;
import cs451.parser.Host;

import cs451.utilities.Parameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Manager class to start the consensus and
 * deliver proposals by writing the output.
 *
 * Main functions:
 *  1) Load all the classes.
 *  2) Write the logs.
 */
public class CommunicationService {

    /** BufferedWriter to write efficiently the output file */
    public static BufferedWriter writer;

    /**
     * Start the consensus and initialize all static fields
     */
    public static void start() {
        // Retrieves the information from the parser
        Map<Byte, Host> hosts = HostsParser.getHosts();

        // Static initialization
        Parameters.setParams(hosts.size());
        Process.initialize();
        Network.populateNetwork(hosts);

        // Start the consensus
        try {
            LatticeConsensus.start(hosts.get(myHost).getPort(), hosts.size(), CommunicationService::deliver);
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
     * Deliver a proposal by writing it into the file as new line.
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
                    writer.flush();
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
