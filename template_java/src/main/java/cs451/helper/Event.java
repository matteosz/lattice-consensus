package cs451.helper;

import java.util.Objects;

public class Event {

    private char mode;
    private int sequenceNr, sender = 0;
    private int myId;

    public Event(char mode, int sequenceNr, int sender, int myId) {
        this(mode, sequenceNr, myId);
        this.sender = sender;
    }

    public Event(char mode, int sequenceNr, int myId) {
        this.mode = mode;
        this.sequenceNr = sequenceNr;
        this.myId = myId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mode);
        if (mode == 'b') {
            sb.append(" ").append(Integer.toString(sequenceNr));
        }
        else if (mode == 'd'){
            sb.append(" ").append(Integer.toString(sender)).append(" ").append(Integer.toString(sequenceNr));
        }
        return sb.append("\n").toString();
    }

    public int getId() {
        return myId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return mode == event.mode && sequenceNr == event.sequenceNr && sender == event.sender && myId == event.myId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, sequenceNr, sender, myId);
    }
}
