package com.petcare.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RolUsuario {
    gestor,
    cliente,
    OWNER,
    CAREGIVER;

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
