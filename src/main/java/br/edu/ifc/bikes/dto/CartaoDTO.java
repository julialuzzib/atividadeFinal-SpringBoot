package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CartaoDTO(
        @NotBlank @Size(max = 100) String nomeTitular,
        @NotBlank @Pattern(regexp = "[0-9 ]{13,23}", message = "Número do cartão inválido")
        String numero,
        @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Use validade no formato MM/AA")
        String validade,
        @NotBlank @Pattern(regexp = "\\d{3,4}", message = "O CVV deve conter 3 ou 4 dígitos")
        String cvv
) {
}
