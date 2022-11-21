package cs451;

import cs451.parser.Parser;
import cs451.service.CommunicationService;

public class Main {

    private static void handleSignal() {
        CommunicationService.logAndTerminate();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::handleSignal));
    }

    public static void main(String[] args) throws InterruptedException {

        Parser parser = new Parser(args);

        initSignalHandlers();

        CommunicationService.start(parser);

        while (true) {
            Thread.sleep(3600000);
        }
    }
}
