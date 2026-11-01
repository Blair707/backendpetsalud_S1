package com.petsalud.backendpetsalud.config;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        @Value("${petsalud.admin.email:}") String email,
                        @Value("${petsalud.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nombre("Administrador")
                .apellido("Petsalud")
                .rol(Rol.ADMIN)
                .build());
        LOG.info("Usuario administrador inicial creado: {}", email);
    }
}
