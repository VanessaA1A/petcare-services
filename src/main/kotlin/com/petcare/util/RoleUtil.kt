package com.petcare.util

object RoleUtil {
    fun mapDbRoleToApi(dbRole: String?): String? {
        if (dbRole.isNullOrBlank()) return null
        val normalized = dbRole.trim().lowercase()
        return when {
            normalized == "propietario" || normalized == "owner" || normalized == "dueno" || normalized == "dueño" -> "OWNER"
            normalized == "gestor" || normalized == "caregiver" || normalized == "cuidador" -> "CAREGIVER"
            normalized == "administrador" || normalized == "admin" -> "ADMIN"
            normalized.contains("propiet") || normalized.contains("owner") || normalized.contains("due") -> "OWNER"
            normalized.contains("gest") || normalized.contains("care") || normalized.contains("cuid") -> "CAREGIVER"
            normalized.contains("admin") -> "ADMIN"
            else -> "CAREGIVER"
        }
    }

    fun normalizeRoleForDatabase(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val normalized = input.trim().lowercase()
        return when {
            normalized == "administrador" || normalized == "admin" -> "administrador"
            normalized == "propietario" || normalized == "owner" || normalized == "dueno" || normalized == "dueño" -> "propietario"
            normalized == "gestor" || normalized == "caregiver" || normalized == "cuidador" || normalized == "cliente" || normalized == "customer" -> "gestor"
            else -> null
        }
    }
}
