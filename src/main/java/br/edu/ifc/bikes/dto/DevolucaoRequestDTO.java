package br.edu.ifc.bikes.dto;

public record DevolucaoRequestDTO(Boolean problemaManutencao) {
    public boolean deveIrParaManutencao() {
        return Boolean.TRUE.equals(problemaManutencao);
    }
}
