package com.petcare.controller;

/*
 * Comentario de modulo PetCare:
 * Controlador REST legacy. Recibe peticiones HTTP y delega la logica al servicio.
 */

import com.petcare.dto.UsuarioDto;
import com.petcare.model.Usuario;
import com.petcare.service.AuthService;
import com.petcare.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
        String rol
    ) {}

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserRequest request) {
        Usuario created = authService.createUser(request.username(), request.email(), request.password(), request.rol());
        UsuarioDto dto = usuarioService.toDto(created);
        return ResponseEntity.created(URI.create("/api/users/" + dto.getId())).body(Map.of("user", dto));
    }

    @GetMapping
    public List<UsuarioDto> getAllUsers() {
        return usuarioService.findAll().stream().map(usuarioService::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> getUserById(@PathVariable Integer id) {
        return usuarioService.findById(id)
            .map(usuarioService::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(ResponseEntity.notFound()::build);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        String username = body.containsKey("username") ? (String) body.get("username") : null;
        String email = body.containsKey("email") ? (String) body.get("email") : null;
        String password = body.containsKey("password") ? (String) body.get("password") : null;
        String rol = body.containsKey("rol") ? (String) body.get("rol") : null;
        Boolean isActive = body.containsKey("is_active") ? (Boolean) body.get("is_active") : null;

        Optional<Usuario> updated = usuarioService.updateUser(id, username, email, password, rol, isActive);
        return updated.map(usuarioService::toDto).map(ResponseEntity::ok).orElseGet(ResponseEntity.notFound()::build);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        return usuarioService.deleteById(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<?> assignRole(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "role is required"));
        }
        return usuarioService.assignRole(id, role)
            .map(usuarioService::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(ResponseEntity.notFound()::build);
    }
}
