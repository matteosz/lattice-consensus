package cs451.helper;

public class Operations {

    public static final int MASK = 0Xff;

    public static void fromIntegerToByte(int value, byte[] array, int offset) {
        array[offset] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte) value;
    }

    public static int fromByteToInteger(byte[] bytes, int offset) {
        return  ((bytes[offset] & MASK) << 24) |
                ((bytes[offset+1] & MASK) << 16) |
                ((bytes[offset+2] & MASK) << 8 ) |
                ((bytes[offset+3] & MASK));
    }

    private Operations() {}
}
