package br.com.dicop.cambio.dto;

import java.util.List;

public record CotacaoHistoricoResponse(
        String moeda,
        String paridade,
        int dias,
        List<CotacaoHistoricaItemResponse> historico
) {
}
