package com.stc.fugitive.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.stc.fugitive.config.exception.BusinessException;
import com.stc.fugitive.entity.PornMovie;
import com.stc.fugitive.service.PornMovieService;
import com.stc.fugitive.util.FfmpegUtils;
import com.stc.fugitive.util.HsexPornSpiderUtils;
import com.stc.fugitive.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.selector.Html;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author suntianci on 2022/6/27
 */
@Slf4j
@Service
public class HsexPornServiceImpl {

    @Autowired
    private PornMovieService pornMovieService;

    public static final String USER_INDEX_PAGE_URL = "https://hsex.men/user-%s.htm?author=%s";
    public static final String VIDEO_PAGE_URL = "https://hsex.men/list-%s.htm";

    public void downPornMovie(PornMovie pornMovie) {
        int videoId = pornMovie.getVideoId();
        String fileName = HsexPornSpiderUtils.getFileName(pornMovie.getAuthor(), videoId, pornMovie.getTitle());
        File fileMp4 = new File(HsexPornSpiderUtils.ROOT_PATH + File.separator + fileName);
        if (Arrays.stream("success_PASS".split(",")).collect(Collectors.toList()).contains(pornMovie.getStatus())) {
            return;
        }
        //todo finish success_DOWNLOAD_UNCOMPELETE
        if ("success_DOWNLOAD_UNCOMPELETE".equals(pornMovie.getStatus())) {
            if (fileMp4.exists()) {
                fileMp4.delete();
                HsexPornSpiderUtils.deleteFile(new File(HsexPornSpiderUtils.VIDEO_TEMP_PATH + File.separator + videoId));
            }

        }
        if (fileMp4.exists()) {
            log.info("文件{}已经合并完成，无需下载", fileMp4.getPath());
            pornMovie.setFileName(fileName);
            pornMovie.setDurationSecond(FfmpegUtils.convert(pornMovie.getDuration()));
            pornMovie.setFfmpegDurationSecond(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.ROOT_PATH, fileName));
            pornMovie.setFfmpegDuration(FfmpegUtils.convert(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.ROOT_PATH, fileName)));
            pornMovie.setDurationRate(FfmpegUtils.calculateDurationRate(pornMovie.getFfmpegDurationSecond(), pornMovie.getDurationSecond()));
            if (pornMovie.getDurationRate().compareTo(new BigDecimal("80")) >= 0) {
                pornMovie.setStatus("success_PASS");
                HsexPornSpiderUtils.deleteFile(new File(HsexPornSpiderUtils.VIDEO_TEMP_PATH + File.separator + videoId));
            } else {
                pornMovie.setStatus("success_DOWNLOAD_UNCOMPELETE");
            }
            pornMovieService.updateById(pornMovie);
            return;
        }
        log.info("{}处理中...", fileMp4.getPath());
        List<Integer> filelist = new ArrayList<>();
        List<Integer> filelistFail = new ArrayList<>();
        int tsId = 0;
        int responseCode = 200;
        while (true) {
            for (int i = 0; i < 3; i++) {
                responseCode = HsexPornSpiderUtils.down(videoId, tsId);
                if (responseCode == 200 || responseCode == 0) {
                    filelistFail.clear();
                    break;
                }
                log.error("下载片段失败videoId:{},tsId:{}，responseCode:{},重试:{}", videoId, tsId, responseCode, i);
            }
            if ((responseCode == 404 || responseCode == 502) && filelist.size() > 0) {//下载完成
                filelistFail.add(tsId);
                if (filelistFail.size() > 5) {
                    break;
                }
                tsId++;
                continue;
            }
            if (responseCode == 0) {//文件为空，不计入@可能第一个文件为空
                tsId++;
                continue;
            }
            if (responseCode != 200 && responseCode != 404 && responseCode != 502) {//错在错误500
                pornMovieService.update(new UpdateWrapper<PornMovie>().lambda().set(PornMovie::getStatus, "fail").eq(PornMovie::getVideoId, videoId));
                return;
            }
            filelist.add(tsId);
            tsId++;
        }
        HsexPornSpiderUtils.writeFile(filelist, videoId);//写入ffmpegComandFile.txt
        try {
            HsexPornSpiderUtils.runCommand(videoId, fileName);//执行聚合命令
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            if (fileMp4.exists()) {//合并完成 ，已经生成文件
                pornMovie.setDurationSecond(FfmpegUtils.convert(pornMovie.getDuration()));
                pornMovie.setFfmpegDurationSecond(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.ROOT_PATH, fileName));
                pornMovie.setFfmpegDuration(FfmpegUtils.convert(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.ROOT_PATH, fileName)));
                pornMovie.setDurationRate(FfmpegUtils.calculateDurationRate(pornMovie.getFfmpegDurationSecond(), pornMovie.getDurationSecond()));
                pornMovie.setFileName(fileName);
                pornMovie.setVideoLength(fileMp4.length());

                if (pornMovie.getDurationRate().compareTo(new BigDecimal("80")) >= 0) {
                    pornMovie.setStatus("success_PASS");
                    HsexPornSpiderUtils.deleteFile(new File(HsexPornSpiderUtils.VIDEO_TEMP_PATH + File.separator + videoId));
                    log.info("合并文件成功，生成文件：{}", fileName);
                } else {
                    pornMovie.setStatus("success_DOWNLOAD_UNCOMPELETE");
                }
                pornMovieService.updateById(pornMovie);
            } else {
                HsexPornSpiderUtils.writeCommand(videoId, fileName);//写入聚合命令
                pornMovieService.update(new UpdateWrapper<PornMovie>().lambda().set(PornMovie::getStatus, "fail").eq(PornMovie::getVideoId, videoId));
                log.info("合并文件失败，未生成文件：{}", fileName);
            }
        } catch (Exception e) {
            pornMovieService.update(new UpdateWrapper<PornMovie>().lambda().set(PornMovie::getStatus, "fail").eq(PornMovie::getVideoId, videoId));
            System.err.println("runCommand 文件合并错误！videoId=" + videoId);
            log.error(e.getMessage(), e);
        }

    }

    public void syncMovie() {//http://localhost:7085/pornUser/syncMovie
        ForkJoinPool forkJoinPool = new ForkJoinPool(5);
        try {
            List<Integer> list = new CopyOnWriteArrayList<>();
            List<Integer> listErr = new CopyOnWriteArrayList<>();
            forkJoinPool.submit(() -> IntStream.range(0, 3600).forEach(i -> {
                List<PornMovie> pornMovieList = getMovieByPage(i, HsexPornServiceImpl.VIDEO_PAGE_URL);
                if (CollectionUtils.isNotEmpty(pornMovieList)) {
                    list.add(i);
                    pornMovieService.saveOrUpdateMultiple(pornMovieList);
                    pornMovieList.stream().forEach(e -> HsexPornSpiderUtils.thumb(e.getVideoId()));
                } else {
                    listErr.add(i);
                }
            })).get();

            List<Integer> collect = list.stream().sorted().collect(Collectors.toList());
            List<Integer> collectErr = listErr.stream().sorted().collect(Collectors.toList());
            System.out.println(collectErr = listErr.stream().sorted().collect(Collectors.toList()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<PornMovie> getMovieByPage(int pageNo, String url) {
        log.info("处理第{}页，url={}", pageNo, url);
        List<PornMovie> pornMovieList = new ArrayList<>();
        Document document = null;
        try {
            document = getDocument(String.format(url, pageNo));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return pornMovieList;
        }
        Elements movies = document.select("div.container > div.row > div.col-xs-6");
        if (CollectionUtils.isEmpty(movies)) {
            return pornMovieList;
        }
        for (Element movieElement : movies) {
            try {
                PornMovie pornMovie = getMovieByUserIndex(movieElement);
                pornMovieList.add(pornMovie);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BusinessException("getMovieByUserIndex()转换视频错误");
            }
        }
        return pornMovieList;
    }

    public List<PornMovie> getMovieByAuthorUid() {
        List<PornMovie> pornMovieList = new ArrayList<>();
        try {
            List<String> htmlList = Files.readAllLines(Paths.get("/Users/suntianci/Downloads/wudalan/html.txt"), StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlList.stream().collect(Collectors.joining("\n")));
            if (document.html().contains("�")) {
                System.out.println();
            }
            Elements movies = document.select("div.container > div.row > div.col-xs-6");
            if (CollectionUtils.isEmpty(movies)) {
                return pornMovieList;
            }
            for (Element movieElement : movies) {
                try {
                    PornMovie pornMovie = getMovieByUserIndex(movieElement);
                    pornMovie.setAuthor(pornMovie.getAuthor());
                    pornMovieList.add(pornMovie);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new BusinessException("getMovieByUserIndex()转换视频错误");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pornMovieList;
    }

    public List<PornMovie> getMovieByAuthorUid(String author) {//syncFirstPage同步全部视频还是第一页
        int pageNo = 1;
        int pageSize = 24;
        List<PornMovie> pornMovieList = new ArrayList<>();
        List<Integer> errorPageNoList = new ArrayList<>();
        do {
//            String html = HsexPornSpiderUtils.read(String.format(USER_INDEX_PAGE_URL, pageNo, author));
//            Document document = Jsoup.parse(html);
            Document document = null;
            try {
                document = getDocument(String.format(USER_INDEX_PAGE_URL, pageNo, author));
            } catch (IOException e) {
                errorPageNoList.add(pageNo);
                log.error(e.getMessage(), e);
                continue;
            }
            if (document.html().contains("�")) {
                System.out.println();
            }

            Elements movies = document.select("div.container > div.row > div.col-xs-6");
            pageSize = movies.size();
            if (CollectionUtils.isEmpty(movies)) {
                break;
            }
            for (Element movieElement : movies) {
                try {
                    PornMovie pornMovie = getMovieByUserIndex(movieElement);
                    pornMovie.setAuthor(author);
                    pornMovieList.add(pornMovie);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new BusinessException("getMovieByUserIndex()转换视频错误");
                }
            }
            log.info("第{}页完成，pornMovieList={}", pageNo, pornMovieList.size());
            if (true) {
                pornMovieService.saveOrUpdateMultiple(pornMovieList);
                pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumb(e.getVideoId()));//处理缩略图
                pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumbmp4(e.getVideoId()));//处理缩略图
                pornMovieList.clear();
            }
            pageNo++;
        } while (pageSize == 24);
        System.out.println(errorPageNoList);
        return pornMovieList;
    }

    public Document getDocument(String urlHtml) throws IOException {
        if (true) {
            return getDocumentWM(urlHtml);
        }
        Document document = Jsoup.connect(urlHtml)
                .header("Accept-Charset", "utf-8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .userAgent("Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1")
                .timeout(10 * 1000)
                .post();
        return document;
    }

    //https://blog.csdn.net/eric520zenobia/article/details/113700334
    public Document getDocumentWM(String url) {
        System.setProperty("webdriver.chrome.driver", WebUtils.CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");//开启无头模式
        options.addArguments("--disable-gpu");//禁止gpu渲染
        options.addArguments("–-no-sandbox");//关闭沙盒模式
        options.addArguments("--disable-dev-shm-usage");

        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings", 2);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("blink-settings=imagesEnabled=false");//禁用图片

        options.addArguments("disable-features=NetworkService");
        options.addArguments("ignore-certificate-errors");
        options.addArguments("silent-launch");
        options.addArguments("disable-application-cache");
        options.addArguments("disable-web-security");
        options.addArguments("no-proxy-server");
        options.addArguments("disable-dev-shm-usage");
        options.addArguments("window-size=2048,1536");


        WebDriver webDriver = new ChromeDriver(options);
//        WebDriver webDriver = new ChromeDriver();
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        webDriver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        webDriver.manage().window().maximize();
        webDriver.get(url);
        String cookies = "a=a;b=b;c=c";
        //为了绕过登录，在此处设置cookie信息
        if (StringUtils.isNotBlank(cookies)) {
            String[] cookieArr = cookies.split("\\;");
            for (String cookieStr : cookieArr) {
                if (StringUtils.isNotBlank(cookieStr)) {
                    cookieStr = cookieStr.trim();
                    String[] entry = cookieStr.split("\\=");
                    webDriver.manage().addCookie(new Cookie(entry[0].trim(), entry[1].trim()));
                }
            }
        }
        webDriver.get(url);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        String content = webElement.getAttribute("outerHTML");

        Html html = new Html(content, url);
        Document document = html.getDocument();

        //webDriver.quit();//关闭所有窗口,关闭浏览器
        webDriver.close();//关闭当前窗口

        return document;
    }

    //用户信息页面提取视频
    public PornMovie getMovieByUserIndex(Element elementMovie) {
        String title = cleanString(elementMovie.select("div.thumbnail > div.caption h5 > a").text());
        String author = cleanString(elementMovie.select("div.thumbnail > div.info > p > a").text());
        String duration = cleanString(elementMovie.select("var.duration").text());
        Integer videoId = getVideoId(elementMovie);
        PornMovie pornMovie = PornMovie.builder()
                .title(title)
                .author(author)
                .duration(duration)
                .videoId(videoId)
                .build();
        convert(pornMovie, elementMovie.select("div.thumbnail > div.info p").text());
        log.info("扫描到了视频：{} ", pornMovie.toString());
        return pornMovie;
    }

    public Integer convert(PornMovie pornMovie, String data) {
        int viewCount = 0;
        data = data == null ? "" : data;
        data = cleanString(data.trim());
        List<String> list = Arrays.stream(data.split(" ")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        String viewCountStr = null;
        if (list.size() == 3) {
            pornMovie.setAddTime(list.get(2));
            viewCountStr = list.get(1).replaceAll("[\\u4e00-\\u9fa5]", "").replaceAll("�", "");
        } else if (StringUtils.isNotEmpty(pornMovie.getAuthor())) {
            list = Arrays.stream(data.replace(pornMovie.getAuthor(), "").split("次观看")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (list.size() == 2) {
                pornMovie.setAddTime(list.get(1));
                viewCountStr = list.get(0).replaceAll("[\\u4e00-\\u9fa5]", "").replaceAll("�", "");
            }
        }
        if (viewCountStr != null) {
            if (viewCountStr.contains("k")) {
                viewCountStr = viewCountStr.replace("k", "");
                try {
                    viewCount = (int) (new BigDecimal(viewCountStr).floatValue() * 1000);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else if (viewCountStr.contains("m")) {
                viewCountStr = viewCountStr.replace("m", "");
                viewCount = (int) (new BigDecimal(viewCountStr).floatValue() * 1000 * 1000);
            } else {
                viewCount = Integer.parseInt(viewCountStr);
            }
            pornMovie.setViewCount(viewCount);
        }
        return viewCount;
    }

    public Integer getVideoId(Element elementMovie) {
        Integer result = -1;
        try {
            String href = elementMovie.select("div.thumbnail > a").get(0).attr("href");
            int index = href.lastIndexOf("-");
            if (index < 0) {
                return result;
            }
            href = href.substring(index + 1);
            index = href.lastIndexOf(".");
            if (index < 0) {
                return result;
            }
            href = href.substring(0, index);
            result = NumberUtils.toInt(href, -1);
        } catch (Exception e) {
            log.error("获取VideoId错误");
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static String cleanString(String dirtyString) {
        return dirtyString.replaceAll("\\s*", "").replaceAll("&nbsp;", "").replaceAll("\\u00A0", "");
    }

    public int getMovieViewCountByVideoId(PornMovie pornMovie) {
        int viewCount = pornMovie.getViewCount();
        Document document = null;
        try {
            document = getDocument(String.format(HsexPornSpiderUtils.VIDEO_PAGE, pornMovie.getVideoId()));
            Element element = document.select("div.panel-body").get(0);
            String viewCountStr = cleanString(element.select("div.col-md-3").get(2).text());
            if (StringUtils.isEmpty(viewCountStr)) {
                return viewCount;
            }
            viewCountStr = viewCountStr.trim().replace("观看：", "");

            if (viewCountStr.endsWith("k")) {
                viewCountStr = viewCountStr.replace("k", "");
                try {
                    viewCount = (int) (new BigDecimal(viewCountStr).floatValue() * 1000);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else if (viewCountStr.endsWith("m")) {
                viewCountStr = viewCountStr.replace("m", "");
                viewCount = (int) (new BigDecimal(viewCountStr).floatValue() * 1000 * 1000);
            } else {
                viewCount = Integer.parseInt(viewCountStr);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }


        return viewCount;
    }


}
