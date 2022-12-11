package cs451.utilities;

/**
 * Utilities class used for converting
 * byte to int/long and vice-versa.
 */
public class Utilities {

    /** Mask used to extract the first 8 bytes through a bit-wise AND operation*/
    public static final int MASK = 0Xff;

    /** Long version of the mask */
    public static final long LONG_MASK = 0xff;

    /**
     * Fill a byte array starting from an offset with a
     * converted integer value.
     * @param value integer to be converted in bytes
     * @param array byte array where to store the value
     * @param offset in the byte array from where to start filling with value
     */
    public static void fromIntegerToByteArray(int value, byte[] array, int offset) {
        array[offset    ] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte)  value;
    }

    /**
     * Convert a byte array starting from an offset into
     * an integer value.
     * @param bytes byte array containing the value
     * @param offset in the byte array from where to start retrieving the value
     * @return converted integer
     */
    public static int fromByteToIntegerArray(byte[] bytes, int offset) {
        return  ((bytes[offset  ] & MASK) << 24) |
                ((bytes[offset+1] & MASK) << 16) |
                ((bytes[offset+2] & MASK) << 8 ) |
                ((bytes[offset+3] & MASK));
    }

    /**
     * Long version of fromIntegerToByteArray.
     * @param value integer to be converted in bytes
     * @param array byte array where to store the value
     * @param offset in the byte array from where to start filling with value
     */
    public static void fromLongToByteArray(long value, byte[] array, int offset) {
        array[offset    ] = (byte) (value >>> 56);
        array[offset + 1] = (byte) (value >>> 48);
        array[offset + 2] = (byte) (value >>> 40);
        array[offset + 3] = (byte) (value >>> 32);
        array[offset + 4] = (byte) (value >>> 24);
        array[offset + 5] = (byte) (value >>> 16);
        array[offset + 6] = (byte) (value >>> 8);
        array[offset + 7] = (byte)  value;
    }

    /**
     * Long version of fromByteToIntegerArray.
     * @param bytes byte array containing the value
     * @param offset in the byte array from where to start retrieving the value
     * @return converted integer
     */
    public static long fromByteToLongArray(byte[] bytes, int offset) {
        return  ((bytes[offset  ] & LONG_MASK) << 56) |
                ((bytes[offset+1] & LONG_MASK) << 48) |
                ((bytes[offset+2] & LONG_MASK) << 40) |
                ((bytes[offset+3] & LONG_MASK) << 32) |
                ((bytes[offset+4] & LONG_MASK) << 24) |
                ((bytes[offset+5] & LONG_MASK) << 16) |
                ((bytes[offset+6] & LONG_MASK) << 8 ) |
                ((bytes[offset+7] & LONG_MASK));
    }

    /**
     * The following function converts an integer into byte
     * assuming: Integer in range [1, 128] -> Byte in range [0, 127].
     * @param value initial integer value
     * @return byte conversion scaled of 1 unit
     */
    public static byte fromIntegerToByte(int value) {
        return (byte) (value - 1);
    }

}
