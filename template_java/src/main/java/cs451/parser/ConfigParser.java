package cs451.parser;

import static cs451.consensus.LatticeConsensus.originals;

import cs451.message.Proposal;
import java.io.BufferedReader;
import java.io.File;
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

    /**
     * Read the config file and populate the proposals list.
     * @param value config filename
     * @param myHost id of my host
     * @return true if correctly parsed, false otherwise
     */
    public static boolean populate(String value, byte myHost) {
        File file = new File(value);
        try (BufferedReader br = new BufferedReader(new FileReader(file.getPath()))) {
            // Read the header -> p vs ds
            String[] header = br.readLine().split("\\s");
            if (header.length != 3) {
                System.err.println("Header of config not correct");
                return false;
            }
            try {
                int proposalNumber = Integer.parseInt(header[0]);
                int maxProposalLength = Integer.parseInt(header[1]);
                // Check if are valid integers
                if (proposalNumber <= 0 || maxProposalLength <= 0) {
                    return false;
                }
                originals = new LinkedList<>();
                int p = 0;
                for(String line; (line = br.readLine()) != null; ) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] splits = line.split("\\s");
                    if (splits.length > maxProposalLength) {
                        return false;
                    }
                    Set<Integer> values = new HashSet<>(maxProposalLength);
                    for (String split : splits) {
                        values.add(Integer.parseInt(split));
                    }
                    // Add in tail to the list
                    originals.add(new Proposal(p++, (byte) 0, myHost, values, 1));
                }
                if (originals.size() != proposalNumber) {
                    System.err.println("Not as many proposals as declared in the header");
                    return false;
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
