package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.ResourceConflictException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ClienteResponseDTO create(Long usuarioId, ClienteRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(
                () -> new EntityNotFoundException("Usuário não encontrado"));
        if (usuario.getRole() != Usuario.Role.ROLE_CLIENTE) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Somente usuários com perfil CLIENTE podem cadastrar cliente");
        }
        if (clienteRepository.existsByUsuarioId(usuarioId)) {
            throw new ResourceConflictException("Este usuário já possui cadastro de cliente");
        }
        String cpf = request.cpf().replaceAll("\\D", "");
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ResourceConflictException("CPF já cadastrado");
        }
        Cliente cliente = new Cliente();
        String nomeCompleto = request.nomeCompleto().trim();
        cliente.setNomeCompleto(nomeCompleto);
        cliente.setNomeCompletoCompatibilidade(nomeCompleto);
        cliente.setCpf(cpf);
        cliente.setUsuario(usuario);
        return toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Transactional
    public ClienteResponseDTO getMine(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cadastro de cliente não encontrado"));
    }

    @Transactional
    public ClienteResponseDTO getById(Long id) {
        return toResponse(clienteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cliente com id=" + id + " não encontrado")));
    }

    @Transactional
    public List<ClienteResponseDTO> getAll() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    private ClienteResponseDTO toResponse(Cliente cliente) {
        return new ClienteResponseDTO(cliente.getId(), cliente.getNomeCompleto(), cliente.getCpf(),
                cliente.getUsuario().getId(), cliente.getUsuario().getUsername());
    }
}
