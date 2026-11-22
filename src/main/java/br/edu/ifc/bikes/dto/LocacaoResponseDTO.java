package br.edu.ifc.bikes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LocacaoResponseDTO(
        Long id,
        String nomeCliente,
        String codigoBicicleta,
        LocalDateTime dataRetirada,
        LocalDateTime devolucaoPrevista,
        LocalDateTime devolucaoEfetiva,
        Long duracaoMinutos,
        BigDecimal valorDebitado,
        String status,
        String cartaoFinal
) {
}
