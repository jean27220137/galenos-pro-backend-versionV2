package com.galenospro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrarUsuarioRequestDto {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String cargo;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "FARMACEUTICO|JEFE_FARMACIA|ALMACENERO|ADMIN",
             message = "Rol inválido")
    private String rol;

    private Long farmaciaId;
}
