package org.uu.gateway.security;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

    @Component
    @Slf4j
    @RequiredArgsConstructor
    public class PortalFilter implements GlobalFilter, Ordered {

    @Value("${spring.application.name}")
    private String applicationName;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("serviceName", applicationName)
                .build();
        exchange = exchange.mutate().request(request).build();
//        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
//        Route newRoute = Route.async()
//                .asyncPredicate(route.getPredicate())
//                .filters(route.getFilters())
//                .id(route.getId())
//                .order(route.getOrder())
//                .uri("lb://"+genNewRoute(route.getUri().toString(),request))
//                .build();
//        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
        return chain.filter(exchange);
    }

    private String genNewRoute(String toString, ServerHttpRequest request) {
        return "uu-manager";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

