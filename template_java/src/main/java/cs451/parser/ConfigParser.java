package cs451.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {

    private int messages;

    public boolean populate(String value) {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(value).getPath()))) {

            String param = br.readLine();
            try {
                messages = Integer.parseInt(param);

                if (messages <= 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public int getMessages() {
        return messages;
    }
}
