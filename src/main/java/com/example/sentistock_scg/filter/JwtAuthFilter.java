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

        // Preflight는 무조건 통과
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // ✅ 공개 경로(로그인/회원가입/OAuth/문서/헬스체크)는 토큰 없이 통과
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/auth/oauth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/board/v3/api-docs")
                || path.startsWith("/board/swagger-ui")
                || path.equals("/actuator/health")
                || path.equals("/health")) {
            return chain.filter(exchange);
        }

        // ✅ 보호할 대상만 검사 (/api/**, /board/**)
        if (!(path.startsWith("/api/") || path.startsWith("/board/"))) {
            return chain.filter(exchange);
        }

        // 헤더 초기화
        ServerHttpRequest.Builder reqBuilder = exchange.getRequest().mutate();
        reqBuilder.headers(h -> h.remove("X-User-Id"));

        // 토큰 검사
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
