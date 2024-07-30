package org.uu.common.web.config.custom;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uu.common.core.constant.GlobalConstants;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * 重写Date类格式化方式 + 时区
 *
 * @author Parker
 * @since 2024-07-15
 */
@Slf4j
@Configuration
@SuppressWarnings("all")
public class CustomDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 362402284034797645L;

    public CustomDateFormat() {
        super();
    }

    private ZoneId zoneId = ZoneId.systemDefault();

    private String pattern;

    public CustomDateFormat(String pattern) {
        super(pattern);
        this.pattern = pattern;
    }

    public CustomDateFormat(String pattern, Locale locale) {
        super(pattern, locale);
        this.pattern = pattern;
    }

    public CustomDateFormat(String pattern, DateFormatSymbols dateFormatSymbols) {
        super(pattern, dateFormatSymbols);
        this.pattern = pattern;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (StringUtils.isEmpty(timeZone)) {
            if (Objects.isNull(date)) {
                return new StringBuffer();
            }
            return super.format(date, toAppendTo, pos);
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.pattern);
        return new StringBuffer(dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.of(timeZone))));
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        Date date = super.parse(text, pos);
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (StringUtils.isEmpty(timeZone)) {
            if (StringUtils.isEmpty(text)) {
                return null;
            }
            return date;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(text, dateTimeFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(timeZone)).withZoneSameInstant(zoneId);
        return Date.from(zonedDateTime.toInstant());
    }
}
