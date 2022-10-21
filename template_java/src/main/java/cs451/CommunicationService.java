package cs451;

import cs451.link.PerfectLink;
import cs451.link.Process;
import cs451.parser.Config;
import cs451.parser.Host;
import cs451.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class CommunicationService {

    private static CommunicationService instance = null;
    private static PerfectLink pl;
    private static Process p;

    private CommunicationService() {}

    public static CommunicationService getInstance() {
        return instance;
    }

    public static void createInstance() {
        if (instance == null) {
            synchronized (CommunicationService.class) {
                if (instance == null)
                    instance = new CommunicationService();
            }
        }
    }

    public static void start(Parser parser, Config config) {
        List<Host> hosts = parser.hosts();

        int myId = parser.myId(), numMessages = config.getMessages();

        Host myHost = hosts.get(myId-1), target = hosts.get(config.getTarget()-1);

        pl =



    }

    public static void log() {

        pl.closeSocket();



    }

}
