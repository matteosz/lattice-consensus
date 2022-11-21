package cs451.parser;

import java.util.Map;

public class Parser {
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        parse(args);
    }

    public void parse(String[] args) {

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        if (args.length != 7) {
            help();
        }

        if (!idParser.populate(args[0], args[1])) {
            help();
        }

        if (!hostsParser.populate(args[2], args[3])) {
            help();
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help();
        }

        if (!outputParser.populate(args[4], args[5])) {
            help();
        }

        if (!configParser.populate(args[6])) {
            help();
        }
    }

    private void help() {
        System.exit(1);
    }

    public byte myId() {
        return idParser.getId();
    }

    public Map<Byte, Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public int messages() {
        return configParser.getMessages();
    }
}
