package cs451.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    private int target, messages;

    public boolean populate(String path) {

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String[] params = br.readLine().split(" ");
            try {
                messages = Integer.parseInt(params[0]);
                target = Integer.parseInt(params[1]);
                if (messages <= 0) {
                    System.err.println("Number of messages must be positive");
                }
                if (target <= 0) {
                    System.err.println("Id of the receiver must be a positive number");
                }
            } catch (NumberFormatException e) {
                System.err.println("m and i must be numbers");
                return false;
            }

        } catch (IOException e) {
            System.out.println("Impossible to find the config file under path: " + path);
            return false;
        }

        return true;
    }

    public int getTarget() {
        return target;
    }

    public int getMessages() {
        return messages;
    }
}
