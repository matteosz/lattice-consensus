package cs451.helper;

public class Operations {

    public static final int MASK = 0Xff;
    public static final int BYTES = Integer.BYTES;
    public static final int BITS = BYTES * 8;

    public static void fromIntegerToByte(int x, byte[] array, int offset) {

        for (int i = 0; i < BYTES; i++) {
            array[offset + i] = (byte) (x >> BITS - 8 * (i + 1));
        }

    }

    public static int fromByteToInteger(byte[] array, int offset) {
        int finalInt = 0;

        for (int i = 0; i < BYTES; i++) {
            finalInt = (finalInt << BYTES) | (int) array[offset + i] & MASK;
        }

        return finalInt;
    }

    private Operations() {}
}
