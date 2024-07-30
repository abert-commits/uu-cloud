package org.uu.common.web.config.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.uu.common.core.constant.GlobalConstants;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Admin
 */
@Slf4j
@Configuration
@SuppressWarnings("all")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CustomLocalDateTimeFormatConfig implements WebMvcConfigurer {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Resource
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        //取消时间的转化格式，默认是时间戳,同时需要设置要表现的时间格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        // 解决客户端传入后端未定义接收的参数
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        objectMapper.setDateFormat(new CustomDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();   // 默认序列化没有实现，反序列化有实现
        javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        // 设置时区
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.registerModule(javaTimeModule);

        if (mappingJackson2HttpMessageConverter == null) {
            mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        }
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        converters.add(0, mappingJackson2HttpMessageConverter);
    }

    /**
     * 时区转换
     *
     * @param localDateTime
     * @param originZoneId
     * @param targetZoneId
     * @return
     */
    public static LocalDateTime convertLocalDateTime(LocalDateTime localDateTime, ZoneId originZoneId, ZoneId targetZoneId) {
        return localDateTime.atZone(originZoneId).withZoneSameInstant(targetZoneId).toLocalDateTime();
    }

    /**
     * LocalDateTime序列化
     */
    public static class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        private final DateTimeFormatter formatter;

        public CustomLocalDateTimeSerializer(DateTimeFormatter formatter) {
            super();
            this.formatter = formatter;
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator generator, SerializerProvider provider)
                throws IOException {
            String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
            if (Objects.isNull(value)) {
                value = null;
                return;
            }
            if (timeZone != null) {
                generator.writeString(convertLocalDateTime(value, ZoneId.systemDefault(), ZoneId.of(timeZone))
                        .format(formatter));
            } else {
                generator.writeString(convertLocalDateTime(value, ZoneId.systemDefault(), ZoneId.systemDefault())
                        .format(formatter));
            }

        }

    }

    /**
     * LocalDateTime反序列化
     */
    public static class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter;

        public CustomLocalDateTimeDeserializer(DateTimeFormatter formatter) {
            super();
            this.formatter = formatter;
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {

            String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
            String parserText = parser.getText();
            if (Objects.isNull(parserText) || StringUtils.isEmpty(parserText) || StringUtils.isEmpty(parserText.trim())) {
                return null;
            }

            if (timeZone != null) {
                return convertLocalDateTime(LocalDateTime.parse(parser.getText(), formatter), ZoneId.of(timeZone),
                        ZoneId.systemDefault());
            } else {
                return convertLocalDateTime(LocalDateTime.parse(parser.getText(), formatter), ZoneId.systemDefault(),
                        ZoneId.systemDefault());
            }

        }
    }
}