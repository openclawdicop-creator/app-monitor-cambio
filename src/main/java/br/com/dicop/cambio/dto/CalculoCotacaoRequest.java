package br.com.dicop.cambio.dto;

import java.math.BigDecimal;

public record CalculoCotacaoRequest(
        String moeda,
        BigDecimal valor,
        String moedaOrigem,
        String moedaDestino
) {
}
