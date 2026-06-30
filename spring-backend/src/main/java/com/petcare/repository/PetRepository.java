package com.petcare.repository;

/*
 * Comentario de modulo PetCare:
 * Repositorio legacy. Expone consultas de persistencia para entidades antiguas.
 */

import com.petcare.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
    List<Pet> findByOwnerId(Integer ownerId);
}
