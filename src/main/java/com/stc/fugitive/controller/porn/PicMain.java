package com.stc.fugitive.controller.porn;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author suntianci on 2023/2/19
 */
public class PicMain {

    private static List<Map<String, Object>> FILE_LIST = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        distinct();
    }

    public static void distinct() throws IOException {
        listPic("/Users/suntianci/Downloads/91pic");
        FILE_LIST = FILE_LIST.stream().filter(m -> m != null).sorted(
                Comparator.comparing(PicMain::comparingByParentPath)
                        .thenComparing(Comparator.comparing(PicMain::comparingByModify))
        ).collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> md5HexMap = FILE_LIST.stream().collect(Collectors.groupingBy(
                map -> String.valueOf(map.get("md5Hex")
                )));
        List<List<Map<String, Object>>> collectRepeat = md5HexMap.values().stream().filter(list -> list.size() > 1).collect(Collectors.toList());
        for (List<Map<String, Object>> mapList : collectRepeat) {
            mapList = mapList.stream().sorted(
                    Comparator.comparing(PicMain::comparingByPath)
                            .thenComparing(Comparator.comparing(PicMain::comparingByModify).reversed())
            ).collect(Collectors.toList());
            String path = mapList.stream().map(m -> (String) m.get("path")).collect(Collectors.joining(","));
            System.out.println(path);
        }
        System.out.println();
    }


    public static void copy() throws IOException {
        listPic("/Users/suntianci/Downloads/temp");
        FILE_LIST = FILE_LIST.stream().filter(m -> m != null).sorted(
                Comparator.comparing(PicMain::comparingByParentPath)
                        .thenComparing(Comparator.comparing(PicMain::comparingByModify))
        ).collect(Collectors.toList());

        for (int i = 0; i < FILE_LIST.size(); i++) {
            String path = (String) FILE_LIST.get(i).get("path");
            File source = new File(path);
            String format = String.format("%05d", i + 19610);
            String fineName = String.format("IMG_%s." + FilenameUtils.getExtension(source.getPath()), format);
            File target = new File("/Users/suntianci/Downloads/91pic/" + fineName);
            copyFileUsingFileChannels(source, target);
            System.out.println(path + "  -->  " + fineName);
        }
        System.out.println();
    }


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


    private static String comparingByParentPath(Map<String, Object> map) {
        return (String) map.get("parentPath");
    }

    private static String comparingByPath(Map<String, Object> map) {
        return (String) map.get("path");
    }

    private static Long comparingByModify(Map<String, Object> map) {
        return (Long) map.get("lastModified");
    }

    public static List<String> listDirs(String dir) throws IOException {
        File file = new File(dir);
        File[] listFiles = file.listFiles();
        List<String> collect = Arrays.stream(listFiles).filter(Objects::nonNull).filter(f -> !".DS_Store".equals(f.getName())).sorted(Comparator.comparing(File::getPath).reversed()).map(File::getPath).collect(Collectors.toList());
        return collect;
    }

    public static void listPic(String dir) {
        if (StringUtils.isBlank(dir) || dir.endsWith(".DS_Store")) {
            return;
        }
        File fileParent = new File(dir);
        List<File> fileList = Arrays.stream(fileParent.listFiles()).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());

        fileList.parallelStream().forEach(
                file -> {
                    String name = file.getName();
                    if (StringUtils.isBlank(name) || ".DS_Store".equals(name)) {
                        return;
                    }
                    if (file.isFile()) {
                        Map<String, Object> map = new HashMap() {{
                            put("name", name);
                            put("length", file.length());
                            put("path", file.getPath());
                            put("parentName", file.getParentFile().getName());
                            put("parentPath", file.getParent());
                            long lastModified = file.lastModified();
                            put("lastModified", lastModified);
                            put("lastModifiedTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastModified)));
                            put("creationTime", readAttributes(file));
                            put("md5Hex", md5Hex(file));
                        }};
                        if (map != null) {
                            FILE_LIST.add(map);
                        }
                    }
                    System.out.println(dir + "文件个数：" + FILE_LIST.size());
                }
        );
    }

    public static void addFiles(String dir) throws IOException {
        File file = new File(dir);
        File[] listFiles = file.listFiles();
        List<File> collect = Arrays.stream(listFiles).sorted(Comparator.comparing(f -> f.getName())).collect(Collectors.toList());
        for (File listFile : collect) {
            String name = listFile.getName();
            if (StringUtils.isBlank(name) || ".DS_Store".equals(name)) {
                continue;
            }

            if (listFile.isFile()) {
                Map<String, Object> map = new HashMap() {{
                    put("name", name);
                    put("length", listFile.length());
                    put("path", listFile.getPath());
                    put("parentName", listFile.getParentFile().getName());
                    put("parentPath", listFile.getParent());
                    long lastModified = listFile.lastModified();
                    put("lastModified", lastModified);
                    put("lastModifiedTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastModified)));
                    put("creationTime", readAttributes(listFile));
                    put("md5Hex", md5Hex(listFile));
                }};
                FILE_LIST.add(map);
            } else {
                addFiles(listFile.getPath());
                System.out.println(listFile.getPath() + "文件个数：" + FILE_LIST.size());
            }
        }

    }

    public static String md5Hex(File file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            System.out.println("Exception md5Hex err");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    System.out.println("IOException md5Hex err");
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String readAttributes(File file) {
        BasicFileAttributes attr = null;
        try {
            Path path = file.toPath();
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 创建时间
        Instant instant = attr.creationTime().toInstant();
        Date date = new Date(instant.toEpochMilli());
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

}
