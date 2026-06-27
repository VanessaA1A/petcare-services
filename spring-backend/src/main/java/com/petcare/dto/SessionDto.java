package com.petcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SessionDto {
    private UUID id;
    private String token;
    private OffsetDateTime fechaInicio;

    public SessionDto() {
    }

    public SessionDto(UUID id, String token, OffsetDateTime fechaInicio) {
        this.id = id;
        this.token = token;
        this.fechaInicio = fechaInicio;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonProperty("token_sesion")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("fecha_inicio")
    public OffsetDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(OffsetDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
}
