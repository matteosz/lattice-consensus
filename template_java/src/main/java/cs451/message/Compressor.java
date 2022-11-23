package cs451.message;

public class Compressor {

    private static class Node {
        private int first, last;
        private Node next;

        public Node(int first, int last, Node next) {
            this.first = first;
            this.last = last;
            this.next = next;
        }

        public boolean extend(int value) {
            if (first == value + 1) {
                first = value;
            } else if (last == value - 1) {
                last = value;
            } else {
                return false;
            }
            return true;
        }

        public void overlap(Node prev) {
            if (prev != null && prev.last == first - 1) {
                prev.last = last;
                prev.next = next;
            } else if (next != null && next.first == last + 1) {
                last = next.last;
                next = next.next;
            }
        }

        public boolean included(int value) {
            return value >= first && value <= last;
        }

        public boolean follows(int value) {
            return first > value + 1;
        }
    }

    private Node head;
    private final Boolean access;

    public Compressor() {
        this.access = Boolean.TRUE;
        this.head = null;
    }

    public void setHead(int first, int last) {
        synchronized (access) {
            head = new Node(first, last, head);
        }
    }

    public boolean contains(int value) {
        synchronized (access) {
            for (Node curr = head; curr != null && value >= curr.first; curr = curr.next) {
                if (curr.included(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean add(int value) {
        synchronized (access) {
            if (head == null) {
                head = new Node(value, value, null);
                return true;
            }
            Node prev = null;
            for (Node curr = head; curr != null; prev = curr, curr = curr.next) {
                if (curr.included(value)) {
                    return false;
                }
                if (curr.extend(value)) {
                    curr.overlap(prev);
                    return true;
                }
                if (curr.follows(value)) {
                    Node previous = new Node(value, value, curr);
                    if (prev == null) {
                        head = previous;
                    } else {
                        prev.next = previous;
                    }
                    return true;
                }

            }
            prev.next = new Node(value, value, null);
            return true;
        }
    }

    public int takeFirst() {
        synchronized (access) {
            if (head == null) {
                return -1;
            }
            int value = head.first;
            shiftHead();
            return value;
        }
    }

    public int getHeadLast() {
        if (head == null) {
            return -1;
        }
        return head.last;
    }

    private void shiftHead() {
        if (head.first == head.last) {
            head = head.next;
        } else {
            head.first++;
        }
    }

}
