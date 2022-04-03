package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(LoggingFilter.Config config) {

        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) ->{
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("LOGGING FILTER baseMessage: {}", config.getBaseMessage());

            if(config.isPreLogger())
                log.info("LOGGING PRE FILTER: 요청 id -> {}", request.getId());

            //Custom Post Filter 적용
            return chain.filter(exchange).then(Mono.fromRunnable(() ->{
                //Mono -> Spring Webflux 기능:  동기 방식이 아니라 비동기 방식의 서버를 지원할 때 단일 값 전달 할용 때 사용
                if(config.isPostLogger())
                    log.info("LOGGING POST FILTER: 응답 code -> {}", response.getStatusCode());
            }));
        }, Ordered.LOWEST_PRECEDENCE);
        // Ordered.HIGHEST_PRECEDENCE -> 가장 높은 실행 순서
        // Ordered.LOWEST_PRECEDENCE -> 가장 낮은 실행 순서
        return filter;
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}