package org.uu.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.http.MediaType;
import org.uu.common.core.result.ResultCode;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.reactive.function.server.ServerResponse;
import javax.annotation.PostConstruct;


@Configuration
public class SentinelConfig {

//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public WebFilter sentinelWebFluxFilter(){
//        return new MySentinelWebFluxFilter();
//    }
//
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public WebExceptionHandler sentinelBlockExceptionHandler(){
//        return new MySentinelBlockExceptionHandler();
//    }

    @PostConstruct
    private void initBlockHandler() {
        GatewayCallbackManager.setBlockHandler(
                (exchange, t) ->
                        ServerResponse
                                .status(HttpStatus.TOO_MANY_REQUESTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        BodyInserters.fromValue(ResultCode.FLOW_LIMITING.toString())
                                )
        );
    }
}
