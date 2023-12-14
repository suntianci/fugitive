package com.stc.fugitive.controller;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Crawler {

    private static final String url = "https://hsex.men/user-2.htm?author=khcheung";


    private static final String CHROME_DRIVER_PATH = "/Users/suntianci/Downloads/chromedriver-mac-x64/chromedriver";

    public static void main(String[] args) {

        Crawler crawler = new Crawler();
        WebDriver driver = crawler.open();
        crawler.pageTurning(driver);

//        driver.quit();
    }


    /**
     * 翻页
     */
    public void pageTurning(WebDriver driver) {
//        WebElement content = driver.findElement(By.cssSelector("body"));
//        List<WebElement> trs = driver.findElements(By.tagName("tr"));

//        WebElement content = driver.findElement(By.cssSelector("div.container > div.row > div.col-xs-6"));
        WebElement content = driver.findElement(By.cssSelector("div.container > div.row"));
        List<WebElement> elements = content.findElements(By.cssSelector("div.col-xs-6"));


        parseElement(content);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement pageTools = driver.findElement(By.className("page-tools-bottom"));
        WebElement paginationBox = pageTools.findElement(By.id("pagination_box"));
        //下一页的按钮
        WebElement next = paginationBox.findElement(By.className("next"));
        //按钮存在并且可点击
        if (null != next && next.isEnabled()) {
            //点击翻页
            next.click();
            System.out.println("点击下一页");
            try {
                //等待页面加载
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pageTurning(driver);
        }

    }


    public void parseElement(WebElement content) {
        List<WebElement> trs = content.findElements(By.tagName("tr"));
        System.out.println(trs.size());

        for (WebElement tr : trs) {
            List<WebElement> tds = tr.findElements(By.tagName("td"));

            WebElement td = tds.get(1);
            WebElement newsContent = td.findElement(By.className("news-content"));

            String id = newsContent.getAttribute("id");

            WebElement newsItem = newsContent.findElement(By.className("news-item"));

            String title = newsItem.findElement(By.className("profile-title")).findElement(By.className("ng-scope")).findElement(By.className("ng-binding")).getText();
            String summary = newsItem.findElement(By.className("news-item-title")).getText();

            WebElement newsItemTools = newsItem.findElement(By.className("news-item-tools"));

            WebElement mr5 = newsItemTools.findElement(By.className("mr5"));
            List<WebElement> industries = mr5.findElements(By.tagName("span")).get(1).findElements(By.tagName("div"));
            StringBuilder industry = new StringBuilder();
            if (!CollectionUtils.isEmpty(industries)) {
                for (WebElement ele : industries) {
                    industry.append(ele.getText()).append(",");
                }
                industry.deleteCharAt(industry.length() - 1);
            }

            WebElement mr10 = newsItemTools.findElement(By.className("mr10"));
            String addr = mr10.findElements(By.tagName("span")).get(1).findElement(By.tagName("div")).getText();

            WebElement relativeKeyword = newsItemTools.findElement(By.className("relative-keyword"));
            String keyword = relativeKeyword.findElements(By.tagName("span")).get(1).getText();

            WebElement btnGroup = newsItemTools.findElement(By.className("btn-group"));
            String url = btnGroup.findElements(By.className("inline-block")).get(3).findElement(By.tagName("a")).getAttribute("href");


            String source1 = tds.get(3).findElement(By.tagName("span")).getText();
            String source2 = tds.get(3).findElement(By.tagName("div")).getText();
            List<WebElement> timeEle = tds.get(4).findElements(By.tagName("span"));
            String time1 = timeEle.get(0).getText();
            String time2 = timeEle.get(1).getText();

            System.out.println("id：" + id);
            System.out.println("标题：" + title);
            System.out.println("摘要：" + summary);
            System.out.println("行业：" + industry.toString());
            System.out.println("地址：" + addr);
            System.out.println("关键字：" + keyword);

            System.out.println("来源：" + source1 + " " + source2);
            System.out.println("时间：" + time1 + " " + time2);

            System.out.println("源地址：" + url);

        }
    }

    public WebDriver open() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        driver.manage().window().maximize();


        driver.get(url);

        String cookies = "a=a;b=b;c=c";
        //为了绕过登录，在此处设置cookie信息
        if (StringUtils.isNotBlank(cookies)) {
            String[] cookieArr = cookies.split("\\;");
            for (String cookieStr : cookieArr) {
                if (StringUtils.isNotBlank(cookieStr)) {
                    cookieStr = cookieStr.trim();
                    String[] entry = cookieStr.split("\\=");
                    driver.manage().addCookie(new Cookie(entry[0].trim(), entry[1].trim()));
                }
            }
        }
        driver.get(url);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return driver;
    }

}
