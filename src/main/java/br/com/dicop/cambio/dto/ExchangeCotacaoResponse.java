package br.com.dicop.cambio.dto;

import java.math.BigDecimal;

public record ExchangeCotacaoResponse(
        String exchange,
        BigDecimal compra,
        BigDecimal venda,
        String timestamp,
        String linkNegociacao,
        String taxaTaker
) {
}
