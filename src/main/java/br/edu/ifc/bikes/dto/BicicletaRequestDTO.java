package br.edu.ifc.bikes.dto;

import br.edu.ifc.bikes.entity.Bicicleta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BicicletaRequestDTO(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{2,40}",
                message = "O código deve ter 2 a 40 caracteres alfanuméricos, '_' ou '-'")
        String codigo,
        Bicicleta.Status status
) {
}
