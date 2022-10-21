package cs451.helper;

public class Operations {

    private Operations() {}

    public static void intToByte(int x, byte[] array, int offset) {
        int shift = Integer.BYTES*8;
        for (int i = 0; i < Integer.BYTES; i++) {
            shift -= 8;
            array[i+offset] = (byte) (x >> shift);
        }
    }

    public static void longToByte(long x, byte[] array, int offset) {
        int shift = Long.BYTES*8;
        for (int i = 0; i < Long.BYTES; i++) {
            shift -= 8;
            array[i+offset] = (byte) (x >> shift);
        }
    }

    public static int byteToInt(byte[] array, int offset) {
        int x = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            x <<= 8;
            x |= (int) array[i+offset] & 0xFF;
        }
        return x;
    }

    public static long byteToLong(byte[] array, int offset, int size) {
        long x = 0;
        for (int i = 0; i < size; i++) {
            x <<= 8;
            x |= (long) array[i+offset] & 0xFF;
        }
        return x;
    }
}
