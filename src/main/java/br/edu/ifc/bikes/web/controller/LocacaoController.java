package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.DevolucaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.LocacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locacoes")
@RequiredArgsConstructor
public class LocacaoController {
    private final LocacaoService locacaoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<LocacaoResponseDTO> create(@AuthenticationPrincipal JwtUserDetails principal,
                                                       @Valid @RequestBody LocacaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locacaoService.create(principal.getId(), request));
    }

    @PostMapping("/{id}/devolucao")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<LocacaoResponseDTO> returnBike(@PathVariable Long id,
                                                           @AuthenticationPrincipal JwtUserDetails principal,
                                                           @RequestBody(required = false) DevolucaoRequestDTO request) {
        return ResponseEntity.ok(locacaoService.returnBike(id, principal.getId(),
                "ADMIN".equals(principal.getRole()), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocacaoResponseDTO>> getAll() {
        return ResponseEntity.ok(locacaoService.getAll());
    }
}
