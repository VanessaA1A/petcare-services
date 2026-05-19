function mapDbRoleToApi(dbRole) {
  if (!dbRole) return null;
  const r = String(dbRole).trim().toLowerCase();
  if (r === 'gestor' || r === 'owner' || r === 'propietario' || r === 'admin' || r === 'manager') return 'OWNER';
  if (r === 'cuidador' || r === 'caregiver' || r === 'cliente' || r === 'customer') return 'CAREGIVER';
  if (r.indexOf('gest') !== -1 || r.indexOf('owner') !== -1 || r.indexOf('propiet') !== -1) return 'OWNER';
  if (r.indexOf('cuid') !== -1 || r.indexOf('care') !== -1 || r.indexOf('client') !== -1) return 'CAREGIVER';
  return 'OWNER';
}

module.exports = { mapDbRoleToApi };
