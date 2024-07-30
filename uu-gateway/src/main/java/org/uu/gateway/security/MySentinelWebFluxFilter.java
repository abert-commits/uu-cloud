//package org.uu.gateway.security;
//
//import com.alibaba.csp.sentinel.EntryType;
//import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
//import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
//import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
//
//import com.alibaba.csp.sentinel.adapter.spring.webflux.SentinelWebFluxFilter;
//import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
//import org.springframework.http.HttpHeaders;
//
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.Optional;
//
//public class MySentinelWebFluxFilter extends SentinelWebFluxFilter {
//    private static final String EMPTY_ORIGIN = "";
//
//    public MySentinelWebFluxFilter() {
//
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        return chain.filter(exchange).transform(this.buildSentinelTransformer(exchange));
//
//    }
//
//    private SentinelReactorTransformer<Void> buildSentinelTransformer(ServerWebExchange exchange) {
//        ServerHttpRequest serverHttpRequest = exchange.getRequest();
//        String path = exchange.getRequest().getPath().value();
//        String finalPath = (String) Optional.ofNullable(WebFluxCallbackManager.getUrlCleaner()).map((f) -> {
//            return (String) f.apply(exchange, path);
//        }).orElse(path);
//        String origin = getIpAddress(serverHttpRequest);
//        return new SentinelReactorTransformer<>(new EntryConfig(finalPath, EntryType.IN, new ContextConfig(finalPath, origin)));
//
//    }
//
//    public static  String getIpAddress(ServerHttpRequest request){
//        HttpHeaders headers = request.getHeaders();
//        String ip = headers.getFirst("x-forwarded-for");
//        if(ip !=null && ip.length()!=0 &&!"unknown".equalsIgnoreCase(ip)){
//            if(ip.indexOf(",")!=-1){
//                ip = ip.split(",")[0];
//            }
//        }
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = headers.getFirst("Proxy-Client-Ip");
//        }
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = headers.getFirst("WL-Proxy-Client-Ip");
//        }
//
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = headers.getFirst("HTTP_CLIENT_IP");
//        }
//
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
//        }
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = headers.getFirst("X-Real-IP");
//        }
//        if(ip == null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getRemoteAddress().getAddress().getHostAddress();
//        }
//
//        return ip;
//    }
//
//}
