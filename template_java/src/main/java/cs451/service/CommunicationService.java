package cs451.service;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.FairLossLink;
import cs451.channel.Network;
import cs451.channel.StubbornLink;
import cs451.consensus.LatticeConsensus;

import cs451.parser.ConfigParser;
import java.io.BufferedWriter;
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

    /** Buffered Writer to write logs on output file */
    public static BufferedWriter writer;

    /** Flush to alternate flushing for improved performance */
    private static boolean flush = true;

    /**
     * Start the consensus and initialize all static fields
     */
    public static void start() {
        Network.populateNetwork();
        // Start the consensus
        try {
            LatticeConsensus.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After the SIGINT or SIGTERM, stop all threads
     * and logs into the output file
     */
    public static void logAndTerminate() {
        BestEffortBroadcast.stopThreads();
        StubbornLink.stopThreads();
        FairLossLink.stopThreads();
        synchronized (writer) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ConfigParser.closeFile();
    }

    /**
     * Deliver a proposal by writing it into the sb as new line.
     * @param proposals (simply set of integers) to deliver
     */
    public static void deliver(Set<Integer> proposals) {
        synchronized (writer) {
            Iterator<Integer> iterator = proposals.iterator();
            try {
                while (iterator.hasNext()) {
                    int num = iterator.next();
                    // Check if it's last number
                    if (!iterator.hasNext()) {
                        writer.write(String.format("%d\n", num));
                    } else {
                        writer.write(String.format("%d ", num));
                    }
                }
                if (flush) {
                    writer.flush();
                }
                flush = !flush;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
