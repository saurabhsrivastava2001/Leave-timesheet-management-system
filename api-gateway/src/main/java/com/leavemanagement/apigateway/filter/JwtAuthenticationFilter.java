package com.leavemanagement.apigateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                
                if (jwtUtil.isInvalid(authHeader)) {
                    return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
                }
                
                var claims = jwtUtil.getClaims(authHeader);
                String employeeCode = claims.getSubject();
                Object rolesClaim = claims.get("roles");
                String roles = "";
                if (rolesClaim instanceof Collection<?> rolesCollection) {
                    roles = rolesCollection.stream().map(Object::toString).collect(Collectors.joining(","));
                } else if (rolesClaim != null) {
                    roles = rolesClaim.toString();
                }
                ServerHttpRequest request = exchange.getRequest()
                        .mutate()
                        .header("X-Employee-Code", employeeCode)
                        .header("X-User-Roles", roles)
                        .build();
                return chain.filter(exchange.mutate().request(request).build());
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
