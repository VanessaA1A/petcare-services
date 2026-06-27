package com.petcare.controller;

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
    public List<PetDto> getPetsByOwner(@PathVariable("ownerId") UUID ownerId) {
        return petService.findByOwnerId(ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDto> getPetById(@PathVariable("id") UUID id) {
        PetDto pet = petService.findById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<PetDto> createPet(@RequestBody @Valid PetDto petDto) {
        PetDto created = petService.create(petDto);
        return ResponseEntity.created(URI.create("/api/pets/" + created.getId())).body(created);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PetDto>> createPets(@RequestBody @Valid List<PetDto> pets) {
        List<PetDto> created = pets.stream().map(petService::create).toList();
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDto> updatePet(@PathVariable("id") UUID id, @RequestBody @Valid PetDto petDto) {
        PetDto updated = petService.update(id, petDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable("id") UUID id) {
        return petService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
