package ml.dev.jasper.server.rest;

import ml.dev.common.dto.MediaDTO;
import ml.dev.common.dto.jasper.ReportRequestDTO;
import ml.dev.common.exception.MLException;
import ml.dev.common.rest.JsonHelper;
import ml.dev.jasper.server.service.JasperService;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("report")
public class JasperController {

    private final JasperService jasperService;
    @Autowired
    public Map<String, JasperReport> jasperMap;

    @Autowired
    public JasperController(JasperService jasperService) {
        this.jasperService = jasperService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity generateReport(@RequestPart(value = "report_request") String reportRequest, @RequestPart("report") MultipartFile reportFile) {
        try {
            ReportRequestDTO requestDTO = JsonHelper.jsonToObject(ReportRequestDTO.class, reportRequest);
            //if (!jasperMap.containsKey(requestDTO.getReportName()))
                jasperMap.put(requestDTO.getReportName(), jasperService.compile(reportFile.getBytes()));
            return ResponseEntity.ok(jasperService.generateReport(jasperMap.get(requestDTO.getReportName()), requestDTO));
        } catch (MLException | IOException e) {

            return ResponseEntity.status(500).build();
        }
    }

}