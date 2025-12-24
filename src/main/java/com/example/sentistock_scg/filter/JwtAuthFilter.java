package com.example.sentistock_scg.filter;

import com.example.sentistock_scg.jwt.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

    
        String normalized = path;
        if (normalized.startsWith("/api/api/")) {
            normalized = normalized.substring("/api".length());
        }
        if (normalized.startsWith("/board/board/")) {
            normalized = normalized.substring("/board".length()); 
        }

    
        if (normalized.startsWith("/api/swagger-ui")
                || normalized.startsWith("/api/v3/api-docs")
                || normalized.startsWith("/api/webjars")
                || normalized.equals("/api/swagger-ui.html")
                || normalized.startsWith("/board/swagger-ui")
                || normalized.startsWith("/board/v3/api-docs")
                || normalized.startsWith("/board/webjars")
                || normalized.equals("/board/swagger-ui.html")) {
            return chain.filter(exchange);
        }

       
        if (normalized.startsWith("/api/auth/")
                || normalized.startsWith("/auth/")
                || normalized.startsWith("/api/oauth2/")
                || normalized.startsWith("/oauth2/")) {
            return chain.filter(exchange);
        }

        if (!(normalized.startsWith("/api/") || normalized.startsWith("/board/"))) {
            return chain.filter(exchange);
        }


        ServerHttpRequest.Builder reqBuilder = exchange.getRequest().mutate();
        reqBuilder.headers(h -> h.remove("X-User-Id"));

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7);

        if (!jwtTokenProvider.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtTokenProvider.getUserId(token);

   
        ServerHttpRequest mutated = reqBuilder
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
