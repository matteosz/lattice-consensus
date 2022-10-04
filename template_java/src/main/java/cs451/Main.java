package cs451;

import java.io.IOException;
import java.util.List;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::handleSignal));
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        List<Host> hosts = parser.hosts();
        int myId = parser.myId();
        Host myHost = hosts.get(myId);
        Config config = parser.getConfig();
        Host target = hosts.get(config.getTarget());
        int messages = config.getMessages();

        if (myId == config.getTarget()) {
            Process receiver = new Process(target, messages, hosts.size());
            receiver.listenAll();
        }
        else {
            Process sender = new Process(myHost, target, messages);
            sender.sendAll();
        }
        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
