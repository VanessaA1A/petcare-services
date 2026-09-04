package com.petcare.security;

/*
 * Comentario de modulo PetCare:
 * Seguridad del backend. Configura autenticacion, JWT y usuarios reconocidos por Spring Security.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/users").permitAll()
                    .requestMatchers("/api/users/**").permitAll()
                    .requestMatchers("/api/pets/**").permitAll()
                    .requestMatchers("/api/offered-services/**").permitAll()
                    .requestMatchers("/api/service-requests/**").permitAll()
                    .requestMatchers("/api/service-applications/**").permitAll()
                    .requestMatchers("/api/ratings/**").permitAll()
                    .requestMatchers("/api/razas").permitAll()
                    .requestMatchers("/api/razas/**").permitAll()
                    .requestMatchers("/api/geo/**").permitAll()
                    .requestMatchers("/api/chat/**").permitAll()
                    .requestMatchers("/api/solicitudes/**").permitAll()
                    .requestMatchers("/api/favoritos/**").permitAll()
                    .requestMatchers("/api/notas/**").permitAll()
                    .requestMatchers("/api/busquedas-guardadas/**").permitAll()
                    .requestMatchers("/api/usuarios/verificar-rol").permitAll()
                    .requestMatchers("/api/usuarios/{id}/foto").permitAll()
                    .requestMatchers("/static/**").permitAll()
                    .requestMatchers("/api/usuarios/me/**").authenticated()
                    .requestMatchers("/api/usuarios/me").authenticated()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
