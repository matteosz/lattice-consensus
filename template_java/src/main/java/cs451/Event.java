package cs451;

public class Event {

    private char mode;
    private int sequenceNr, sender;

    public Event(char mode, int sequenceNr, int sender) {
        this.mode = mode;
        this.sequenceNr = sequenceNr;
        this.sender = sender;
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
}
