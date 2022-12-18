package cs451.service;

import cs451.channel.Network;
import cs451.consensus.LatticeConsensus;

import cs451.parser.ConfigParser;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager class to start the consensus and
 * deliver proposals by writing the output.
 * <p>
 * Main functions:
 *  1) Load all the classes.
 *  2) Write the logs.
 */
public class CommunicationService {

    /** Buffered Writer to write logs on output file. */
    public static BufferedWriter writer;

    /** Running flag. */
    public static final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Start the consensus and initialize all static fields.
     */
    public static void start() {
        Network.populateNetwork();
        try {
            LatticeConsensus.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After the SIGINT or SIGTERM, stop all threads
     * and logs into the output file.
     */
    public static void logAndTerminate() {
        running.set(false);
        try {
            synchronized (writer) {
                writer.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        ConfigParser.closeFile();
    }

    /**
     * Deliver a proposal by writing it into the sb as new line.
     * @param proposals (simply set of integers) to deliver.
     */
    public static void deliver(Set<Integer> proposals) {
        try {
            // Early stop to avoid writing after program termination
            if (!running.get()) {
                return;
            }
            synchronized (writer) {
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
                writer.flush();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
