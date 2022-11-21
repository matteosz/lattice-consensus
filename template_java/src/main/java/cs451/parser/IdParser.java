package cs451.parser;

import static cs451.utilities.Utilities.fromIntegerToByte;

public class IdParser {

    private byte id;

    public boolean populate(String key, String value) {

        if (!key.equals("--id")) {
            return false;
        }

        try {
            int id = Integer.parseInt(value);
            if (id <= 0 || id > 128) {
                return false;
            }
            this.id = fromIntegerToByte(id);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public byte getId() {
        return id;
    }

}
