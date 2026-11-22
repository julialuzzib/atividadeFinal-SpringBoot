package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "locacoes")
public class Locacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bicicleta_id", nullable = false)
    private Bicicleta bicicleta;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @Column(name = "devolucao_prevista", nullable = false)
    private LocalDateTime devolucaoPrevista;

    @Column(name = "devolucao_efetiva")
    private LocalDateTime devolucaoEfetiva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ATIVA;

    @Column(name = "duracao_minutos")
    private Long duracaoMinutos;

    @Column(name = "valor_debitado", precision = 10, scale = 2)
    private BigDecimal valorDebitado;

    @Column(name = "cartao_final", length = 4)
    private String cartaoFinal;

    public enum Status {
        ATIVA, DEVOLVIDA
    }
}
