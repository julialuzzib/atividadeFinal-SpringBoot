package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.BicicletaStatusDTO;
import br.edu.ifc.bikes.service.BicicletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bicicletas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BicicletaController {
    private final BicicletaService bicicletaService;

    @PostMapping
    public ResponseEntity<BicicletaResponseDTO> create(@Valid @RequestBody BicicletaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BicicletaResponseDTO>> getAll() {
        return ResponseEntity.ok(bicicletaService.getAll());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<BicicletaResponseDTO> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(bicicletaService.getByCodigo(codigo));
    }

    @PatchMapping("/{codigo}/status")
    public ResponseEntity<BicicletaResponseDTO> updateStatus(@PathVariable String codigo,
                                                               @Valid @RequestBody BicicletaStatusDTO request) {
        return ResponseEntity.ok(bicicletaService.updateStatus(codigo, request.status()));
    }
}
