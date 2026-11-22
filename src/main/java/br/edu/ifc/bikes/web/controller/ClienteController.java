package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@AuthenticationPrincipal JwtUserDetails principal,
                                                       @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.create(principal.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponseDTO> getMine(@AuthenticationPrincipal JwtUserDetails principal) {
        return ResponseEntity.ok(clienteService.getMine(principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> getAll() {
        return ResponseEntity.ok(clienteService.getAll());
    }
}
