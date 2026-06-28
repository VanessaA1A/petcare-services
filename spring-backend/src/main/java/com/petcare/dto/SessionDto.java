package com.petcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class SessionDto {
    private Integer id;
    private String token;
    private OffsetDateTime fechaInicio;

    public SessionDto() {
    }

    public SessionDto(Integer id, String token, OffsetDateTime fechaInicio) {
        this.id = id;
        this.token = token;
        this.fechaInicio = fechaInicio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
