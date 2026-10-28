package com.pointway.resource;

import com.pointway.bo.UsuarioBO;
import com.pointway.dto.PontosRequest;
import com.pointway.dto.ResumoUsuarioResponse;
import com.pointway.dto.UsuarioRequest;
import com.pointway.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioResource {

    private final UsuarioBO usuarioBO;

    public UsuarioResource(UsuarioBO usuarioBO) {
        this.usuarioBO = usuarioBO;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() throws SQLException {
        return ResponseEntity.ok(usuarioBO.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(usuarioBO.buscarPorId(id));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Usuario> buscarPorCpf(@PathVariable String cpf) throws SQLException {
        return ResponseEntity.ok(usuarioBO.buscarPorCpf(cpf));
    }

    @GetMapping("/{id}/resumo")
    public ResponseEntity<ResumoUsuarioResponse> resumo(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(usuarioBO.resumo(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> cadastrar(@RequestBody UsuarioRequest dados) throws SQLException {
        Usuario usuario = usuarioBO.cadastrar(dados);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(local).body(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable int id, @RequestBody UsuarioRequest dados)
            throws SQLException {
        return ResponseEntity.ok(usuarioBO.atualizar(id, dados));
    }

    @PatchMapping("/{id}/pontos")
    public ResponseEntity<Usuario> adicionarPontos(@PathVariable int id, @RequestBody PontosRequest dados)
            throws SQLException {
        return ResponseEntity.ok(usuarioBO.adicionarPontos(id, dados == null ? null : dados.pontos()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) throws SQLException {
        usuarioBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
