package cs451;

import cs451.parser.Parser;

public class Main {

    private static void handleSignal() {
        CommunicationService.log();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::handleSignal));
    }

    public static void main(String[] args) throws InterruptedException {

        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        CommunicationService.start(parser);

        while (true) {
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
