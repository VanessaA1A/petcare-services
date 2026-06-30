package com.petcare.service;

/*
 * Comentario de modulo PetCare:
 * Servicio legacy. Conserva reglas de negocio usadas por la version anterior del backend.
 */

import com.petcare.dto.PetDto;
import com.petcare.model.Pet;
import com.petcare.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {
    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<PetDto> findAll() {
        return petRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public PetDto findById(Integer id) {
        return petRepository.findById(id).map(this::toDto).orElse(null);
    }

    public List<PetDto> findByOwnerId(Integer ownerId) {
        return petRepository.findByOwnerId(ownerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public PetDto create(PetDto dto) {
        Pet pet = toEntity(dto);
        pet.setCreatedAt(OffsetDateTime.now());
        pet.setUpdatedAt(OffsetDateTime.now());
        return toDto(petRepository.save(pet));
    }

    public PetDto update(Integer id, PetDto dto) {
        return petRepository.findById(id).map(existing -> {
            if (dto.getName() != null) existing.setName(dto.getName());
            if (dto.getSpecies() != null) existing.setSpecies(dto.getSpecies());
            if (dto.getBreed() != null) existing.setBreed(dto.getBreed());
            if (dto.getSize() != null) existing.setSize(dto.getSize());
            if (dto.getAge() != null) existing.setAge(dto.getAge());
            if (dto.getWeight() != null) existing.setWeight(dto.getWeight());
            if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
            existing.setUpdatedAt(OffsetDateTime.now());
            return toDto(petRepository.save(existing));
        }).orElse(null);
    }

    public boolean delete(Integer id) {
        if (!petRepository.existsById(id)) return false;
        petRepository.deleteById(id);
        return true;
    }

    private PetDto toDto(Pet pet) {
        PetDto dto = new PetDto();
        dto.setId(pet.getId());
        dto.setOwnerId(pet.getOwnerId());
        dto.setName(pet.getName());
        dto.setSpecies(pet.getSpecies());
        dto.setBreed(pet.getBreed());
        dto.setSize(pet.getSize());
        dto.setAge(pet.getAge());
        dto.setWeight(pet.getWeight());
        dto.setDescription(pet.getDescription());
        dto.setCreatedAt(pet.getCreatedAt());
        dto.setUpdatedAt(pet.getUpdatedAt());
        return dto;
    }

    private Pet toEntity(PetDto dto) {
        Pet pet = new Pet();
        pet.setId(dto.getId());
        pet.setOwnerId(dto.getOwnerId());
        pet.setName(dto.getName());
        pet.setSpecies(dto.getSpecies());
        pet.setBreed(dto.getBreed());
        pet.setSize(dto.getSize());
        pet.setAge(dto.getAge());
        pet.setWeight(dto.getWeight());
        pet.setDescription(dto.getDescription());
        pet.setCreatedAt(dto.getCreatedAt());
        pet.setUpdatedAt(dto.getUpdatedAt());
        return pet;
    }
}
