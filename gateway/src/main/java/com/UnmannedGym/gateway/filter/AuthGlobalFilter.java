package com.UnmannedGym.gateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String SECRET = "privatekey#^&^%!save";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        System.out.println("请求路径: " + path);

        if (path.startsWith("/api/auth/") ||
                path.equals("/api/user/login") ||
                path.equals("/api/user/managerLogin") ||
                path.equals("/api/user/register") ||
                path.equals("/api/order/getLegalOrderbyUid")||
                path.startsWith("/api/face/") ||
                path.startsWith("/api/assistant/") ||
                path.startsWith("/api/count/") ||
                path.startsWith("/api/order/package/")||
                path.startsWith("/ws/")) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            return unauthorized(exchange.getResponse(), "未登录，请先登录");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
            Map<String, Claim> claims = decodedJWT.getClaims();

            Integer userId = claims.get("id").asInt();
            Integer role = claims.get("role").asInt();

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", String.valueOf(role))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (TokenExpiredException e) {
            return unauthorized(exchange.getResponse(), "Token已过期，请重新登录");
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "Token无效");
        }
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"msg\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
