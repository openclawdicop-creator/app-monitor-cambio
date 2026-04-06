package br.com.dicop.cambio.dto;

import java.math.BigDecimal;

public record CalculoCotacaoResponse(
        String moeda,
        String origem,
        String destino,
        BigDecimal valorInformado,
        BigDecimal cotacaoUtilizada,
        BigDecimal valorConvertido,
        String dataHoraCotacao
) {
}
