package org.jys.example.common.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public final class ByteUtil {
    public static String toHex(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            StringBuilder buff = new StringBuilder(bytes.length << 1);
            String tmp;
            for (byte aByte : bytes) {
                tmp = (Integer.toHexString(aByte & 0xFF));
                if (tmp.length() == 1) {
                    buff.append('0');
                }
                buff.append(tmp);
            }
            return buff.toString();
        }

        return null;
    }

    public static byte[] fromHex(String hex) {
        if (hex != null && hex.length() > 1) {
            try {
                byte[] bytes = new byte[hex.length() / 2];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) Integer.parseInt(hex.substring(i << 1, (i << 1) + 2), 16);
                }
                return bytes;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static byte[] getBytes(String s) {
        if (s != null) {
            try {
                return s.getBytes(Charset.defaultCharset());
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    public static Collection<byte[]> getBytes(Collection<String> ss) {
        Collection<byte[]> rv = new ArrayList<byte[]>(ss.size());
        for (String s : ss) {
            rv.add(getBytes(s));
        }
        return rv;
    }

    public static byte[] fromInt(int res) {
        byte[] targets = new byte[4];
        // 最低位
        targets[0] = (byte) (res & 0xff);
        // 次低位
        targets[1] = (byte) ((res >> 8) & 0xff);
        // 次高位
        targets[2] = (byte) ((res >> 16) & 0xff);
        // 最高位,无符号右移。
        targets[3] = (byte) (res >>> 24);
        return targets;
    }

    public static int toInt(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }
}