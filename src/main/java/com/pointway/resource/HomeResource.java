package com.pointway.resource;

import com.pointway.bo.ViagemBO;
import com.pointway.dto.ImpactoResponse;
import com.pointway.model.ModalTransporte;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeResource {

    private final ViagemBO viagemBO;

    public HomeResource(ViagemBO viagemBO) {
        this.viagemBO = viagemBO;
    }

    /** GET / - confirma que a API esta no ar (usado tambem como health check do deploy). */
    @GetMapping("/")
    public Map<String, Object> status() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Pointway");
        info.put("versao", "2.0.0");
        info.put("status", "online");
        info.put("recursos", List.of("/usuarios", "/operadoras", "/vouchers", "/viagens", "/impacto", "/modais"));
        return info;
    }

    /** GET /impacto - indicadores gerais para a pagina inicial do front-end. */
    @GetMapping("/impacto")
    public ResponseEntity<ImpactoResponse> impacto() throws SQLException {
        return ResponseEntity.ok(viagemBO.impactoGeral());
    }

    /** GET /modais - modais aceitos e o fator de CO2 de cada um (preenche o select do front). */
    @GetMapping("/modais")
    public List<Map<String, Object>> modais() {
        return Arrays.stream(ModalTransporte.values())
                .map(m -> Map.<String, Object>of("modal", m.name(),
                        "fatorReducaoCO2KgPorKm", m.getFatorReducaoCO2KgPorKm()))
                .toList();
    }
}
