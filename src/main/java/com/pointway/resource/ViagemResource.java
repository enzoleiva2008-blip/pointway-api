package com.pointway.resource;

import com.pointway.bo.ViagemBO;
import com.pointway.dto.AtualizarViagemRequest;
import com.pointway.dto.ViagemRequest;
import com.pointway.model.Viagem;
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
@RequestMapping("/viagens")
public class ViagemResource {

    private final ViagemBO viagemBO;

    public ViagemResource(ViagemBO viagemBO) {
        this.viagemBO = viagemBO;
    }

    @GetMapping
    public ResponseEntity<List<Viagem>> listar(@RequestParam(required = false) Integer usuarioId)
            throws SQLException {
        return ResponseEntity.ok(viagemBO.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Viagem> buscar(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(viagemBO.buscarPorId(id));
    }

    /** Usa o voucher na catraca: registra a viagem e marca o voucher como UTILIZADO. */
    @PostMapping
    public ResponseEntity<Viagem> registrar(@RequestBody ViagemRequest dados) throws SQLException {
        Viagem viagem = viagemBO.registrar(dados);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(viagem.getId()).toUri();
        return ResponseEntity.created(local).body(viagem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Viagem> atualizar(@PathVariable int id, @RequestBody AtualizarViagemRequest dados)
            throws SQLException {
        return ResponseEntity.ok(viagemBO.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) throws SQLException {
        viagemBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
