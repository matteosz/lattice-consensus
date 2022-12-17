package cs451.parser;

import cs451.service.CommunicationService;
import cs451.utilities.Parameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to parse the output filename.
 */
public class OutputParser {

    /**
     * Parse the output filename and create the writer.
     * @param key argument in the command line (--arg).
     * @param value value after the key.
     * @return true if correctly parsed, false otherwise.
     */
    public static boolean populate(String key, String value) {
        if (!key.equals("--output")) {
            System.err.println("Wrong arg used for output file");
            return false;
        }
        File file = new File(value);
        try {
            CommunicationService.writer = new BufferedWriter(new FileWriter(file.getPath()), 32768);
            Parameters.STARTED = true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}