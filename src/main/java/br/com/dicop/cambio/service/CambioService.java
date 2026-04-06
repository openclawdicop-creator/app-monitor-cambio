package br.com.dicop.cambio.service;

import br.com.dicop.cambio.client.AwesomeApiClient;
import br.com.dicop.cambio.client.dto.AwesomeHistoricalRateResponse;
import br.com.dicop.cambio.client.dto.AwesomeLatestRateResponse;
import br.com.dicop.cambio.client.dto.AwesomeRateItem;
import br.com.dicop.cambio.dto.CalculoCotacaoResponse;
import br.com.dicop.cambio.dto.CotacaoAtualResponse;
import br.com.dicop.cambio.dto.CotacaoHistoricaItemResponse;
import br.com.dicop.cambio.dto.CotacaoHistoricoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CambioService {

    private static final int HISTORICO_DIAS = 30;
    private static final String MOEDA_BASE = "BRL";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @RestClient
    AwesomeApiClient awesomeApiClient;

    public CotacaoAtualResponse buscarCotacaoAtual(String moeda) {
        String codigoMoeda = normalizarMoeda(moeda);
        AwesomeRateItem rate = obterCotacaoAtual(codigoMoeda);
        return new CotacaoAtualResponse(
                codigoMoeda,
                codigoMoeda + "-" + MOEDA_BASE,
                toBigDecimal(rate.bid),
                toBigDecimal(rate.ask),
                toBigDecimal(rate.high),
                toBigDecimal(rate.low),
                toBigDecimal(rate.pctChange),
                formatarData(rate.timestamp)
        );
    }

    public CotacaoHistoricoResponse buscarHistorico(String moeda) {
        String codigoMoeda = normalizarMoeda(moeda);
        AwesomeHistoricalRateResponse[] response = awesomeApiClient.getHistory(codigoMoeda + "-" + MOEDA_BASE, HISTORICO_DIAS);

        List<CotacaoHistoricaItemResponse> historico = Arrays.stream(response)
                .map(item -> new CotacaoHistoricaItemResponse(
                        formatarDataSomenteDia(item.timestamp),
                        toBigDecimal(item.bid),
                        toBigDecimal(item.ask),
                        toBigDecimal(item.high),
                        toBigDecimal(item.low)
                ))
                .sorted(Comparator.comparing(CotacaoHistoricaItemResponse::data))
                .toList();

        return new CotacaoHistoricoResponse(codigoMoeda, codigoMoeda + "-" + MOEDA_BASE, historico.size(), historico);
    }

    public CalculoCotacaoResponse calcularConversao(String moeda, BigDecimal valor, String origem, String destino) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor informado deve ser maior que zero.");
        }

        String codigoMoeda = normalizarMoeda(moeda);
        String origemNormalizada = origem == null ? MOEDA_BASE : origem.trim().toUpperCase();
        String destinoNormalizado = destino == null ? codigoMoeda : destino.trim().toUpperCase();

        if (origemNormalizada.equals(destinoNormalizado)) {
            throw new BadRequestException("Origem e destino nao podem ser iguais.");
        }

        AwesomeRateItem rate = obterCotacaoAtual(codigoMoeda);
        BigDecimal cotacaoVenda = toBigDecimal(rate.ask);
        BigDecimal valorConvertido;

        if (MOEDA_BASE.equals(origemNormalizada) && codigoMoeda.equals(destinoNormalizado)) {
            valorConvertido = valor.divide(cotacaoVenda, 4, RoundingMode.HALF_UP);
        } else if (codigoMoeda.equals(origemNormalizada) && MOEDA_BASE.equals(destinoNormalizado)) {
            valorConvertido = valor.multiply(cotacaoVenda).setScale(4, RoundingMode.HALF_UP);
        } else {
            throw new BadRequestException("Use apenas conversoes entre BRL e " + codigoMoeda + ".");
        }

        return new CalculoCotacaoResponse(
                codigoMoeda,
                origemNormalizada,
                destinoNormalizado,
                valor.setScale(4, RoundingMode.HALF_UP),
                cotacaoVenda,
                valorConvertido,
                formatarData(rate.timestamp)
        );
    }

    private AwesomeRateItem obterCotacaoAtual(String moeda) {
        AwesomeLatestRateResponse response = awesomeApiClient.getLatest(moeda + "-" + MOEDA_BASE);
        return response.getRates().values().stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Nao foi possivel obter a cotacao para a moeda " + moeda + "."));
    }

    private String normalizarMoeda(String moeda) {
        if (moeda == null || moeda.isBlank()) {
            return "USD";
        }
        return moeda.trim().toUpperCase();
    }

    private BigDecimal toBigDecimal(String value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private String formatarData(String timestamp) {
        return FORMATTER.format(Instant.ofEpochSecond(Long.parseLong(timestamp)));
    }

    private String formatarDataSomenteDia(String timestamp) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(Long.parseLong(timestamp)));
    }
}
