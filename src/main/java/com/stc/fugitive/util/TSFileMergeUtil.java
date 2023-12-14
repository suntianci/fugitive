package com.stc.fugitive.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author suntianci on 2022/6/14
 */
@Slf4j
public class TSFileMergeUtil {
    public static void main(String[] args) {

        String sourcePath = "/Users/suntianci/Downloads/wudalan/459358";
        String targetPath = "/Users/suntianci/Downloads/638889_merge_RandomAccessFile.mp4";
        String targetPath2 = "/Users/suntianci/Downloads/638889_merge_FileOutputStream.mp4";
        String targetPath3 = "/Users/suntianci/Downloads/638889_merge_FileOutputStream3.mp4";
        try {
            merge(sourcePath, targetPath);
//            mergeMutilThread(sourcePath, targetPath);
//            mergeFile(sourcePath, targetPath2);
//            mergeFile2(sourcePath, targetPath3);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void mergeMutilThread(String sourcePath, String targetPath) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(targetPath), "rw");

        AtomicLong pos = new AtomicLong();
        for (File tempFile : getTempFileList(sourcePath)) {
            byte[] bytes = Files.readAllBytes(tempFile.toPath());
            new Thread(() -> {
                try {
                    randomAccessFile.seek(pos.get());
                    randomAccessFile.write(bytes);
                    pos.addAndGet(bytes.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            randomAccessFile.write(Files.readAllBytes(tempFile.toPath()));
        }
    }


    public static void mergeFile2(String sourcePath, String targetPath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(targetPath);
        byte[] buf = new byte[1024 * 1024];
        for (File tempFile : getTempFileList(sourcePath)) {
            InputStream inputStream = new FileInputStream(tempFile);
            int bufLen = 0;
            while ((bufLen = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, bufLen);
            }
        }
    }

    public static void mergeFile(String sourcePath, String targetPath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(targetPath);
        for (File tempFile : getTempFileList(sourcePath)) {
            fileOutputStream.write(Files.readAllBytes(tempFile.toPath()));
        }
    }

    public static void merge(String sourcePath, String targetPath) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(targetPath), "rw");
        for (File tempFile : getTempFileList(sourcePath)) {
            randomAccessFile.write(Files.readAllBytes(tempFile.toPath()));
        }
    }

    public static List<File> getTempFileList(String filePath) {
        List<File> tempFileList = new ArrayList<>();
        File sourceFile = new File(filePath);
        if (!sourceFile.isDirectory()) {
            return tempFileList;
        }
        File[] tempFiles = sourceFile.listFiles();
        for (File f : tempFiles) {
            tempFileList.add(f);
        }
        return tempFileList;
    }

}
