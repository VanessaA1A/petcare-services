package com.petcare.service;

import com.petcare.model.Usuario;
import com.petcare.repository.UsuarioRepository;
import com.petcare.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Optional<String> login(String username, String password) {
        return usuarioRepository.findByUsername(username)
            .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
            .map(user -> jwtTokenService.createToken(user.getId(), user.getUsername(), user.getEmail(), user.getRol()));
    }

    public Usuario createUser(String username, String email, String password, String rol) {
        Usuario user = new Usuario();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRol(rol);
        user.setIsActive(true);
        return usuarioRepository.save(user);
    }
}
