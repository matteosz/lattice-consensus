package cs451.parser;

import static cs451.process.Process.myHost;

import java.io.IOException;

/**
 * Parser class that takes the command line arguments
 * and parse them calling the specific sub-parser.
 */
public class Parser {

    /**
     * Parse the input.
     * @param args arguments passed from command line
     */
    public static void parse(String[] args) throws IOException {
        // Check that the arguments are 7
        if (args.length != 7 || !IdParser.populate(args[0], args[1])     ||
                                !HostsParser.populate(args[2], args[3])  ||
                                !OutputParser.populate(args[4], args[5]) ||
                                !ConfigParser.populate(args[6])) {
            help();
        }
    }

    /**
     * Help function to signal error in parsing
     */
    private static void help() {
        System.exit(1);
    }

}
