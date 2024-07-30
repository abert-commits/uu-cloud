package org.uu.auth;


import org.springframework.context.ConfigurableApplicationContext;
import org.uu.common.pay.api.MemberFeignClient;
import org.uu.common.pay.api.OAuthClientFeignClient;
import org.uu.common.pay.api.UserFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = {UserFeignClient.class, OAuthClientFeignClient.class, MemberFeignClient.class})
public class AuthApp {
    public static void main(String[] args) {

        // 设置默认时区为UTC时区
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        ConfigurableApplicationContext application = SpringApplication.run(AuthApp.class, args);
        //优雅停机
        refreshBootContext(application);
    }

    private static void refreshBootContext(ConfigurableApplicationContext context) {
        //添加springboot 集成的优雅停机钩子
        context.registerShutdownHook();
        //扩展钩子
        refreshExtendShutDown(context);
    }

    /**
     * 如果springboot启停无法满足，扩展自己的钩子
     * @param context
     */
    private static void refreshExtendShutDown(ConfigurableApplicationContext context){
        // 注册 Shutdown Hook 线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("扩展钩子优雅停机中...");
            // 关闭 Spring 应用上下文
            //SpringApplication.exit(context);
        }));
    }
}
