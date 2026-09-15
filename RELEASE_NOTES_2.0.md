# PetCare v2.0 — Release Notes

Resumen de lo agregado sobre la plataforma base (autenticación, mascotas, solicitudes de
servicio, postulaciones, chat, calificaciones y seguimiento en vivo por mapa), repartido en los
Bloques 4 al 12. Excluye explícitamente monetización, membresías, planes premium y
organizaciones (veterinarias/tiendas/adopción) — no se tocó nada de eso.

## Bloque 4 — UX / Play Store ready
- Snackbar en vez de Toast en toda la app, con acción de "Deshacer" donde aplica.
- Coil configurado globalmente (caché de memoria 25%, caché de disco 50MB).
- Interceptor de reintentos en Retrofit con backoff exponencial (1s/2s/4s), solo para peticiones GET.
- Soporte para inglés (`values-en/strings.xml`) en las pantallas principales.

## Bloque 5 — Offline y accesibilidad
- Caché offline de mensajes de chat en Room, con cola de reintento automática al reconectar.
- Auditoría de accesibilidad: contraste WCAG AA, tamaño mínimo de áreas táctiles, `contentDescription` en imágenes.

## Bloque 6 — Tests de integración
- Backend: suite de integración end-to-end (`IntegrationTest.kt`) contra una base PostgreSQL
  local real (`petcare_test`, sin TestContainers ni Docker), cubriendo el flujo completo
  registro→login→solicitud→postulación→aceptación→chat→completado→calificación.
- Android: tests de red con MockWebServer (200/400/500/timeout con reintento).

## Bloque 7 — Etiquetas por calificación (badges)
- `usuarios.badge`: NUEVO, EN_CRECIMIENTO, CONFIABLE, EXPERIMENTADO, ELITE o EN_OBSERVACION,
  calculado a partir de servicios completados, calificación promedio y cancelaciones.
- Se recalcula automáticamente al completar/cancelar un servicio o recibir una calificación.
- Visible en el perfil, en las tarjetas de ofertas y en las tarjetas de solicitudes.

## Bloque 8 — Foto antes/después de un servicio
- El cuidador sube una foto al iniciar (ANTES) y al terminar (DESPUÉS) el servicio.
- Si no hay internet, el cambio de estado se permite igual (la foto se sube al reconectar).

## Bloque 9 — Calendario integrado
- Vista de calendario (mes/semana) con los servicios programados del usuario, como dueño o como cuidador.

## Bloque 10 — Llamadas telefónicas
- Botón de llamada directa (`Intent.ACTION_DIAL`) en el detalle de la solicitud, visible solo
  una vez que el servicio está confirmado (ACCEPTED/DONE_BY_CAREGIVER/COMPLETED).

## Bloque 11 — Expediente médico de la mascota
- Historial médico por mascota (vacunas, desparasitación, alergias, medicamentos, cirugías,
  peso, notas), editable solo por el dueño.
- Alertas automáticas (push) cuando una vacuna vence en 15 días o menos.

## Bloque 12 — Alerta de mascota perdida
- El dueño activa una alerta con ubicación; se notifica por push a usuarios cercanos (1km
  urgente, 5km ampliado, 10km zona).
- Cualquier usuario puede reportar un avistamiento. Cierre automático a los 7 días.

## Notas de implementación
- Varias migraciones nuevas (`010`-`016` en `petcare-services`/`petcare-bd`) agregan las tablas
  `chat_messages.image_url`, `emergencias`, `valoraciones_tiempo_real`, `usuarios.badge`,
  `evidencias_servicio`, `expediente_medico`, `alertas_perdida` y `avistamientos`.
- Donde el DDL exacto no venía especificado, se diseñó siguiendo las convenciones ya usadas en
  el esquema (ver notas de procedencia en `petcare-bd/MIGRATIONS.md`).
- La API Key real de Google Maps sigue pendiente de configurar por el usuario — ver
  `PetCareApp/GOOGLE_MAPS_SETUP.md`.
- Documentación completa de la API en Swagger (`/swagger-ui.html` con el backend corriendo).
