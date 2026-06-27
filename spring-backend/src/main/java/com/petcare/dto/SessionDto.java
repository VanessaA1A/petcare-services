package com.petcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SessionDto {
    private UUID id;
    private String token;

    public SessionDto() {
    }

    public SessionDto(UUID id, String token) {
        this.id = id;
        this.token = token;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
