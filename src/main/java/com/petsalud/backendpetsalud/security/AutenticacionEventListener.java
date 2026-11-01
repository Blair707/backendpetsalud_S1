package com.petsalud.backendpetsalud.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AutenticacionEventListener {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria.seguridad");

    @EventListener
    public void onExito(AuthenticationSuccessEvent event) {
        AUDITORIA.info("LOGIN_OK usuario={}", limpiar(event.getAuthentication().getName()));
    }

    @EventListener
    public void onFallo(AbstractAuthenticationFailureEvent event) {
        AUDITORIA.warn("LOGIN_FALLIDO usuario={} motivo={}",
                limpiar(event.getAuthentication().getName()),
                event.getException().getClass().getSimpleName());
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.replaceAll("[\\r\\n\\t]", "_");
    }
}
