package com.pointway.dto;

import com.pointway.model.StatusVoucher;

import java.time.LocalDate;

/** Campos do voucher que podem ser alterados (validade e status). */
public record AtualizarVoucherRequest(LocalDate dataValidade, StatusVoucher status) {
}
