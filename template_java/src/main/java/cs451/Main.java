package cs451;

import cs451.parser.Parser;

public class Main {

    private static void handleSignal() {
        CommunicationService.logAndTerminate();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::handleSignal));
    }

    public static void main(String[] args) throws InterruptedException {

        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        CommunicationService.start(parser);

        while (true) {
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
