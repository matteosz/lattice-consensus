package cs451.parser;

import cs451.message.Proposal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConfigParser {

    private final List<Proposal> proposals = new LinkedList<>();

    public boolean populate(String value, byte myHost) {
        File file = new File(value);
        try (BufferedReader br = new BufferedReader(new FileReader(file.getPath()))) {

            String[] header = br.readLine().split("\\s");
            if (header.length != 3) {
                return false;
            }
            try {
                int proposalNumber = Integer.parseInt(header[0]);
                int maxProposalLength = Integer.parseInt(header[1]);
                int maxDistinct = Integer.parseInt(header[2]);

                if (proposalNumber <= 0 || maxProposalLength <= 0 || maxDistinct <= 0) {
                    return false;
                }
                int p = 1;
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
                    proposals.add(new Proposal(p++, (byte) 0, myHost, values, 1));
                }
                if (proposals.size() != proposalNumber) {
                    return false;
                }

            } catch (NumberFormatException e) {
                return false;
            }

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public List<Proposal> getProposals() {
        return proposals;
    }
}
