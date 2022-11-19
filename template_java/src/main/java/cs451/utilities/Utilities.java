package cs451.utilities;

public class Utilities {

    public static final int ARG_LIMIT_CONFIG = 7;

    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    public static final int CONFIG_VALUE = 6;

    public static final int PORT_MIN = 11000;
    public static final int PORT_MAX = 11999;
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
