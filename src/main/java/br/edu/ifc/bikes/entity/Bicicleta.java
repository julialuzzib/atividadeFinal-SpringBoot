package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bicicletas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bicicletas_codigo", columnNames = "codigo")
})
public class Bicicleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.LIVRE;

    public enum Status {
        LIVRE, OCUPADA, MANUTENCAO
    }
}
