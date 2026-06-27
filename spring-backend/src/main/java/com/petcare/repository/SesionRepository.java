package com.petcare.repository;

import com.petcare.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, UUID> {
    Optional<Sesion> findByTokenSesion(String tokenSesion);
}
