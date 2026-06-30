package com.petcare.dto;

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define datos simples que entran o salen por la API.
 */

public class UsuarioDto {
    private Integer id;
    private String username;
    private String email;
    private String rol;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
