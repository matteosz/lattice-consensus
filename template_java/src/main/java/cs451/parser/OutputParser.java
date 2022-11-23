package cs451.parser;

import java.io.File;

public class OutputParser {

    private String path;

    public boolean populate(String key, String value) {

        if (!key.equals("--output")) {
            return false;
        }

        path = new File(value).getPath();
        return true;
    }

    public String getPath() {
        return path;
    }

}