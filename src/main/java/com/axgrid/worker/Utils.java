package com.axgrid.worker;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Utils {

    static MessageDigest md;

    public static String md5(String text) {
        if (md == null) {
            try {
                md = MessageDigest.getInstance("MD5");
            }catch (NoSuchAlgorithmException ignore) {}
        }
        md.update(text.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    public static long crc32(String input) {
        byte[] bytes = input.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }

    public static int shardIndex(String key, int totalCount) {
        return (int)(crc32(md5(key)) % totalCount);
    }
}
