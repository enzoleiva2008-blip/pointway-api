package com.pointway.resource;

import com.pointway.bo.OperadoraBO;
import com.pointway.dto.OperadoraRequest;
import com.pointway.model.OperadoraTransporte;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/operadoras")
public class OperadoraResource {

    private final OperadoraBO operadoraBO;

    public OperadoraResource(OperadoraBO operadoraBO) {
        this.operadoraBO = operadoraBO;
    }

    @GetMapping
    public ResponseEntity<List<OperadoraTransporte>> listar(@RequestParam(required = false) String cidade)
            throws SQLException {
        return ResponseEntity.ok(operadoraBO.listar(cidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperadoraTransporte> buscar(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(operadoraBO.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<OperadoraTransporte> cadastrar(@RequestBody OperadoraRequest dados) throws SQLException {
        OperadoraTransporte operadora = operadoraBO.cadastrar(dados);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(operadora.getId()).toUri();
        return ResponseEntity.created(local).body(operadora);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperadoraTransporte> atualizar(@PathVariable int id, @RequestBody OperadoraRequest dados)
            throws SQLException {
        return ResponseEntity.ok(operadoraBO.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) throws SQLException {
        operadoraBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
