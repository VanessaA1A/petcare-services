package com.petcare.service;

import com.petcare.dto.SessionDto;
import com.petcare.model.Sesion;
import com.petcare.repository.SesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SesionRepository sesionRepository;

    public SessionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Transactional
    public SessionDto createSession(UUID userId, String token, String ipAddress, String userAgent) {
        Sesion session = new Sesion();
        session.setId(UUID.randomUUID());
        session.setUsuarioId(userId);
        session.setTokenSesion(token);
        session.setFechaInicio(OffsetDateTime.now());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setLogoutExplicito(false);
        Sesion saved = sesionRepository.save(session);
        return new SessionDto(saved.getId(), saved.getTokenSesion());
    }

    public Optional<SessionDto> findActiveSessionDto(String token) {
        return findActiveSessionByToken(token).map(sesion -> new SessionDto(sesion.getId(), sesion.getTokenSesion()));
    }

    public Optional<Sesion> findActiveSessionByToken(String token) {
        return sesionRepository.findByTokenSesion(token)
            .filter(sesion -> sesion.getFechaFin() == null || Boolean.FALSE.equals(sesion.getLogoutExplicito()));
    }

    @Transactional
    public boolean invalidateSession(String token) {
        return findActiveSessionByToken(token).map(sesion -> {
            sesion.setFechaFin(OffsetDateTime.now());
            sesion.setLogoutExplicito(true);
            sesionRepository.save(sesion);
            return true;
        }).orElse(false);
    }
}
