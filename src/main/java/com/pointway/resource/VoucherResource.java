package com.pointway.resource;

import com.pointway.bo.VoucherBO;
import com.pointway.dto.AtualizarVoucherRequest;
import com.pointway.dto.EmitirVoucherRequest;
import com.pointway.model.StatusVoucher;
import com.pointway.model.Voucher;
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
@RequestMapping("/vouchers")
public class VoucherResource {

    private final VoucherBO voucherBO;

    public VoucherResource(VoucherBO voucherBO) {
        this.voucherBO = voucherBO;
    }

    @GetMapping
    public ResponseEntity<List<Voucher>> listar(@RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) StatusVoucher status) throws SQLException {
        return ResponseEntity.ok(voucherBO.listar(usuarioId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> buscar(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(voucherBO.buscarPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Voucher> buscarPorCodigo(@PathVariable String codigo) throws SQLException {
        return ResponseEntity.ok(voucherBO.buscarPorCodigo(codigo));
    }

    /** Converte pontos do usuario em um voucher de transporte. */
    @PostMapping
    public ResponseEntity<Voucher> emitir(@RequestBody EmitirVoucherRequest dados) throws SQLException {
        Voucher voucher = voucherBO.emitir(dados);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(voucher.getId()).toUri();
        return ResponseEntity.created(local).body(voucher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> atualizar(@PathVariable int id, @RequestBody AtualizarVoucherRequest dados)
            throws SQLException {
        return ResponseEntity.ok(voucherBO.atualizar(id, dados));
    }

    /** Cancela o voucher (se ainda nao foi usado) e devolve os pontos ao usuario. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable int id) throws SQLException {
        voucherBO.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
