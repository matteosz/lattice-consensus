package cs451.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HostsParser {

    private final Map<Byte, Host> hosts = new HashMap<>();

    public boolean populate(String key, String filename) {

        if (!key.equals("--hosts")) {
            return false;
        }

        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                if (line.isBlank()) {
                    continue;
                }

                String[] splits = line.split("\\s+");
                if (splits.length != 3) {
                    return false;
                }

                Host newHost = new Host();
                if (!newHost.populate(splits[0], splits[1], splits[2])) {
                    return false;
                }

                hosts.put(newHost.getId(), newHost);
            }
        } catch (IOException e) {
            return false;
        }

        if (!checkIdRange()) {
            return false;
        }

        return true;
    }

    private boolean checkIdRange() {
        int num = hosts.size();
        for (Host host : hosts.values()) {
            if (host.getId() >= num) {
                return false;
            }
        }

        return true;
    }

    public boolean inRange(byte id) {
        return id < hosts.size();
    }

    public Map<Byte, Host> getHosts() {
        return hosts;
    }
}
