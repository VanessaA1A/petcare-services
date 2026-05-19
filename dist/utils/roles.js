"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.mapDbRoleToApi = mapDbRoleToApi;
function mapDbRoleToApi(dbRole) {
    if (!dbRole)
        return null;
    const r = String(dbRole).trim().toLowerCase();
    // Common mappings from Spanish / English variants to API roles
    if (r === 'gestor' || r === 'owner' || r === 'propietario' || r === 'admin' || r === 'manager')
        return 'OWNER';
    if (r === 'cuidador' || r === 'caregiver' || r === 'cliente' || r === 'customer')
        return 'CAREGIVER';
    // fuzzy checks
    if (r.includes('gest') || r.includes('owner') || r.includes('propiet'))
        return 'OWNER';
    if (r.includes('cuid') || r.includes('care') || r.includes('client'))
        return 'CAREGIVER';
    // Fallback: if unknown, default to OWNER to avoid nulls on client that expects an enum
    return 'OWNER';
}
