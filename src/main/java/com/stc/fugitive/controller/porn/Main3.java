package com.stc.fugitive.controller.porn;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @author suntianci on 2022/9/27
 */
@Slf4j
public class Main3 {
    public static void main(String[] args) {
        String sourceDirect = "/Users/suntianci/Downloads/wudalan/prod";
        String targetDirect = "/Volumes/P10/porn/prod2";
        File rootFile = new File(sourceDirect);
        Arrays.stream(rootFile.list()).forEach(file -> {
            File sourceFile = new File(sourceDirect, file);
            File targetFileRoot = new File(targetDirect);
            if (!targetFileRoot.exists()) {
                targetFileRoot.mkdirs();
            }
            File targetFile = new File(targetDirect, file);
            try {
//                copyFileUsingFileStreams(sourceFile,targetFile);
//                copyFileUsingFileChannels(sourceFile,targetFile);
//                copyFileUsingApacheCommonsIO(sourceFile,targetFile);
                copyFileUsingJava7Files(sourceFile,targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    //使用FileChannel复制
    //Java NIO包括transferFrom方法,根据文档应该比文件流复制的速度更快。
    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
    //使用Commons IO复制
    //这个类使用Java NIO FileChannel内部
    private static void copyFileUsingApacheCommonsIO(File source, File dest)
            throws IOException {
        FileUtils.copyFile(source, dest);
    }
    //使用Java7的Files类复制
    private static void copyFileUsingJava7Files(File source, File dest)
            throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }
}
