package br.com.dicop.cambio.dto;

import java.math.BigDecimal;

public record CotacaoHistoricaItemResponse(
        String data,
        BigDecimal compra,
        BigDecimal venda,
        BigDecimal maxima,
        BigDecimal minima
) {
}
