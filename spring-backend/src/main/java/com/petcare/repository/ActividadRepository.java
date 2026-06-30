package com.petcare.repository;

/*
 * Comentario de modulo PetCare:
 * Repositorio legacy. Expone consultas de persistencia para entidades antiguas.
 */

import com.petcare.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
}
