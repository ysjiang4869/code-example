package org.jys.example.common.utils;

import java.util.BitSet;

public final class BitUtil {
    private final static int[] INTEGER_MASKS = new int[32];
    private final static long[] LONG_MASKS = new long[64];

    static {
        for (int i = 0; i < 32; i++) {
            INTEGER_MASKS[i] = (1 << i);
        }
    }

    static {
        for (int i = 0; i < 64; i++) {
            LONG_MASKS[i] = (1L << i);
        }
    }

    public static boolean is(int value, int index) {
        return index > -1 && index < 32 && (value & INTEGER_MASKS[index]) != 0;
    }

    public static int set(int value, int index) {
        return index > -1 && index < 32 ? value | INTEGER_MASKS[index] : value;
    }

    public static int clear(int value, int index) {
        return index > -1 && index < 32 ? value & (~INTEGER_MASKS[index]) : value;
    }

    public static boolean is(long value, int index) {
        return index > -1 && index < 64 && (value & LONG_MASKS[index]) != 0;
    }

    public static long set(long value, int index) {
        return index > -1 && index < 64 ? value | LONG_MASKS[index] : value;
    }

    public static long clear(long value, int index) {
        return index > -1 && index < 64 ? value & (~LONG_MASKS[index]) : value;
    }

    public static BitSet hexToBitSet(String data) {
        if (data != null && data.length() > 0) {
            return BitSet.valueOf(ByteUtil.fromHex(data));
        }

        return new BitSet();
    }

    public static String bitSetToHex(BitSet data) {
        if (data != null) {
            return ByteUtil.toHex(data.toByteArray());
        }
        return "";
    }
}