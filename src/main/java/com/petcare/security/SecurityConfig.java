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
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/users").permitAll()
                    .requestMatchers("/api/users/**").permitAll()
                    .requestMatchers("/api/pets/**").permitAll()
                    .requestMatchers("/api/offered-services/**").permitAll()
                    .requestMatchers("/api/service-requests/**").permitAll()
                    .requestMatchers("/api/service-applications/**").permitAll()
                    .requestMatchers("/api/ofertas/**").permitAll()
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
                    .requestMatchers("/api/usuarios/fcm-token").permitAll()
                    .requestMatchers("/api/cuidadores/**").permitAll()
                    .requestMatchers("/api/alertas-perdida/**").permitAll()
                    // Bloque 9: igual que el resto de la API, el cliente movil no adjunta un JWT en
                    // ningun otro endpoint de negocio (confia en el usuario_id que manda el cliente,
                    // ver RetrofitClient.kt) - sin esta linea, /api/calendario y /api/cuidadores/**
                    // caian en el "authenticated()" generico de abajo y CalendarioScreen.kt nunca
                    // podia cargar nada en la app real (bug preexistente, encontrado al escribir el
                    // test de integracion de disponibilidad del cuidador).
                    .requestMatchers("/api/calendario").permitAll()
                    .requestMatchers("/api/usuarios/no-molestar").permitAll()
                    .requestMatchers("/api/usuarios/{id}/foto").permitAll()
                    // Bloque 13: login propio del panel de administracion. La pagina HTML (/admin/**)
                    // queda publica (su propio JS redirige a /admin/login si no hay token valido);
                    // el login en si es publico (hace falta poder loguearse sin JWT); el resto de
                    // /api/admin/** exige un JWT valido (autenticado), y el rol "administrador" en si
                    // se valida ademas dentro de cada controlador (AdminAuthController/AdminDashboardController),
                    // ya que este proyecto no usa hasRole()/hasAuthority() en ningun otro lado.
                    .requestMatchers("/api/admin/auth/login").permitAll()
                    .requestMatchers("/api/admin/**").authenticated()
                    .requestMatchers("/admin/**").permitAll()
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
