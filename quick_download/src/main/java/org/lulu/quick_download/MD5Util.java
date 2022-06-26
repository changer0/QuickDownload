package org.lulu.quick_download;

import java.security.MessageDigest;

/**
 * author: changer0
 * date: 2022/6/26
 */
public class MD5Util {
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
