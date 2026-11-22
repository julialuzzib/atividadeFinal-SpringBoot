package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.service.JasperService;
import br.edu.ifc.bikes.service.LocacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {
    private final JasperService jasperService;
    private final LocacaoService locacaoService;

    @GetMapping(value = "/locacoes", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> locacoes() {
        byte[] pdf = jasperService.generatePdf(locacaoService.getReportRows());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-locacoes.pdf")
                .body(pdf);
    }
}
