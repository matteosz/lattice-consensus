package cs451.parser;

import static cs451.utilities.Utilities.fromIntegerToByte;

import cs451.process.Process;

/**
 * Class to parse the id of my local host.
 */
public class IdParser {

    /**
     * Parse the id from key argument and value.
     * @param key argument in the command line (--arg).
     * @param value value after the key.
     * @return true if correctly parsed, false otherwise.
     */
    public static boolean populate(String key, String value) {
        if (!key.equals("--id")) {
            System.err.println("Wrong arg used for id");
            return false;
        }
        try {
            int id = Integer.parseInt(value);
            if (id <= 0 || id > 128) {
                System.err.println("Local id not valid");
                return false;
            }
            Process.MY_HOST = fromIntegerToByte(id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
