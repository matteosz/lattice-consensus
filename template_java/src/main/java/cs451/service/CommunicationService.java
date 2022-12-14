package cs451.service;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.FairLossLink;
import cs451.channel.Network;
import cs451.channel.StubbornLink;
import cs451.consensus.LatticeConsensus;
import cs451.process.Process;

import cs451.utilities.Parameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
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

    /** Output file name */
    public static String output;

    /** String builder containing the logs to write */
    private static final StringBuilder sb = new StringBuilder(32768);

    /**
     * Start the consensus and initialize all static fields
     */
    public static void start() {
        // Static initialization
        Parameters.setParams();
        Network.populateNetwork();

        // Start the consensus
        try {
            LatticeConsensus.start(CommunicationService::deliver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After the SIGINT or SIGTERM, stop all threads
     * and logs into the output file
     */
    public static void logAndTerminate() {
        interruptThreads();
        synchronized (sb) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output), 32768)) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deliver a proposal by writing it into the sb as new line.
     * @param proposals (simply set of integers) to deliver
     */
    private static void deliver(Set<Integer> proposals) {
        synchronized (sb) {
            Iterator<Integer> iterator = proposals.iterator();
            while (iterator.hasNext()) {
                int num = iterator.next();
                // Check if it's last number
                if (!iterator.hasNext()) {
                    sb.append(String.format("%d\n", num));
                } else {
                    sb.append(String.format("%d ", num));
                }
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
