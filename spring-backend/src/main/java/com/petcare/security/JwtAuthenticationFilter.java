package com.petcare.security;

import com.petcare.dto.SessionDto;
import com.petcare.service.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(JwtTokenService tokenService, SessionService sessionService) {
        this.tokenService = tokenService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            try {
                Claims claims = tokenService.parseToken(token);
                UUID userId = UUID.fromString(claims.getSubject());
                String rol = claims.get("rol", String.class);
                String username = claims.get("username", String.class);
                String email = claims.get("email", String.class);
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
                sessionService.findActiveSessionByToken(token).ifPresentOrElse(
                    session -> {
                        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(userId, username, email, rol, authorities);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, token, authorities);
                        auth.setDetails(session);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    },
                    SecurityContextHolder::clearContext
                );
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
