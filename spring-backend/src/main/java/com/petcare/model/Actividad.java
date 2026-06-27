package com.petcare.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "actividades")
public class Actividad {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sesion_id")
    private UUID sesionId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "tipo_actividad")
    private String tipoActividad;

    private String descripcion;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "fecha_hora")
    private OffsetDateTime fechaHora;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSesionId() { return sesionId; }
    public void setSesionId(UUID sesionId) { this.sesionId = sesionId; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public String getTipoActividad() { return tipoActividad; }
    public void setTipoActividad(String tipoActividad) { this.tipoActividad = tipoActividad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }
}
