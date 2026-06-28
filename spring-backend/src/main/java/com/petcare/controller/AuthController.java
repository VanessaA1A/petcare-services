package com.petcare.controller;

import com.petcare.dto.SessionDto;
import com.petcare.dto.UsuarioDto;
import com.petcare.model.Usuario;
import com.petcare.security.JwtAuthenticatedUser;
import com.petcare.service.AuthService;
import com.petcare.service.SessionService;
import com.petcare.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, SessionService sessionService, UsuarioService usuarioService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.usuarioService = usuarioService;
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {
        Optional<String> token = authService.login(request.username(), request.password());
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        Usuario user = authService.findByUsername(request.username()).orElseThrow();
        SessionDto session = sessionService.createSession(
            user.getId(),
            token.get(),
            servletRequest.getRemoteAddr(),
            servletRequest.getHeader("User-Agent")
        );

        UsuarioDto dto = usuarioService.toDto(user);
        Map<String, Object> response = new HashMap<>();
        response.put("user", dto);
        response.put("session", session);
        return ResponseEntity.ok(response);
    }

    record RecoverRequest(@NotBlank String email) {}

    @PostMapping("/recover")
    public ResponseEntity<?> recover(@RequestBody @Valid RecoverRequest request) {
        Optional<String> token = authService.recoverPassword(request.email());
        return token
            .<ResponseEntity<?>>map(value -> ResponseEntity.ok(Map.of("message", "Recovery token created", "token", value)))
            .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Email not found")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        JwtAuthenticatedUser principal = (JwtAuthenticatedUser) auth.getPrincipal();
        Object details = auth.getDetails();
        Map<String, Object> user = new HashMap<>();
        user.put("id", principal.getUserId());
        user.put("username", principal.getUsername());
        user.put("email", principal.getEmail());
        user.put("rol", principal.getRol());
        user.put("role", principal.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("session", details instanceof SessionDto ? details : null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Authorization header"));
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Optional<Integer> invalidated = sessionService.invalidateSession(token);
        if (invalidated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Active session not found"));
        }
        return ResponseEntity.ok(Map.of("message", "Session closed successfully", "sessionId", invalidated.get()));
    }
}

