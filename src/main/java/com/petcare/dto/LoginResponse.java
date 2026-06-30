package com.petcare.dto;

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define datos simples que entran o salen por la API.
 */

import com.petcare.model.User;

public class LoginResponse {
    private String token;
    private User user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
