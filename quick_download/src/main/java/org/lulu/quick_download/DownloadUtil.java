package org.lulu.quick_download;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * author: changer0
 * date: 2022/6/25
 */
public class DownloadUtil {
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSHA256(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("SHA-256");
            return bytes2Hex(md5.digest(str.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp;
        for (byte bt : bts) {
            tmp = (Integer.toHexString(bt & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
