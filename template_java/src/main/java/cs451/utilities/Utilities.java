package cs451.utilities;

public class Utilities {

    public static final int MASK = 0Xff;

    public static void fromIntegerToByteArray(int value, byte[] array, int offset) {
        array[offset    ] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte)  value;
    }

    public static int fromByteToIntegerArray(byte[] bytes, int offset) {
        return  ((bytes[offset  ] & MASK) << 24) |
                ((bytes[offset+1] & MASK) << 16) |
                ((bytes[offset+2] & MASK) << 8 ) |
                ((bytes[offset+3] & MASK));
    }

    /**
     * The two following functions assume:
     * Integer in range [1, 128] -> Byte in range [0, 127]
     */
    public static int fromByteToInteger(byte value) {
        return value + 1;
    }
    public static byte fromIntegerToByte(int value) {
        return (byte) (value - 1);
    }

    private Utilities() {}
}
