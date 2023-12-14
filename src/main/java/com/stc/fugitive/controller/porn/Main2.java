package com.stc.fugitive.controller.porn;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;

/**
 * @author suntianci on 2022/9/27
 */
@Slf4j
public class Main2 {
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
            if (sourceFile.exists() && sourceFile.renameTo(targetFile)) {
                log.info("成功复制：{}", sourceFile.getName());
            } else {
                log.info("失败复制：{}", sourceFile.getName());
            }
        });
    }
}
