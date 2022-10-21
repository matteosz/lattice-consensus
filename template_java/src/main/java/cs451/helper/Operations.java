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

    public static int byteToInt(byte[] array, int offset) {
        int x = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            x <<= 8;
            x |= (int) array[i+offset] & 0xFF;
        }
        return x;
    }
}
