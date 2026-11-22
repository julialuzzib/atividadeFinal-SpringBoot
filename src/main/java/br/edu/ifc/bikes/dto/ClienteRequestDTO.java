package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotBlank
        @Pattern(regexp = "(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})",
                message = "O CPF deve conter 11 dígitos")
        String cpf
) {
}
