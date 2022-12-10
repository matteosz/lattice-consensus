package cs451.message;

/**
 * Compressor
 *
 * It's used to efficiently compress a set of values
 * by using a thread-safe lock-based linked list of ranges
 */
public class Compressor {

    /**
     * LinkedList Node
     */
    private static class Node {

        /** Left bound of the interval (included) */
        private int first;

        /** Right bound of the interval (included) */
        private int last;

        /** Next list's node */
        private Node next;

        /**
         * Node simple constructor
         * @param first
         * @param last
         * @param next
         */
        public Node(int first, int last, Node next) {
            this.first = first;
            this.last = last;
            this.next = next;
        }

        /**
         * Try to extend the given interval
         * This is only possible is value is consecutive to
         * first (on the left) or last (on the right)
         * @param value with which extending the interval
         * @return true if it's possible to extend, false otherwise
         */
        public boolean extend(int value) {
            if (first == value + 1) {
                // Extend to the left
                first = value;
            } else if (last == value - 1) {
                // Extend to the right
                last = value;
            } else {
                return false;
            }
            return true;
        }

        /**
         * Check the previous, current and next nodes, after
         * a list modification, to see if a couple of intervals can be
         * merged
         * @param prev previous node in the list
         */
        public void overlap(Node prev) {
            if (prev != null && prev.last == first - 1) {
                prev.last = last;
                prev.next = next;
            } else if (next != null && next.first == last + 1) {
                last = next.last;
                next = next.next;
            }
        }

        /**
         * Check if a value lies within the current interval
         * @param value
         * @return true if it's included, false otherwise
         */
        public boolean included(int value) {
            return value >= first && value <= last;
        }

        /**
         * Check if a given value precedes my interval
         * in the way that it's not consecutive on the left to first
         * @param value
         * @return true if value lies on the left of first, not being
         *         consecutive to it, false otherwise
         */
        public boolean follows(int value) {
            return first > value + 1;
        }
    }

    /** Head of the linked list */
    private Node head;

    /** Access flag used to synchronize the access */
    private final Boolean access;

    /**
     * Create an empty compressor
     */
    public Compressor() {
        this.access = Boolean.TRUE;
        this.head = null;
    }

    /**
     * Create a compressor given an initial value
     * @param offset initial value in the compressor
     */
    public Compressor(int offset) {
        this.access = Boolean.TRUE;
        head = new Node(offset, offset, null);
    }

    /**
     * Check if the list contains a value
     * @param value
     * @return true if it's in the list, false otherwise
     */
    public boolean contains(int value) {
        synchronized (access) {
            // If value < curr.first early stop the search
            for (Node curr = head; curr != null && value >= curr.first; curr = curr.next) {
                if (curr.included(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Add a value to the list in the right position
     * @param value
     * @return true if added, false if already there
     */
    public boolean add(int value) {
        synchronized (access) {
            if (head == null) {
                head = new Node(value, value, null);
                return true;
            }
            Node prev = null;
            for (Node curr = head; curr != null; prev = curr, curr = curr.next) {
                if (curr.included(value)) {
                    // Already in the list
                    return false;
                }
                // If with this value I can extend the current interval
                if (curr.extend(value)) {
                    // After having extended the interval check for overlaps and solve them
                    curr.overlap(prev);
                    return true;
                }
                // If the value lies before my current interval but has not matched a previous one
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
            // If arrived here then just append at the end the value
            prev.next = new Node(value, value, null);
            return true;
        }
    }

    /**
     * Remove a value from the list
     * @param value
     * @return true if correctly removed, false if not present
     */
    public boolean remove(int value) {
        synchronized (access) {
            Node prev = null;
            for (Node curr = head; curr != null && curr.first <= value; prev = curr, curr = curr.next) {
                if (!curr.included(value)) {
                    continue;
                }
                // If it's included in the curr interval
                if (curr.first == value) {
                    if (curr.last == value) {
                        // Simply skip the item
                        prev.next = curr.next;
                    } else {
                        curr.first++;
                    }
                } else if (curr.last == value) {
                    curr.last--;
                } else {
                    Node split = new Node(value + 1, curr.last, curr.next);
                    curr.last = value - 1;
                    curr.next = split;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @return the right bound of the head, -1 if head is null
     */
    public int takeLast() {
        if (head == null) {
            return -1;
        }
        return head.last;
    }

}
