package com.petcare.security;

/*
 * Comentario de modulo PetCare:
 * Seguridad del backend. Configura autenticacion, JWT y usuarios reconocidos por Spring Security.
 */

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                Claims claims = jwtUtil.validateToken(token);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                if (StringUtils.hasText(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Cargar el UserDetails real (con las authorities segun el rol actual en BD) en vez de
                    // un principal con authorities vacias - antes de este fix, cualquier chequeo
                    // hasRole()/hasAuthority() en @PreAuthorize nunca podia pasar, ya que el principal
                    // siempre tenia una lista de authorities vacia sin importar el rol del usuario.
                    UserDetails userDetails = StringUtils.hasText(email) ? userDetailsService.loadUserByUsername(email) : null;
                    UserPrincipal principal = new UserPrincipal(
                        Integer.valueOf(userId),
                        email,
                        email,
                        userDetails != null ? userDetails.getAuthorities() : java.util.Collections.emptyList()
                    );
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // invalid token, continue without authentication
            }
        }

        filterChain.doFilter(request, response);
    }
}
