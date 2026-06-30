package com.petcare.service;

/*
 * Comentario de modulo PetCare:
 * Servicio legacy. Conserva reglas de negocio usadas por la version anterior del backend.
 */

import com.petcare.dto.SessionDto;
import com.petcare.model.Sesion;
import com.petcare.repository.SesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class SessionService {
    private final SesionRepository sesionRepository;

    public SessionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Transactional
    public SessionDto createSession(Integer userId, String token, String ipAddress, String userAgent) {
        Sesion session = new Sesion();
        session.setUsuarioId(userId);
        session.setTokenSesion(token);
        session.setFechaInicio(OffsetDateTime.now());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setLogoutExplicito(false);
        Sesion saved = sesionRepository.save(session);
        return new SessionDto(saved.getId(), saved.getTokenSesion(), saved.getFechaInicio());
    }

    public Optional<SessionDto> findActiveSessionDto(String token) {
        return findActiveSessionByToken(token).map(sesion -> new SessionDto(sesion.getId(), sesion.getTokenSesion(), sesion.getFechaInicio()));
    }

    public Optional<Integer> invalidateSession(String token) {
        return findActiveSessionByToken(token).map(sesion -> {
            sesion.setFechaFin(OffsetDateTime.now());
            sesion.setLogoutExplicito(true);
            sesionRepository.save(sesion);
            return sesion.getId();
        });
    }

    public Optional<Sesion> findActiveSessionByToken(String token) {
        return sesionRepository.findByTokenSesion(token)
            .filter(sesion -> sesion.getFechaFin() == null || Boolean.FALSE.equals(sesion.getLogoutExplicito()));
    }
}
