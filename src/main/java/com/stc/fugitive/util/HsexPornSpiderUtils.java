package com.stc.fugitive.util;

import com.stc.fugitive.config.exception.BusinessException;
import com.stc.fugitive.test.porn.ProcessMain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author suntianci on 2022/6/9
 */
@Component
@Slf4j
public class HsexPornSpiderUtils {

    @Autowired
    private RestTemplate restTemplate;

    public static int timeoutMillisecond = 10 * 1000;
    public static final String URL_PREFIX = "https://cdn.bigcloud.click/hls/%s/index%s.ts";
    public static final String THUMB_URL = "https://img.bigcloud.click/thumb/%s.webp";
    public static final String THUMB_MP4_URL = "https://vthumb.killcovid2021.com/thumb/%s.mp4";
    public static final String ROOT_PATH = "/Users/suntianci/Downloads/wudalan/91videos";
    public static final String VIDEO_TEMP_PATH = "/Users/suntianci/Downloads/wudalan/91videos_temp";
    public static final String ROOT_THUMB_PATH = "/Users/suntianci/Downloads/wudalan/thumb/";
    public static final String ROOT_THUMB_MP4_PATH = "/Users/suntianci/Downloads/wudalan/thumb/mp4/";

    public static final String DISK_PORN_ROOT_PATH = "/Volumes/P10/porn/91porn_vidoeos_prod";

    public static final String VIDEO_PAGE = "https://hsex.men/video-%s.htm";

