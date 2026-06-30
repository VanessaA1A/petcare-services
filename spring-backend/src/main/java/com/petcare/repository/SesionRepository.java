package com.petcare.repository;

/*
 * Comentario de modulo PetCare:
 * Repositorio legacy. Expone consultas de persistencia para entidades antiguas.
 */

import com.petcare.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    Optional<Sesion> findByTokenSesion(String tokenSesion);
}
