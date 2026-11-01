package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String REGISTRO_OK = """
            {"email":"cliente1@correo.cl","password":"Segura123","nombre":"Ana","apellido":"Pérez",
             "telefono":"+56911112222","direccion":"Santiago"}""";

    @BeforeEach
    void preparar() {
        if (usuarioRepository.findByEmail("vet@petsalud.cl").isEmpty()) {
            usuarioRepository.save(Usuario.builder().email("vet@petsalud.cl")
                    .password(passwordEncoder.encode("Vet123456")).nombre("Vet").apellido("Uno")
                    .rol(Rol.VETERINARIO).build());
        }
    }

    @Test
    void sinCredencialesRetorna401() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void credencialesInvalidasRetorna401() throws Exception {
        mockMvc.perform(get("/api/productos").with(httpBasic("admin@petsalud.cl", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroEsPublico_cifraPasswordYNoLaExpone() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO_OK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(content().string(not(containsString("Segura123"))))
                .andExpect(content().string(not(containsString("password"))));

        Usuario guardado = usuarioRepository.findByEmail("cliente1@correo.cl").orElseThrow();
        assertNotEquals("Segura123", guardado.getPassword());
        assertTrue(passwordEncoder.matches("Segura123", guardado.getPassword()));
    }

    @Test
    void registroRechazaPasswordDebil() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTRO_OK.replace("Segura123", "abc")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroIgnoraRolEnviadoPorElCliente() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTRO_OK.replace("cliente1@", "hacker@").replace("\"nombre\"", "\"rol\":\"ADMIN\",\"nombre\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void clienteNoPuedeUsarEndpointsDeAdmin() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_OK.replace("cliente1@", "cliente2@")));
        mockMvc.perform(get("/api/usuarios").with(httpBasic("cliente2@correo.cl", "Segura123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/productos").with(httpBasic("cliente2@correo.cl", "Segura123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void veterinarioNoPuedeListarUsuariosPeroAdminSi() throws Exception {
        mockMvc.perform(get("/api/usuarios").with(httpBasic("vet@petsalud.cl", "Vet123456")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios").with(httpBasic("admin@petsalud.cl", "Admin12345")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void clienteSoloVeSusPropiosDatos() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_OK.replace("cliente1@", "cliente3@")));
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_OK.replace("cliente1@", "cliente4@")));
        Long id3 = usuarioRepository.findByEmail("cliente3@correo.cl").orElseThrow().getId();
        Long id4 = usuarioRepository.findByEmail("cliente4@correo.cl").orElseThrow().getId();

        mockMvc.perform(get("/api/usuarios/" + id3).with(httpBasic("cliente3@correo.cl", "Segura123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/" + id4).with(httpBasic("cliente3@correo.cl", "Segura123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void soloAdminCreaVeterinarios() throws Exception {
        String body = """
                {"email":"vet2@petsalud.cl","password":"Vet2Pass123","nombre":"Luis","apellido":"Mora"}""";
        mockMvc.perform(post("/api/admin/usuarios?rol=VETERINARIO").with(httpBasic("vet@petsalud.cl", "Vet123456"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/usuarios?rol=VETERINARIO").with(httpBasic("admin@petsalud.cl", "Admin12345"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VETERINARIO"));
    }
}
