package br.edu.ifc.bikes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LocacaoRequestDTO(
        @NotBlank String codigoBicicleta,
        @NotNull @Future LocalDateTime devolucaoPrevista,
        @NotNull @Valid CartaoDTO cartao
) {
}
