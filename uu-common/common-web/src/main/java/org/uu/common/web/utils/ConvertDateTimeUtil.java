package org.uu.common.web.utils;

import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ConvertDateTimeUtil {
    public static Map<String, LocalDateTime> getStartAndEndByLocalDate(LocalDate localDate, ZoneId zoneId) {
        Map<String, LocalDateTime> resultMap = Maps.newConcurrentMap();
        LocalDateTime localDateStartOfDay = localDate.atStartOfDay();
        localDateStartOfDay = convertLocalDateTime(localDateStartOfDay, ZoneId.systemDefault(), zoneId);
        resultMap.put("startTime", localDateStartOfDay);
        LocalDateTime localDateEndOfDay = localDate.plusDays(1).atStartOfDay().plusSeconds(-1);
        localDateEndOfDay = convertLocalDateTime(localDateEndOfDay, ZoneId.systemDefault(), zoneId);
        resultMap.put("endTime", localDateEndOfDay);
        return resultMap;
    }
    public static LocalDateTime getStartDateTimeByLocalDate(LocalDate localDate, ZoneId zoneId) {
        LocalDateTime localDateStartOfDay = localDate.atStartOfDay();
        return convertLocalDateTime(localDateStartOfDay, ZoneId.systemDefault(), zoneId);
    }

    public static LocalDateTime getEndDateTimeByLocalDate(LocalDate localDate, ZoneId zoneId) {
        LocalDateTime localDateTime = localDate.plusDays(1).atStartOfDay().plusSeconds(-1);
        return convertLocalDateTime(localDateTime, ZoneId.systemDefault(), zoneId);
    }

    /**
     * LocalDateTime时区转换
     *
     * @param localDateTime
     * @param originZoneId
     * @param targetZoneId
     */
    public static LocalDateTime convertLocalDateTime(LocalDateTime localDateTime, ZoneId originZoneId, ZoneId targetZoneId) {
        return localDateTime.atZone(originZoneId).withZoneSameInstant(targetZoneId).toLocalDateTime();
    }

    public static void main(String[] args) {
        String date = "2024-07-17";
        ZoneId zoneId = ZoneId.of("UTC+8");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, LocalDateTime> startAndEndByLocalDate = getStartAndEndByLocalDate(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")), zoneId);
        System.out.println("startTime::" + dateTimeFormatter.format(startAndEndByLocalDate.get("startTime")));
        System.out.println("endTime::" + dateTimeFormatter.format(startAndEndByLocalDate.get("endTime")));
    }
}
