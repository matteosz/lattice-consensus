package cs451.parser;

import java.util.List;

import static cs451.utilities.Utilities.*;

public class Parser {

    private String[] args;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public void parse() {

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if (argsNum != ARG_LIMIT_CONFIG) {
            help();
        }

        if (!idParser.populate(args[ID_KEY], args[ID_VALUE])) {
            help();
        }

        if (!hostsParser.populate(args[HOSTS_KEY], args[HOSTS_VALUE])) {
            help();
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help();
        }

        if (!outputParser.populate(args[OUTPUT_KEY], args[OUTPUT_VALUE])) {
            help();
        }

        if (!configParser.populate(args[CONFIG_VALUE])) {
            help();
        }
    }

    private void help() {
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public List<Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public Config getConfig() {
        return configParser.getConfig();
    }
}
