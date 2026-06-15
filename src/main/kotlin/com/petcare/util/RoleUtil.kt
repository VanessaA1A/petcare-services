package com.petcare.util

object RoleUtil {
    fun mapDbRoleToApi(dbRole: String?): String? {
        if (dbRole.isNullOrBlank()) return null
        val normalized = dbRole.trim().lowercase()
        return when {
            normalized == "gestor" || normalized == "owner" || normalized == "propietario" || normalized == "admin" || normalized == "manager" -> "OWNER"
            normalized == "cuidador" || normalized == "caregiver" || normalized == "cliente" || normalized == "customer" -> "CAREGIVER"
            normalized.contains("gest") || normalized.contains("owner") || normalized.contains("propiet") -> "OWNER"
            normalized.contains("cuid") || normalized.contains("care") || normalized.contains("client") -> "CAREGIVER"
            else -> "OWNER"
        }
    }

    fun normalizeRoleForDatabase(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val normalized = input.trim().lowercase()
        return when {
            normalized == "gestor" -> "gestor"
            normalized == "cliente" -> "cliente"
            normalized == "owner" || normalized == "propietario" || normalized == "admin" || normalized == "manager" -> "OWNER"
            normalized == "caregiver" || normalized == "cuidador" || normalized == "customer" -> "CAREGIVER"
            else -> null
        }
    }
}
