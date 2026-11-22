package br.edu.ifc.bikes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import br.edu.ifc.bikes.dto.RelatorioLocacaoDTO;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class JasperService {

    private final ResourceLoader resourceLoader;

    public byte[] generatePdf(List<RelatorioLocacaoDTO> rows) {
        Resource resource = resourceLoader.getResource("classpath:reports/locacoes.jrxml");
        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var report = JasperCompileManager.compileReport(inputStream);
            var dataSource = new JRBeanCollectionDataSource(rows);
            var print = JasperFillManager.fillReport(report, new HashMap<>(), dataSource);
            JasperExportManager.exportReportToPdfStream(print, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Não foi possível gerar o relatório de locações", e);
            throw new IllegalStateException("Falha ao gerar o relatório PDF de locações", e);
        }
    }
}
