package com.petcare.repository;

/*
 * Comentario de modulo PetCare:
 * Repositorio legacy. Expone consultas de persistencia para entidades antiguas.
 */

import com.petcare.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
}
