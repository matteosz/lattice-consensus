package cs451.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    private int messages;

    public boolean populate(String path) {

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String param = br.readLine();
            try {
                messages = Integer.parseInt(param);

                if (messages <= 0) {
                    System.err.println("Number of messages must be positive");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.err.println("m must be a number");
                return false;
            }

        } catch (IOException e) {
            System.out.println("Impossible to find the config file under path: " + path);
            return false;
        }

        return true;
    }

    public int getMessages() {
        return messages;
    }
}
