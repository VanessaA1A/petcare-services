package com.petcare.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoleUtilTest {

    @Test
    fun `mapDbRoleToApi mapea los roles conocidos de la base de datos`() {
        assertEquals("OWNER", RoleUtil.mapDbRoleToApi("propietario"))
        assertEquals("CAREGIVER", RoleUtil.mapDbRoleToApi("gestor"))
        assertEquals("ADMIN", RoleUtil.mapDbRoleToApi("administrador"))
    }

    @Test
    fun `mapDbRoleToApi es insensible a mayusculas y espacios`() {
        assertEquals("OWNER", RoleUtil.mapDbRoleToApi(" Propietario "))
        assertEquals("CAREGIVER", RoleUtil.mapDbRoleToApi("GESTOR"))
    }

    @Test
    fun `mapDbRoleToApi devuelve null para valores vacios`() {
        assertNull(RoleUtil.mapDbRoleToApi(null))
        assertNull(RoleUtil.mapDbRoleToApi("   "))
    }

    @Test
    fun `normalizeRoleForDatabase acepta alias en ingles y espanol`() {
        assertEquals("propietario", RoleUtil.normalizeRoleForDatabase("owner"))
        assertEquals("propietario", RoleUtil.normalizeRoleForDatabase("dueño"))
        assertEquals("gestor", RoleUtil.normalizeRoleForDatabase("caregiver"))
        assertEquals("gestor", RoleUtil.normalizeRoleForDatabase("cliente"))
        assertEquals("administrador", RoleUtil.normalizeRoleForDatabase("admin"))
    }

    @Test
    fun `normalizeRoleForDatabase devuelve null para un rol desconocido`() {
        assertNull(RoleUtil.normalizeRoleForDatabase("superusuario"))
        assertNull(RoleUtil.normalizeRoleForDatabase(null))
    }
}
