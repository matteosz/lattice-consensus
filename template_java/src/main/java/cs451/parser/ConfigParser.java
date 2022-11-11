package cs451.parser;

import java.io.File;

public class ConfigParser {

    private String path;
    private Config config;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        config = new Config();
        return config.populate(path);
    }

    public Config getConfig() {
        return config;
    }
}