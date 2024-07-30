package org.uu.common.core.utils;


import java.time.Duration;
import java.time.LocalDateTime;

public class DurationCalculatorUtil {

    /**
     * 计算订单完成时长
     */
    public static String orderCompleteDuration(LocalDateTime start, LocalDateTime end) {

        // 计算完成时长
        Duration duration = Duration.between(start, end);

        //得到相差的秒数
        long totalSeconds = duration.getSeconds();
        return getOrderCompleteDuration(String.valueOf(totalSeconds));
    }

    public static String getOrderCompleteDuration(String secondsProcess) {
        long totalSeconds = Long.parseLong(secondsProcess);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        //格式化处理
        String formatHours = (hours < 10) ? "0" + hours : String.valueOf(hours);
        String formatMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String formatSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);
        String formatTime = "";
        if(Integer.parseInt(formatHours) != 0){
            formatTime = formatTime + formatHours + "小时";
        }
        if(Integer.parseInt(formatMinutes) != 0){
            formatTime = formatTime + formatMinutes + "分钟";
        }
        formatTime = formatTime + formatSeconds;
        return formatTime + "秒";
    }


    /**
     * 计算两个 LocalDateTime 之间相差的秒数
     */

    public static String secondsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return String.valueOf(Duration.between(startDateTime, endDateTime).getSeconds());
    }


}
