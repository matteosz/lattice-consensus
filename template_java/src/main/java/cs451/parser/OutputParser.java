package cs451.parser;

import cs451.service.CommunicationService;
import java.io.File;

/**
 * Class to parse the output filename.
 */
public class OutputParser {

    /**
     * Parse the output filename.
     * @param key argument in the command line (--arg)
     * @param value value after the key
     * @return true if correctly parsed, false otherwise
     */
    public static boolean populate(String key, String value) {
        if (!key.equals("--output")) {
            System.err.println("Wrong arg used for output file");
            return false;
        }
        File file = new File(value);
        CommunicationService.output = file.getPath();
        return true;
    }

}