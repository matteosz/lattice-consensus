package cs451.parser;

import java.io.File;

/**
 * Class to parse the output filename.
 */
public class OutputParser {

    /** Output file path */
    private static String path;

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
        path = file.getPath();
        return true;
    }

    /**
     * @return output file path
     */
    public static String getPath() {
        return path;
    }

}