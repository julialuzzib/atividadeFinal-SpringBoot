package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO create(UsuarioRequestDTO usuarioRequestDTO) {
        String username = normalizeUsername(usuarioRequestDTO.username());
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new UsernameUniqueViolationException(
                    String.format("O e-mail '%s' já está cadastrado", username));
        }
        Usuario usuario = usuarioMapper.toUsuario(usuarioRequestDTO);
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(usuarioRequestDTO.password()));
        usuario.setRole(toRole(usuarioRequestDTO.role()));
        try {
            return usuarioMapper.toResponse(usuarioRepository.save(usuario));
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameUniqueViolationException(
                    String.format("O e-mail '%s' já está cadastrado", username));
        }
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario.Role toRole(String role) {
        if (role == null || role.isBlank()) {
            return Usuario.Role.ROLE_CLIENTE;
        }
        return switch (role) {
            case "ADMIN", "ROLE_ADMIN" -> Usuario.Role.ROLE_ADMIN;
            case "CLIENTE", "ROLE_CLIENTE" -> Usuario.Role.ROLE_CLIENTE;
            default -> throw new IllegalArgumentException("Perfil inválido");
        };
    }

    @Transactional()
    public UsuarioResponseDTO getById(Long id) {
        return usuarioMapper.toResponse(usuarioRepository.findById(id).orElseThrow(
                ()->new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id))
        ));
    }

    public UsuarioResponseDTO updatePassword(Long id, String novaSenha, String confirmaSenha, String senhaAtual) {
        if (!novaSenha.equals(confirmaSenha)){
            throw new PasswordInvalidException("As senhas não coincidem");
        }
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(
                ()->new EntityNotFoundException(String.format("Usuário %s não encontrado", id))
        );
        if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())){
            throw new PasswordInvalidException("Senha atual incorreta");
        }
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> getAll(){
        return usuarioMapper.toResponse(usuarioRepository.findAll());
    }

    @Transactional()
    public Usuario buscarPorNome(String username) {
        return usuarioRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(()->new EntityNotFoundException(String.format("Usuário '%s' não encontrado", username)));
    }

    public Usuario.Role buscarRolePorUsername(String username){
        return usuarioRepository.findRoleByUsername(normalizeUsername(username));
    }

}