-- Bloque 7 (etiquetas por calificacion): badge calculado a partir del historial de servicios
-- completados, calificacion promedio y cancelaciones. Se recalcula automaticamente al
-- completar un servicio, cancelarlo o recibir una calificacion (ver BadgeService).

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS badge VARCHAR(30) NOT NULL DEFAULT 'NUEVO';
