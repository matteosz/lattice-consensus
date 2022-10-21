package cs451.helper;

import java.util.Objects;

public class Event {

    private char mode;
    private int sequenceNr, sender = 0;

    public Event(char mode, int sequenceNr, int sender) {
        this(mode, sequenceNr);
        this.sender = sender;
    }

    public Event(char mode, int sequenceNr) {
        this.mode = mode;
        this.sequenceNr = sequenceNr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mode);
        if (mode == 'b') {
            sb.append(" ").append(Integer.toString(sequenceNr));
        } else if (mode == 'd') {
            sb.append(" ").append(Integer.toString(sender)).append(" ").append(Integer.toString(sequenceNr));
        }
        return sb.append("\n").toString();
    }
}
