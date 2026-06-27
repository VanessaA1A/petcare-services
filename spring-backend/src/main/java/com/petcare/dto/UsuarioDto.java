package com.petcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UsuarioDto {
    private UUID id;
    private String username;
    private String email;
    private String rol;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @JsonProperty("role")
    public String getRole() { return rol; }
}
