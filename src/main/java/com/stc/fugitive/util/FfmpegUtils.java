package com.stc.fugitive.util;

import com.stc.fugitive.config.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author suntianci on 2022/9/24
 */
@Slf4j
public class FfmpegUtils {

    public static void main(String[] args) {
    }

    public static String getVideoTime_0() {
        //ffprobe -v quiet -select_streams v -show_entries stream=duration -of csv="p=0" /Users/suntianci/Downloads/wudalan/91videos/Youzhi_564822_母狗前女友.mp4

        //ffmpeg -i /Users/suntianci/Downloads/wudalan/91videos/Youzhi_564822_母狗前女友.mp4 2>&1 | grep 'Duration' | cut -d ' ' -f 4 | sed s/,//
        //ffprobe -v quiet -select_streams v -show_entries stream=duration -of csv="p=0" /Users/suntianci/Downloads/wudalan/91videos/Youzhi_564822_母狗前女友.mp4
        //ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 -i /Users/suntianci/Downloads/wudalan/91videos/Youzhi_564822_母狗前女友.mp4
        //ffprobe -v error -select_streams v:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 /Users/suntianci/Downloads/wudalan/91videos/Youzhi_564822_母狗前女友.mp4
        return "0";
    }

    public static BigDecimal calculateDurationRate(Integer ffmpegDurationSecond, Integer durationSecond) {
        if (ffmpegDurationSecond == null || ffmpegDurationSecond == 0) {
            return BigDecimal.ZERO;
        }
        if (durationSecond == null || durationSecond == 0) {
            return new BigDecimal(1000);
        }
        return new BigDecimal(ffmpegDurationSecond).multiply(new BigDecimal(100)).divide(new BigDecimal(durationSecond), 4, BigDecimal.ROUND_HALF_UP);
    }

    public static String convert(int duration) {
        int hour = duration / (60 * 60);
        int minute = (duration - hour * 60 * 60) / (60);
        int second = (duration - hour * 60 * 60 - minute * 60);
        if (hour == 0) {
            return (minute == 0 ? "00" : (minute > 10 ? minute : ("0" + minute))) + ":" + (second == 0 ? "00" : (second > 10 ? second : ("0" + second)));
        } else {
            return (hour == 0 ? "00" : (hour > 10 ? hour : ("0" + hour))) + ":" + (minute == 0 ? "00" : (minute > 10 ? minute : ("0" + minute))) + ":" + (second == 0 ? "00" : (second > 10 ? second : ("0" + second)));
        }
    }

    public static int convert(String duration) {
        if (StringUtils.isBlank(duration)) {
            return 0;
        }
        int millisecond = 0;
        String regexDuration1 = "(\\d*):(\\d*):(\\d*)";
        Pattern pattern1 = Pattern.compile(regexDuration1);
        Matcher matcher1 = pattern1.matcher(duration);

        String regexDuration2 = "(\\d*):(\\d*)";
        Pattern pattern2 = Pattern.compile(regexDuration2);
        Matcher matcher2 = pattern2.matcher(duration);

        if (matcher1.find()) {
            millisecond += NumberUtils.toInt(matcher1.group(1)) * 60 * 60;
            millisecond += NumberUtils.toInt(matcher1.group(2)) * 60;
            millisecond += NumberUtils.toInt(matcher1.group(3));
            System.out.println(duration + ",视频时长：" + matcher1.group(1) + "小时," + matcher1.group(2) + "分钟," + matcher1.group(3) + "秒。");
        } else if (matcher2.find()) {
            millisecond += NumberUtils.toInt(matcher2.group(1)) * 60;
            millisecond += NumberUtils.toInt(matcher2.group(2));
            System.out.println(duration + ",视频时长：" + matcher2.group(1) + "分钟," + matcher2.group(2) + "秒。");
        }
        return millisecond;
    }


    public static int convertVideoTime(String duration) {
        int millisecond = 0;
        String regexDuration = "(\\d*):(\\d*):(\\d*).(\\d*)";
        Pattern pattern = Pattern.compile(regexDuration);
        Matcher m = pattern.matcher(duration);
        if (m.find()) {
            millisecond += NumberUtils.toInt(m.group(1)) * 1000 * 60 * 60;
            millisecond += NumberUtils.toInt(m.group(2)) * 1000 * 60;
            millisecond += NumberUtils.toInt(m.group(3)) * 1000;
            millisecond += NumberUtils.toInt(m.group(4)) * 10;
            System.out.println(duration + ",视频时长：" + m.group(1) + "小时," + m.group(2) + "分钟," + m.group(3) + "秒," + m.group(4) + "0毫秒。");
        }
        return new BigDecimal(millisecond).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static int getVideoTimeSecond(String path, String fileName) {
        return getVideoTimeSecond(path + File.separator + fileName);
    }

    public static int getVideoTimeSecond(String videoPath) {
        if (StringUtils.isEmpty(videoPath)) {
            return 0;
        }
        String duration = "00:00:00.00";
        List<String> commands = new java.util.ArrayList<String>();
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(videoPath);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            final Process p = builder.start();

            //从输入流中读取视频信息
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            //从视频信息中解析时长
//            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (.*?)Stream";
            Pattern pattern = Pattern.compile(regexDuration);
            Matcher m = pattern.matcher(sb.toString());
            if (m.find()) {
//                System.out.println(video_path+",视频时长："+m.group(1)+", 开始时间："+m.group(2)+",比特率："+m.group(3)+"kb/s");
                System.out.println(videoPath + ",视频时长：" + m.group(1) + ", 开始时间：" + m.group(2) + ",比特率：" + m.group(3));
                duration = m.group(1);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }

        return convertVideoTime(duration);
    }
}
