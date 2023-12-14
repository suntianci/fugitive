package com.stc.fugitive.controller.porn;

import com.stc.fugitive.util.HsexPornSpiderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author suntianci on 2022/11/8
 */
@Slf4j
public class PornhubDownload {

    public static final String VIDEO_PATH = "/Users/suntianci/Downloads/wudalan/pornhub";
    public static final String VIDEO_TEMP_PATH = "/Users/suntianci/Downloads/wudalan/pornhub/temp";

    public static void main(String[] args) {
        String videoUrl = "https://ev-h.phncdn.com/hls/videos/202303/24/428076821/,1080P_4000K,720P_4000K,480P_2000K,240P_1000K,_428076821.mp4.urlset/seg-3-f1-v1-a1.ts?validfrom=1687183741&validto=1687190941&ipa=220.246.88.186&hdl=-1&hash=mC9THvwsC7YW8o1nIYjjOS3voL4%3D&";

        String author = "jijiaolian";
        String viewkey = "641d700d2a828";
        String videoName = "淘气的青梅竹马来我家借住，可爱的外表下竟然小恶魔属性全线拉满，满脑子都是色色的事情";

        download(videoUrl, author, viewkey, videoName);
    }

    public static void download(String videoUrl, String author, String viewkey, String videoName) {
        String fileName = String.join("_", author, viewkey, videoName).replaceAll(" ", "-") + ".mp4";

        List<String> tsFilelist = new ArrayList<>();
        boolean flag = false;
        int i = 1;
        while (true) {
            String tsFileName = String.format("seg-%s-f2-v1-a1.ts", i);
            String realUrl = videoUrl.replaceAll("seg-[\\d]{1,3}-f1-v1-a1.ts", tsFileName);
            int responseCode = down(tsFileName, realUrl, author, viewkey);

            if (responseCode == 200) {
                tsFilelist.add(tsFileName);
                flag = true;
            } else if (responseCode == 404) {
                log.error("下载片段失败tsFileName:{},responseCode:{}", tsFileName, responseCode);
                flag = true;
                break;
            } else if (responseCode == 472) {
                log.error("认证失败tsFileName:{},responseCode:{}", tsFileName, responseCode);
                flag = false;
                break;
            } else {
                log.error("未知错误tsFileName:{},responseCode:{}", tsFileName, responseCode);
                flag = false;
                break;
            }
            i++;
        }
        if (flag) {
            writeFile(tsFilelist, author, viewkey);
            String targetPath = String.format("%s/%s", VIDEO_PATH, fileName);
            File file = new File(targetPath);
            if (!file.exists()) {
                String command = String.format("ffmpeg -f concat -i %s/%s/ffmpegComandFile.txt -c copy '%s'", VIDEO_PATH, String.join("_", author, viewkey).replaceAll(" ", "-").replaceAll(" ", ""), targetPath);
                log.info("merge command:{}", command);
                runCommand(command);
            }
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                HsexPornSpiderUtils.deleteFile(new File(VIDEO_PATH + File.separator + String.join("_", author, viewkey).replaceAll(" ", "-").replaceAll(" ", "")));
            }
        }
    }

    public static void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            System.out.println(String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void writeFile(List<String> filelist, String author, String viewkey) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(VIDEO_PATH + File.separator + String.join("_", author, viewkey).replaceAll(" ", "-").replaceAll(" ", "") + File.separator + "ffmpegComandFile.txt", false);
            for (String tsName : filelist) {
                String str = String.format("file '%s'\n", tsName);
                fileOutputStream.write(str.getBytes());
            }
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static int down(String tsFileName, String realUrl, String author, String viewkey) {
        int responseCode = 200;
        File file = new File(VIDEO_PATH + File.separator + String.join("_", author, viewkey).replaceAll(" ", "-").replaceAll(" ", ""));
        if (!file.exists()) {
            file.mkdirs();
        }
//        file = new File(VIDEO_TEMP_PATH + File.separator + String.join("_", author, viewkey).replaceAll(" ", "-").replaceAll(" ", "") + File.separator + tsFileName);
        file = new File(file, tsFileName);
        if (file.exists() && file.length() > 0) {
            log.info("文件{}分块已经下载完成，无需下载", file.getPath());
            return responseCode;
        }
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(realUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            responseCode = httpURLConnection.getResponseCode();
            log.info("responseCode={},path={},url={}", responseCode, file.getPath(), url);
            if (responseCode != 200) {
                return responseCode;
            }
            inputStream = httpURLConnection.getInputStream();
            outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            log.info("Exception tsFileName:{},realUrl:{}", tsFileName, realUrl);
            log.error(e.getMessage(), e);
            if (StringUtils.equals(e.getMessage(), "Read timed out")) {
                responseCode = 404;
            } else {
                responseCode = 500;
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return responseCode;
    }
}
