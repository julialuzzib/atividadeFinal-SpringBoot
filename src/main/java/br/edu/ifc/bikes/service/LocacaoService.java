package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.*;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.ResourceConflictException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.LocacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocacaoService {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LocacaoRepository locacaoRepository;
    private final ClienteRepository clienteRepository;
    private final BicicletaService bicicletaService;

    @Value("${bikes.valor-hora:10.00}")
    private BigDecimal valorHora;

    @Transactional
    public LocacaoResponseDTO create(Long usuarioId, LocacaoRequestDTO request) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Cadastre-se como cliente antes de solicitar uma locação"));
        validateCard(request.cartao());
        Bicicleta bicicleta = bicicletaService.findAvailableForUpdate(request.codigoBicicleta());

        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setBicicleta(bicicleta);
        locacao.setDataRetirada(LocalDateTime.now());
        locacao.setDevolucaoPrevista(request.devolucaoPrevista());
        locacao.setStatus(Locacao.Status.ATIVA);
        String numero = request.cartao().numero().replaceAll("\\s", "");
        locacao.setCartaoFinal(numero.substring(numero.length() - 4));
        bicicleta.setStatus(Bicicleta.Status.OCUPADA);
        return toResponse(locacaoRepository.save(locacao));
    }

    @Transactional
    public LocacaoResponseDTO returnBike(Long locacaoId, Long usuarioId, boolean isAdmin,
                                         DevolucaoRequestDTO request) {
        Locacao locacao = locacaoRepository.findByIdForUpdate(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locação com id=" + locacaoId + " não encontrada"));
        if (!isAdmin && !locacao.getCliente().getUsuario().getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você só pode devolver bicicletas de suas próprias locações");
        }
        if (locacao.getStatus() != Locacao.Status.ATIVA) {
            throw new ResourceConflictException("Esta locação já foi devolvida");
        }

        LocalDateTime returnedAt = LocalDateTime.now();
        long seconds = Math.max(0, Duration.between(locacao.getDataRetirada(), returnedAt).getSeconds());
        long minutes = (seconds + 59) / 60;
        long billableHours = Math.max(1, (seconds + 3599) / 3600);
        BigDecimal amount = valorHora.multiply(BigDecimal.valueOf(billableHours))
                .setScale(2, RoundingMode.HALF_UP);

        locacao.setDevolucaoEfetiva(returnedAt);
        locacao.setDuracaoMinutos(minutes);
        locacao.setValorDebitado(amount);
        locacao.setStatus(Locacao.Status.DEVOLVIDA);
        locacao.getBicicleta().setStatus(request != null && request.deveIrParaManutencao()
                ? Bicicleta.Status.MANUTENCAO : Bicicleta.Status.LIVRE);
        return toResponse(locacaoRepository.save(locacao));
    }

    @Transactional
    public List<LocacaoResponseDTO> getAll() {
        return locacaoRepository.findAllByOrderByDataRetiradaDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<RelatorioLocacaoDTO> getReportRows() {
        return locacaoRepository.findAllByOrderByDataRetiradaDesc().stream().map(locacao -> {
            LocalDateTime end = locacao.getDevolucaoEfetiva() == null
                    ? LocalDateTime.now() : locacao.getDevolucaoEfetiva();
            long minutes = Duration.between(locacao.getDataRetirada(), end).toMinutes();
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            String duration = hours + "h " + remainingMinutes + "min";
            return new RelatorioLocacaoDTO(
                    locacao.getCliente().getNomeCompleto(),
                    locacao.getBicicleta().getCodigo(),
                    locacao.getDataRetirada().format(DATE_FORMAT),
                    locacao.getDevolucaoEfetiva() == null ? "Pendente"
                            : locacao.getDevolucaoEfetiva().format(DATE_FORMAT),
                    duration,
                    locacao.getStatus().name());
        }).toList();
    }

    private void validateCard(CartaoDTO card) {
        String number = card.numero().replaceAll("\\s", "");
        if (number.length() < 13 || number.length() > 19) {
            throw new IllegalArgumentException("Número do cartão inválido");
        }
        YearMonth expiration = YearMonth.parse(card.validade(),
                DateTimeFormatter.ofPattern("MM/yy"));
        if (expiration.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("O cartão está vencido");
        }
    }

    private LocacaoResponseDTO toResponse(Locacao locacao) {
        return new LocacaoResponseDTO(locacao.getId(), locacao.getCliente().getNomeCompleto(),
                locacao.getBicicleta().getCodigo(), locacao.getDataRetirada(),
                locacao.getDevolucaoPrevista(), locacao.getDevolucaoEfetiva(),
                locacao.getDuracaoMinutos(), locacao.getValorDebitado(),
                locacao.getStatus().name(), locacao.getCartaoFinal());
    }
}
