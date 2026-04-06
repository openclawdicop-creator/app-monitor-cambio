package br.com.dicop.cambio.dto;

import java.math.BigDecimal;

public record CotacaoAtualResponse(
        String moeda,
        String paridade,
        BigDecimal compra,
        BigDecimal venda,
        BigDecimal maxima,
        BigDecimal minima,
        BigDecimal variacaoPercentual,
        String dataHoraCotacao
) {
}
