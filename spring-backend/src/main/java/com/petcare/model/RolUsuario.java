package com.petcare.model;

/*
 * Comentario de modulo PetCare:
 * Modelo legacy. Representa tablas usadas por la version anterior del backend.
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RolUsuario {
    administrador,
    propietario,
    gestor;

    @JsonCreator
    public static RolUsuario from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (RolUsuario rol : values()) {
            if (rol.name().equalsIgnoreCase(value)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }

    @JsonValue
    @Override
    public String toString() {
        return name();
    }
}
