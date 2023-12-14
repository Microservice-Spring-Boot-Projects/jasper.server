package ml.dev.jasper.server.service;

import ml.dev.common.dto.MediaDTO;
import ml.dev.common.dto.jasper.ReportRequestDTO;
import ml.dev.common.exception.ExceptionCodes;
import ml.dev.common.exception.MLException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class JasperService {

    public JasperReport compile(byte[] jrxml) throws MLException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(jrxml)) {
            return JasperCompileManager.compileReport(bais);
        } catch (JRException | IOException jr) {
            throw new MLException(ExceptionCodes.NO_VALID_JSON_STRING, jr);
        }
    }

    public MediaDTO generateReport(JasperReport jasperReport, ReportRequestDTO requestDTO) throws MLException {
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, requestDTO.getParameters(), new JRBeanCollectionDataSource(requestDTO.getData()));
            ExporterConfig exporterConfig = getExporter(requestDTO.getReportType());
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                exporterConfig.getExporter().setExporterInput(new SimpleExporterInput(jasperPrint));
                exporterConfig.getExporter().setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                exporterConfig.getExporter().exportReport();
                return new MediaDTO(exporterConfig.getContentType(), outputStream.toByteArray(), requestDTO.getReportName());
            } catch (IOException e) {
                throw new MLException(ExceptionCodes.PRINTER_ERROR, e);
            }
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public ExporterConfig getExporter(ReportRequestDTO.ReportType type) throws MLException {
        switch (type) {
            case PDF:
                return new ExporterConfig(type.name(), MediaType.APPLICATION_PDF_VALUE, new JRPdfExporter());
            case XML:
                return new ExporterConfig(type.name(), MediaType.APPLICATION_XML_VALUE, new JRXmlExporter());
            case DOC:
                return new ExporterConfig(type.name(), MediaType.APPLICATION_OCTET_STREAM_VALUE, new JRDocxExporter());
        }
        throw new MLException(ExceptionCodes.NO_VALID_JSON_STRING, "No config found for type " + type.name());
    }

    private class ExporterConfig {

        private String type;

        private String contentType;

        private JRAbstractExporter exporter;

        public ExporterConfig(String type, String contentType, JRAbstractExporter exporter) {
            this.type = type;
            this.contentType = contentType;
            this.exporter = exporter;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public JRAbstractExporter getExporter() {
            return exporter;
        }

        public void setExporter(JRAbstractExporter exporter) {
            this.exporter = exporter;
        }
    }

}
