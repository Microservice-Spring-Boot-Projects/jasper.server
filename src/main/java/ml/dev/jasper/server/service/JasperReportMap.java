package ml.dev.jasper.server.service;

import net.sf.jasperreports.engine.JasperReport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JasperReportMap {

    @Bean
    @Scope(value = "singleton")
    public Map<String, JasperReport> jasperMap() {
        return new HashMap<>();
    }

}
