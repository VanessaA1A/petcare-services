package com.petcare.service;

import com.petcare.dto.UsuarioDto;
import com.petcare.model.Usuario;
import com.petcare.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
        d.setRol(u.getRol());
        return d;
    }
}
