package com.minigit.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Sha1Utils {

    // 计算tree文件的hash值
    public static String calculateDirSha1(List<TreeEntry> treeEntries) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            StringBuilder sb = new StringBuilder();
            for (TreeEntry treeEntry : treeEntries) {
                sb.append(treeEntry.getEntryType()).append("\t").append(treeEntry.getPath())
                        .append("\t").append(treeEntry.getHash()).append("\n");
            }
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            // 更新SHA-1哈希算法的上下文状态，将实际数据更新到算法中
            sha1Digest.update(data);
            // 计算SHA-1哈希值，将结果写入到byteOut字节数组输出流中
            byteOut.write(sha1Digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 将SHA-1算法生成的二进制哈希值转换为16进制字符串返回
        return bytesToHex(byteOut.toByteArray());
    }

    // 计算blob文件的hash值
    public static String calculateFileSha1(File file) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            System.out.println(file.getAbsolutePath());
            System.out.println(file.getPath());
            System.out.println(Paths.get(file.getAbsolutePath()));
            byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            byte[] bytes = String.format("%s\t%d\t%s\u0000", "blob", data.length, file.getAbsolutePath()).getBytes(StandardCharsets.UTF_8);
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            // 更新SHA-1哈希算法的上下文状态，将type和数据长度拼接成的字节数组更新到算法中
            sha1Digest.update(bytes);
            // 更新SHA-1哈希算法的上下文状态，将实际数据更新到算法中
            sha1Digest.update(data);
            // 计算SHA-1哈希值，将结果写入到byteOut字节数组输出流中
            byteOut.write(sha1Digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 将SHA-1算法生成的二进制哈希值转换为16进制字符串返回
        return bytesToHex(byteOut.toByteArray());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
