package br.edu.ifc.bikes.dto;

import br.edu.ifc.bikes.entity.Bicicleta;
import jakarta.validation.constraints.NotNull;

public record BicicletaStatusDTO(@NotNull Bicicleta.Status status) {
}