    public static void writeFile(List<Integer> filelist, Integer videoId) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(VIDEO_TEMP_PATH + File.separator + videoId + File.separator + "ffmpegComandFile.txt", false);
            for (Integer id : filelist) {
                String str = String.format("file '%s'\n", (videoId + "_" + Integer.toString(id) + ".ts"));
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

    public static void runCommand(Integer videoId, String fileName) {
        String targetPath = String.format("%s/%s", ROOT_PATH, fileName);
        File file = new File(targetPath);
//        if (!file.exists()) {
            String command = String.format("ffmpeg -f concat -i %s/%s/ffmpegComandFile.txt -c copy '%s' -y", VIDEO_TEMP_PATH, videoId, targetPath);
            log.info("merge command:{}", command);
            ProcessMain.runCommand(command);
//        }
    }

    public static String getFileName(String author, Integer videoId, String title) {
        title = StringUtils.isBlank(title) ? "" : title;
        author = StringUtils.isBlank(author) ? "" : author.replaceAll("/", "-");
        String regEx = "[?*:\"<>\\/|]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(title);
        title = matcher.replaceAll("-");
        title = title.replaceAll("\\\\", "-");
        return String.join("_", author, Integer.toString(videoId), title) + ".mp4";
    }

    public static void main(String[] args) {
        String title = "你好?*:\"<>\\/|";
        String regEx = "[?*:\"<>\\/|]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(title);
        title = matcher.replaceAll("-");
        title = title.replaceAll("\\\\", "-");
        System.out.println(title);
    }

    public synchronized static void writeCommand(Integer videoId, String fileName) {
        String targetPath = String.format("%s/%s", ROOT_PATH, fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(ROOT_PATH + File.separator + "command.txt", true);
            String command = String.format("ffmpeg -f concat -i %s/%s/ffmpegComandFile.txt -c copy %s\n", VIDEO_TEMP_PATH, videoId, targetPath);
            fileOutputStream.write(command.getBytes());
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

    public static int down(Integer videoId, Integer tsId) {
        int responseCode = 200;
        File file = new File(VIDEO_TEMP_PATH + File.separator + videoId);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(VIDEO_TEMP_PATH + File.separator + videoId + File.separator + videoId + "_" + Integer.toString(tsId) + ".ts");
        if (file.exists() && file.length() > 0) {
            log.info("文件{}分块已经下载完成，无需下载", file.getPath());
            return responseCode;
        }
        log.info("开始下载videoId:{},tsId:{}", videoId, tsId);
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            URL url = new URL(String.format(URL_PREFIX, videoId, tsId));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(timeoutMillisecond);
            httpURLConnection.setReadTimeout(timeoutMillisecond);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            responseCode = httpURLConnection.getResponseCode();
            log.info("responseCode={},path={},url={}", responseCode, file.getPath(), url);
            if (responseCode != 200) {
                return responseCode;
            }
            inputStream = httpURLConnection.getInputStream();
            if (inputStream.available() == 0) {
                return 0;
            }
            outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error("Exception url={},path={},", String.format(URL_PREFIX, videoId, tsId), file.getPath());
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

    public static boolean thumb(Integer videoId) {
        if (videoId == null) {
            return false;
        }
        log.info("开始下载thumb videoId:{}", videoId);
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String filePath = ROOT_THUMB_PATH + videoId + ".jpg";
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                log.info("文件{}已经下载完成，无需下载", filePath);
                return true;
            }
            URL url = new URL(String.format(THUMB_URL, videoId));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1");

            httpURLConnection.setConnectTimeout(timeoutMillisecond);
            httpURLConnection.setReadTimeout(timeoutMillisecond);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            int responseCode = httpURLConnection.getResponseCode();
            log.info("url={},responseCode={},path={}", url, responseCode, filePath);
            if (responseCode == 404) {
                return false;
            }
            inputStream = httpURLConnection.getInputStream();
            outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (StringUtils.equals("Read timed out", e.getMessage())) {
                return false;
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
        return true;
    }

    public boolean thumbmp4ByRestTemplate(Integer videoId) {
        if (videoId == null) {
            return false;
        }
        log.info("开始下载thumb videoId:{}", videoId);
        try {
            String filePath = ROOT_THUMB_MP4_PATH + videoId + ".mp4";
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                log.info("文件{}已经下载完成，无需下载", filePath);
                return true;
            }
            ResponseEntity<byte[]> rsp = restTemplate.getForEntity(String.format(THUMB_MP4_URL, videoId), byte[].class);
            if (rsp.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Files.write(Paths.get(filePath), Objects.requireNonNull(rsp.getBody(), "未获取到下载文件"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {

        }
        return true;
    }

    public static boolean thumbmp4(Integer videoId) {
        if (videoId == null) {
            return false;
        }
        log.info("开始下载thumb videoId:{}", videoId);
        HttpURLConnection httpURLConnection = null;
        try {
            HttpsUrlValidator.trustAllHttpsCertificates();
        } catch (Exception e) {
            return false;
        }
        HttpsURLConnection.setDefaultHostnameVerifier(HttpsUrlValidator.hv);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String filePath = ROOT_THUMB_MP4_PATH + videoId + ".mp4";
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                log.info("文件{}已经下载完成，无需下载", filePath);
                return true;
            }
            URL url = new URL(String.format(THUMB_MP4_URL, videoId));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            httpURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1");


            httpURLConnection.setConnectTimeout(timeoutMillisecond);
            httpURLConnection.setReadTimeout(timeoutMillisecond);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            int responseCode = httpURLConnection.getResponseCode();
            log.info("url={},responseCode={},path={}", url, responseCode, filePath);
            if (responseCode != 200) {
                return false;
            }
            inputStream = httpURLConnection.getInputStream();
            outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            log.info("success url={},responseCode={},path={}", url, responseCode, filePath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (StringUtils.equals("Read timed out", e.getMessage())) {
                return false;
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
        return true;
    }

    public static String read(String urls) {
        String result = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urls);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1");
            httpURLConnection.setConnectTimeout(timeoutMillisecond);
            httpURLConnection.setReadTimeout(timeoutMillisecond);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);

            int responseCode = httpURLConnection.getResponseCode();
            log.info("开始读取:{},ResponseCode:{}", urls, responseCode);
            if (responseCode != 200) {
                throw new BusinessException("responseCode=" + responseCode);
            }

            inputStream = httpURLConnection.getInputStream();
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                result += new String(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(urls + "下载错误");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return result;
    }

    /**
     * 先根遍历序递归删除文件夹
     *
     * @param dirFile 要被删除的文件或者目录
     * @return 删除成功返回true, 否则返回false
     */
    public static boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }
        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

}
