package com.petcare.service;

import com.petcare.dto.UsuarioDto;
import com.petcare.model.RolUsuario;
import com.petcare.model.Usuario;
import com.petcare.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> updateUser(Integer id, String username, String email, String password, String rol, Boolean isActive) {
        return usuarioRepository.findById(id).map(user -> {
            if (username != null) user.setUsername(username);
            if (email != null) user.setEmail(email);
            if (password != null && !password.isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(password));
            }
            if (rol != null) user.setRol(RolUsuario.from(rol));
            if (isActive != null) user.setIsActive(isActive);
            return usuarioRepository.save(user);
        });
    }

    public boolean deleteById(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            return false;
        }
        usuarioRepository.deleteById(id);
        return true;
    }

    public Optional<Usuario> assignRole(Integer id, String role) {
        return usuarioRepository.findById(id).map(user -> {
            user.setRol(RolUsuario.from(role));
            return usuarioRepository.save(user);
        });
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public UsuarioDto toDto(Usuario u) {
        UsuarioDto d = new UsuarioDto();
        d.setId(u.getId());
        d.setEmail(u.getEmail());
        d.setUsername(u.getUsername());
        d.setRol(u.getRol() != null ? u.getRol().toString() : null);
        return d;
    }
}
