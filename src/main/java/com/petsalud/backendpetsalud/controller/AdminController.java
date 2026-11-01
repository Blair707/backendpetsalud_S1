package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.RegistroRequest;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Crea empleados (VETERINARIO / ADMIN) u otros usuarios: POST /api/admin/usuarios?rol=VETERINARIO */
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody RegistroRequest request,
                                                        @RequestParam Rol rol) {
        Usuario creado = usuarioService.registrarUsuario(Usuario.builder()
                .email(request.email())
                .password(request.password())
                .nombre(request.nombre())
                .apellido(request.apellido())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .rol(rol)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }
}
