package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_clientes_cpf", columnNames = "cpf")
})
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompletoCompatibilidade;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}
