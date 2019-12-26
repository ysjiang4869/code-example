package org.jys.example.common.utils;


/**
 * @author YueSong Jiang
 * @date 2019/1/12
 * common tools for byte operation
 */
public class ByteUtils {

    /**
     * return int form byte array based big endian
     *
     * @param bytes byte array
     * @return int value
     */
    public static int getIntBigEndian(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
    }

    /**
     * get byte array of int value based on big endian
     *
     * @param value int value
     * @return byte array
     */
    public static byte[] toBytesBigEndian(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) (value)};
    }

    /**
     * get last index of target array for array
     * such as array is [1,3,4,6,7,9] , target is [6.7]
     * it will return 3
     *
     * @param array  the array
     * @param target the array need to find
     * @return target array last start index
     */
    public static int lastIndexOf(byte[] array, byte[] target) {
        int targetLen = target.length;
        for (int i = array.length - 1; i >= targetLen - 1; i--) {
            boolean found = true;
            int tmp = i - targetLen + 1;
            for (int j = targetLen - 1; j >= 0; j--) {
                if (array[tmp + j] != target[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i - targetLen + 1;
            }
        }
        return -1;
    }
}
