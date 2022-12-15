package cs451.parser;

import static cs451.consensus.LatticeConsensus.originals;
import static cs451.message.Packet.MAX_COMPRESSION;
import static cs451.process.Process.myHost;
import static cs451.utilities.Parameters.PROPOSAL_BATCH;

import cs451.message.Packet;
import cs451.message.Proposal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * The ConfigParser reads the .config file,
 * check if it's properly formatted and then
 * create a list of all the proposals.
 */
public class ConfigParser {

    /** BufferedReader to read from config files the proposals */
    private static BufferedReader br;

    /** Current last proposal read */
    private static int currentProposal = 0;

    /** Total number of proposals */
    public static int totalProposal;

    /** Maximum number of distinct elements in a proposal's shot */
    private static int maxProposalLength;

    /**
     * Read the config file and populate the proposals list.
     * @param value config filename
     * @return true if correctly parsed, false otherwise
     */
    public static boolean populate(String value) throws IOException {
        File file = new File(value);
        try {
            br = new BufferedReader(new FileReader(file.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        };
        // Read the header -> p vs ds
        String[] header = br.readLine().split("\\s");
        if (header.length != 3) {
            System.err.println("Header of config not correct");
            return false;
        }
        try {
            totalProposal = Integer.parseInt(header[0]);
            maxProposalLength = Integer.parseInt(header[1]);
            int maxDistinctValues = Integer.parseInt(header[2]);
            // Check if are valid integers
            if (totalProposal <= 0 || maxProposalLength <= 0 || maxDistinctValues <= 0) {
                return false;
            }
            // Set the batch accordingly with ds
            if (maxDistinctValues > 100 && maxDistinctValues <= 200) {
                PROPOSAL_BATCH >>= 1;
            }
            if (maxDistinctValues > 200 && maxDistinctValues < 300) {
                PROPOSAL_BATCH >>= 2;
            } else if (maxDistinctValues >= 300 && maxDistinctValues < 600) {
                PROPOSAL_BATCH >>= 3;
            } else if (maxDistinctValues >= 600 && maxDistinctValues < 800) {
                PROPOSAL_BATCH >>= 4;
            } else if (maxDistinctValues >= 800) {
                PROPOSAL_BATCH >>= 5;
            }
            // Set global maximum packet size
            Packet.MAX_PACKET_SIZE = Packet.HEADER + MAX_COMPRESSION * (maxDistinctValues + 4) * Integer.BYTES;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        // Load first batch of proposals
        originals = new LinkedList<>();
        for(String line; (line = br.readLine()) != null && currentProposal < PROPOSAL_BATCH; ) {
            if (line.isBlank()) {
                continue;
            }
            readLine(line);
            ++currentProposal;
        }
        if (currentProposal == totalProposal) {
            br.close();
        }
        return true;
    }


    /**
     * Read a "packet" of proposal.
     * @return true if read at least one, false otherwise
     */
    public static boolean readProposals() {
        // If already read all proposals
        if (currentProposal == totalProposal) {
            return false;
        }
        synchronized (br) {
            try {
                int p = 0;
                for (String line; (line = br.readLine()) != null && p < Math.min(MAX_COMPRESSION, PROPOSAL_BATCH); ) {
                    if (line.isBlank()) {
                        continue;
                    }
                    readLine(line);
                    if (++currentProposal == totalProposal) {
                        br.close();
                        break;
                    }
                    ++p;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return !originals.isEmpty();
            }
        }
        return true;
    }

    /**
     * Read and parse a line of the config file.
     * @param line string with proposal's set
     * @return true if correctly parsed, false otherwise
     */
    private static void readLine(String line) {
        String[] splits = line.split("\\s");
        Set<Integer> values = new HashSet<>(maxProposalLength);
        for (String split : splits) {
            values.add(Integer.parseInt(split));
        }
        originals.add(new Proposal(currentProposal, (byte) 0, myHost, values, 1));
    }

    /**
     * Close the file reader if still open
     */
    public static void closeFile() {
        if (currentProposal == totalProposal) {
            return;
        }
        synchronized (br) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
