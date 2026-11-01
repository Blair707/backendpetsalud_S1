package com.petsalud.backendpetsalud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        @Size(max = 120, message = "El email es demasiado largo")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe incluir mayúscula, minúscula y número")
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre es demasiado largo")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60, message = "El apellido es demasiado largo")
        String apellido,

        @Pattern(regexp = "^$|^\\+?[0-9]{8,15}$", message = "El teléfono no es válido")
        String telefono,

        @Size(max = 200, message = "La dirección es demasiado larga")
        String direccion
) {
}
