package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.ResourceConflictException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import br.edu.ifc.bikes.repository.LocacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BicicletaService {
    private final BicicletaRepository bicicletaRepository;
    private final LocacaoRepository locacaoRepository;

    public BicicletaResponseDTO create(BicicletaRequestDTO request) {
        if (bicicletaRepository.findByCodigo(request.codigo()).isPresent()) {
            throw new ResourceConflictException("Código de bicicleta já cadastrado");
        }
        Bicicleta.Status status = request.status() == null ? Bicicleta.Status.LIVRE : request.status();
        if (status == Bicicleta.Status.OCUPADA) {
            throw new ResourceConflictException("Uma bicicleta só pode ficar OCUPADA por meio de uma locação");
        }
        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setCodigo(request.codigo());
        bicicleta.setStatus(status);
        try {
            return toResponse(bicicletaRepository.save(bicicleta));
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("Código de bicicleta já cadastrado");
        }
    }

    @Transactional
    public BicicletaResponseDTO getByCodigo(String codigo) {
        return bicicletaRepository.findByCodigo(codigo).map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Bicicleta '" + codigo + "' não encontrada"));
    }

    @Transactional
    public List<BicicletaResponseDTO> getAll() {
        return bicicletaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public Bicicleta findAvailableForUpdate(String codigo) {
        Bicicleta bicicleta = bicicletaRepository.findByCodigoForUpdate(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Bicicleta '" + codigo + "' não encontrada"));
        if (bicicleta.getStatus() != Bicicleta.Status.LIVRE) {
            throw new ResourceConflictException("A bicicleta não está disponível para locação");
        }
        return bicicleta;
    }

    @Transactional
    public BicicletaResponseDTO updateStatus(String codigo, Bicicleta.Status status) {
        Bicicleta bicicleta = bicicletaRepository.findByCodigoForUpdate(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Bicicleta '" + codigo + "' não encontrada"));
        if (status == Bicicleta.Status.OCUPADA) {
            throw new ResourceConflictException("O status OCUPADA é controlado pelas locações");
        }
        if (locacaoRepository.existsByBicicleta_CodigoAndStatus(codigo, br.edu.ifc.bikes.entity.Locacao.Status.ATIVA)) {
            throw new ResourceConflictException("Não é possível alterar o status enquanto a bicicleta está locada");
        }
        bicicleta.setStatus(status);
        return toResponse(bicicletaRepository.save(bicicleta));
    }

    private BicicletaResponseDTO toResponse(Bicicleta bicicleta) {
        return new BicicletaResponseDTO(bicicleta.getId(), bicicleta.getCodigo(), bicicleta.getStatus().name());
    }
}
