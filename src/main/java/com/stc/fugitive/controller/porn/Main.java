package com.stc.fugitive.controller.porn;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author suntianci on 2022/9/27
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        String sourceDirect = "/Volumes/P10/porn/91porn_vidoeos_prod";
        String targetDirect = "/Volumes/P10/porn/91porn_vidoeos_prod_mac";
        File rootFile = new File(sourceDirect);
        Arrays.stream(rootFile.list()).forEach(subFile -> {
                    File subFileRoot = new File(sourceDirect, subFile);
                    List<String> filelist = Arrays.stream(subFileRoot.list()).filter(f -> f.endsWith(".mp4")).collect(Collectors.toList());
                    filelist.stream().forEach(file -> {
                        File sourceFile = new File(sourceDirect + File.separator + subFile, file);
                        File targetFileRoot = new File(targetDirect);
                        if (!targetFileRoot.exists()) {
                            targetFileRoot.mkdirs();
                        }
                        File targetFile = new File(targetDirect, file);
                        if (sourceFile.exists() && sourceFile.renameTo(targetFile)) {
                            log.info("成功复制：{}", sourceFile.getName());
                        } else {
                            log.info("失败复制：{}", sourceFile.getName());
                        }
                    });
                    subFileRoot = new File(sourceDirect, subFile);
                    if (subFileRoot == null || subFileRoot.list().length == 0) {
                        subFileRoot.delete();
                    }
                }
        );

    }
}
