package com.petsalud.backendpetsalud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RegistroAccesoDenegadoHandler implements AccessDeniedHandler {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria.seguridad");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth != null ? auth.getName() : "anonimo";
        AUDITORIA.warn("ACCESO_DENEGADO usuario={} metodo={} ruta={} ip={}",
                limpiar(usuario), request.getMethod(), limpiar(request.getRequestURI()), request.getRemoteAddr());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
    }

    private static String limpiar(String valor) {
        // Evita inyección de saltos de línea en los logs (log forging)
        return valor == null ? "" : valor.replaceAll("[\\r\\n\\t]", "_");
    }
}
