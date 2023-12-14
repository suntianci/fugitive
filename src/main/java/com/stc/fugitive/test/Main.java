package com.stc.fugitive.test;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author suntianci on 2022/6/28
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        String data = "   squirtyu   487次观看  15小时前";
        data += "   squirtyu   1.1k次观看  4月前";

//        Pattern pattern = Pattern.compile("\\d+次观看");
//        Pattern pattern = Pattern.compile("[1-9]\\d{0,2}\\.\\dk次观看");
        Pattern pattern = Pattern.compile("(\\d+次观看)|([1-9]\\d{0,2}\\.\\dk次观看)");
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            String sub = data.substring(matcher.start(), matcher.end());
            System.out.println(sub);
        }


        Date before = new Date();
        TimeUnit.SECONDS.sleep(1);
        Date after = new Date();
        System.out.println(before.compareTo(after));
        System.out.println(before.before(after));

        String str = "{\"ip\":\"61.157.14.61\",\"pro\":\"四川省\",\"proCode\":\"510000\",\"city\":\"成都市\",\"cityCode\":\"510100\",\"region\":\"\",\"regionCode\":\"0\",\"addr\":\"四川省成都市 电信\",\"regionNames\":\"\",\"err\":\"\"}\n";
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);
        String result = jsonObject.get("pro2") == null ? null : String.valueOf(jsonObject.get("pro2"));
        System.out.println(result);


    }
}
