package org.uu.manager.config;


import org.uu.manager.interceptor.WhiteListInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ManagerWebConfig implements WebMvcConfigurer {
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new WhiteListInterceptor()).addPathPatterns("/api/v1/accountChangeAdmin/*",
                "/accountChangeAdmin/*",
                "/menu/*",
                "/api/v1/appealOrderAdmin/*", "/appealOrderAdmin/*",
                "/api/v1/applyDistributedAdmin/*", "/applyDistributedAdmin/*",
                "/api/v1/cancellationRecharge/*", "/cancellationRecharge/*",
                "/api/v1/collectionInfoAdmin/*", "/collectionInfoAdmin/*",
                "/api/v1/collectionOrderAdmin", "/collectionOrderAdmin",
                "/api/v1/matchingOrderAdmin/*", "/matchingOrderAdmin/*",
                "/api/v1/matchPoolAdmin/*", "/matchPoolAdmin/*",
                "/api/v1/memberGroupAdmin/*", "/memberGroupAdmin/*",
                "/api/v1/memberInfoAdmin/*", "/memberInfoAdmin/*",
                "/api/v1/paymentOrderAdmin/*", "/paymentOrderAdmin/*",
                "/api/v1/merchantInfoAdmin/*", "/merchantInfoAdmin/*",
                "/syswhite/save", "/syswhite/update",
                "/api/v1/tradeConfigAdmin/*", "/tradeConfigAdmin/*",
                "/api/v1/usdtBuyOrderAdmin/*", "/usdtBuyOrderAdmin/*",
                "/api/v1/usdtConfigAdmin/*", "/usdtConfigAdmin/*",
                "/api/v1/withdrawalCancellation/*", "/withdrawalCancellation/*",
                "/api/v1/biMerchantPayOrderDaily/*", "/biMerchantPayOrderDaily/*",
                "/api/v1/biMerchantWithdrawOrderDaily/*", "/biMerchantWithdrawOrderDaily/*"
                );
        
    }


}
