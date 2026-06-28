package com.petcare.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petcare.dto.PetDto;
import com.petcare.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/all")
    public List<PetDto> getAllPets() {
        return petService.findAll();
    }

    @GetMapping("/owner/{ownerId}")
    public List<PetDto> getPetsByOwner(@PathVariable("ownerId") Integer ownerId) {
        return petService.findByOwnerId(ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDto> getPetById(@PathVariable("id") Integer id) {
        PetDto pet = petService.findById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<PetDto> createPet(@RequestBody @Valid PetDto petDto) {
        PetDto created = petService.create(petDto);
        return ResponseEntity.created(URI.create("/api/pets/" + created.getId())).body(created);
    }

    public record BulkPetRequest(@JsonProperty("owner_id") Integer ownerId, List<PetDto> pets) {}

    @PostMapping("/bulk")
    public ResponseEntity<List<PetDto>> createPets(@RequestBody @Valid BulkPetRequest request) {
        List<PetDto> created = request.pets().stream().map(pet -> {
            pet.setOwnerId(request.ownerId());
            return petService.create(pet);
        }).toList();
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDto> updatePet(@PathVariable("id") Integer id, @RequestBody @Valid PetDto petDto) {
        PetDto updated = petService.update(id, petDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable("id") Integer id) {
        return petService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
