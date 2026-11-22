package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

// @NotBlank testa o envio de ao menos 1 caracter
public record UsuarioRequestDTO(
        @NotBlank
        @Email(message = "O email deve ser válido")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min=6, max=6, message = "A senha deve conter exatamente 6 caracteres")
        String password,
        @Pattern(regexp = "ROLE_ADMIN|ROLE_CLIENTE|ADMIN|CLIENTE",
                message = "O perfil deve ser ADMIN ou CLIENTE")
        String role
) {
    public UsuarioRequestDTO(String username, String password) {
        this(username, password, null);
    }
}