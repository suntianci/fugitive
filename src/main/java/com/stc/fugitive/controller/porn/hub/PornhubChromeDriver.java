package com.stc.fugitive.controller.porn.hub;

import com.stc.fugitive.util.WebUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import us.codecraft.webmagic.selector.Html;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author suntianci on 2023/12/24
 */
public class PornhubChromeDriver {

    private static final String VIDEO_URL_PREFIX = "https://cn.pornhub.com";

    public static void main(String[] args) {
        Document document = getDocument("https://cn.pornhub.com/model/jijiaolian");

        Elements movies = document.select("div.mostRecentPornstarVideos > ul#modelMostRecentVideosSection > li.pcVideoListItem");
        for (Element movie : movies) {
            String duration = movie.select("a.linkVideoThumb>div.marker-overlays>var.duration").text();
            Elements aTag = movie.select("a.linkVideoThumb");
            String href = aTag.get(0).attr("href");
            String dataTitle = aTag.get(0).attr("data-title");
            Document document1 = getDocument(VIDEO_URL_PREFIX + href);

            System.out.println(movie);
        }
        System.out.println();


    }


    public static Document getDocument(String url) {
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


//        WebDriver webDriver = new ChromeDriver(options);
        WebDriver webDriver = new ChromeDriver();
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        webDriver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        webDriver.manage().window().maximize();

        webDriver.get(url);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        WebElement webElement = webDriver.findElement(By.xpath("/html"));

        String pageSource = webDriver.getPageSource();
        String content = webElement.getAttribute("outerHTML");

        Html html = new Html(content, url);
        Document document = html.getDocument();

        //webDriver.quit();//关闭所有窗口,关闭浏览器
        webDriver.close();//关闭当前窗口

        return document;
    }
}
