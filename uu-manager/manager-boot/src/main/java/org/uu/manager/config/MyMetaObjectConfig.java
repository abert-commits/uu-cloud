//package org.uu.manager.config;
//
//import com.baomidou.mybatisplus.core.config.GlobalConfig;
//
//import org.uu.manager.handler.MyMetaObjectHandler;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//@Configuration
//public class MyMetaObjectConfig {
//
//    /**
//     * 自动填充数据库创建人、创建时间、更新人、更新时间
//     */
//    @Bean
//    public GlobalConfig globalConfig() {
//        GlobalConfig globalConfig = new GlobalConfig();
//        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
//        return globalConfig;
//    }
//}
