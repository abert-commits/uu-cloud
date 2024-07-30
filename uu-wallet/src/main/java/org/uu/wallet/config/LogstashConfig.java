//package org.uu.wallet.config;
//
//import ch.qos.logback.classic.Logger;
//import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
//import ch.qos.logback.core.net.ssl.SSLConfiguration;
//import net.logstash.logback.appender.LogstashTcpSocketAppender;
//import net.logstash.logback.encoder.LogstashEncoder;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConfigurationProperties(prefix = "logging.logstash")
//public class LogstashConfig {
//
//    private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";
//    private String url = "8.222.160.178:14560";
//    private String trustStoreLocation;
//
//    private String trustStorePassword;
//
//    @Value("${spring.application.name:-}")
//    String name;
//
//    @Bean
//    public LogstashTcpSocketAppender logstashAppender() {
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        LogstashTcpSocketAppender logstashTcpSocketAppender = new LogstashTcpSocketAppender();
//        logstashTcpSocketAppender.setName(LOGSTASH_APPENDER_NAME);
//        logstashTcpSocketAppender.setContext(loggerContext);
//        logstashTcpSocketAppender.addDestination(url);
//        if (trustStoreLocation != null) {
//            SSLConfiguration sslConfiguration = new SSLConfiguration();
//            KeyStoreFactoryBean factory = new KeyStoreFactoryBean();
//            factory.setLocation(trustStoreLocation);
//            if (trustStorePassword != null)
//                factory.setPassword(trustStorePassword);
//            sslConfiguration.setTrustStore(factory);
//            logstashTcpSocketAppender.setSsl(sslConfiguration);
//        }
//        LogstashEncoder encoder = new LogstashEncoder();
//        encoder.setContext(loggerContext);
//        encoder.setIncludeContext(true);
//        encoder.setCustomFields("{\"appname\":\"" + name + "\"}");
//        encoder.start();
//        logstashTcpSocketAppender.setEncoder(encoder);
//        logstashTcpSocketAppender.start();
//        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(logstashTcpSocketAppender);
//        return logstashTcpSocketAppender;
//    }
//
//}
