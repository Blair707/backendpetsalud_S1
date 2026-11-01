package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("acceso")
public class AccesoService {

    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;

    public AccesoService(UsuarioRepository usuarioRepository, MascotaRepository mascotaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
    }

    /** true si el usuario autenticado es el usuario con ese id. */
    public boolean esUsuario(Long usuarioId, Authentication auth) {
        if (usuarioId == null || auth == null) {
            return false;
        }
        return usuarioRepository.findById(usuarioId)
                .map(u -> u.getEmail().equalsIgnoreCase(auth.getName()))
                .orElse(false);
    }

    /** true si el usuario autenticado es el propietario de la mascota. */
    public boolean esPropietarioDeMascota(Long mascotaId, Authentication auth) {
        if (mascotaId == null || auth == null) {
            return false;
        }
        return mascotaRepository.findById(mascotaId)
                .map(m -> m.getPropietario() != null
                        && m.getPropietario().getEmail().equalsIgnoreCase(auth.getName()))
                .orElse(false);
    }
}
