package br.com.dicop.cambio.service;

import br.com.dicop.cambio.client.BinanceExchangeClient;
import br.com.dicop.cambio.client.BitgetExchangeClient;
import br.com.dicop.cambio.client.BitmartExchangeClient;
import br.com.dicop.cambio.client.BitprecoExchangeClient;
import br.com.dicop.cambio.client.BybitExchangeClient;
import br.com.dicop.cambio.client.KuCoinExchangeClient;
import br.com.dicop.cambio.client.MexcExchangeClient;
import br.com.dicop.cambio.client.OkxExchangeClient;
import br.com.dicop.cambio.client.dto.BinanceOrderBookResponse;
import br.com.dicop.cambio.client.dto.BitgetOrderBookResponse;
import br.com.dicop.cambio.client.dto.BitmartOrderBookResponse;
import br.com.dicop.cambio.client.dto.BitprecoOrderBookResponse;
import br.com.dicop.cambio.client.dto.BybitOrderBookResponse;
import br.com.dicop.cambio.client.dto.KuCoinOrderBookResponse;
import br.com.dicop.cambio.client.dto.MexcOrderBookResponse;
import br.com.dicop.cambio.client.dto.OkxOrderBookResponse;
import br.com.dicop.cambio.dto.ExchangeCotacaoRequest;
import br.com.dicop.cambio.dto.ExchangeCotacaoResponse;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ExchangeCotacaoService {

    private static final Logger LOG = Logger.getLogger(ExchangeCotacaoService.class);
    private static final int TOP_LEVELS = 5;
    private static final int SCALE = 8;
    private static final String BINANCE_SYMBOL = "USDTBRL";
    private static final String BITGET_SYMBOL = "USDTBRL";
    private static final String BITMART_SYMBOL = "BRLUSDT";
    private static final String KUCOIN_SYMBOL = "USDT-BRL";
    private static final String MEXC_SYMBOL = "BRLUSDT";
    private static final String OKX_INST_ID = "USDT-BRL";
    private static final String BYBIT_SYMBOL = "USDTBRL";

    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    @RestClient
    BinanceExchangeClient binanceExchangeClient;

    @RestClient
    BitgetExchangeClient bitgetExchangeClient;

    @RestClient
    BitmartExchangeClient bitmartExchangeClient;

    @RestClient
    BitprecoExchangeClient bitprecoExchangeClient;

    @RestClient
    KuCoinExchangeClient kuCoinExchangeClient;

    @RestClient
    MexcExchangeClient mexcExchangeClient;

    @RestClient
    OkxExchangeClient okxExchangeClient;

    @RestClient
    BybitExchangeClient bybitExchangeClient;

    public List<ExchangeCotacaoResponse> buscarCotacoes(ExchangeCotacaoRequest request) {
        BigDecimal valorBRL = validarValor(request);

        CompletableFuture<ExchangeCotacaoResponse> binanceFuture = CompletableFuture
                .supplyAsync(() -> consultarBinance(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("Binance", throwable));

        CompletableFuture<ExchangeCotacaoResponse> bitgetFuture = CompletableFuture
                .supplyAsync(() -> consultarBitget(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("Bitget", throwable));

        CompletableFuture<ExchangeCotacaoResponse> bitmartFuture = CompletableFuture
                .supplyAsync(() -> consultarBitmart(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("Bitmart", throwable));

        CompletableFuture<ExchangeCotacaoResponse> bitprecoFuture = CompletableFuture
                .supplyAsync(() -> consultarBitpreco(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("BitPreco", throwable));

        CompletableFuture<ExchangeCotacaoResponse> kucoinFuture = CompletableFuture
                .supplyAsync(() -> consultarKuCoin(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("KuCoin", throwable));

        CompletableFuture<ExchangeCotacaoResponse> mexcFuture = CompletableFuture
                .supplyAsync(() -> consultarMexc(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("MEXC", throwable));

        CompletableFuture<ExchangeCotacaoResponse> okxFuture = CompletableFuture
                .supplyAsync(() -> consultarOkx(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("OKX", throwable));

        CompletableFuture<ExchangeCotacaoResponse> bybitFuture = CompletableFuture
                .supplyAsync(() -> consultarBybit(valorBRL), executor)
                .exceptionally(throwable -> tratarFalha("Bybit", throwable));

        CompletableFuture.allOf(binanceFuture, bitgetFuture, bitmartFuture, bitprecoFuture, kucoinFuture, mexcFuture, okxFuture, bybitFuture).join();

        List<ExchangeCotacaoResponse> cotacoes = new ArrayList<>();
        if (binanceFuture.join() != null) cotacoes.add(binanceFuture.join());
        if (bitgetFuture.join() != null) cotacoes.add(bitgetFuture.join());
        if (bitmartFuture.join() != null) cotacoes.add(bitmartFuture.join());
        if (bitprecoFuture.join() != null) cotacoes.add(bitprecoFuture.join());
        if (kucoinFuture.join() != null) cotacoes.add(kucoinFuture.join());
        if (mexcFuture.join() != null) cotacoes.add(mexcFuture.join());
        if (okxFuture.join() != null) cotacoes.add(okxFuture.join());
        if (bybitFuture.join() != null) cotacoes.add(bybitFuture.join());
        
        cotacoes.sort(Comparator.comparing(ExchangeCotacaoResponse::compra));

        if (cotacoes.isEmpty()) {
            throw new WebApplicationException(
                    "Nao foi possivel consultar nenhuma exchange no momento.",
                    Response.Status.BAD_GATEWAY
            );
        }

        return cotacoes;
    }

    @PreDestroy
    void shutdownExecutor() {
        executor.shutdown();
    }

    private ExchangeCotacaoResponse consultarBinance(BigDecimal valorBRL) {
        try {
            BinanceOrderBookResponse response = binanceExchangeClient.getOrderBook(BINANCE_SYMBOL, TOP_LEVELS);
            return montarCotacao("Binance", response.asks, response.bids, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar Binance: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarBitget(BigDecimal valorBRL) {
        try {
            BitgetOrderBookResponse response = bitgetExchangeClient.getOrderBook(BITGET_SYMBOL, String.valueOf(TOP_LEVELS));
            if (response == null || response.data == null || (response.code != null && !"0".equals(response.code) && !"00000".equals(response.code))) {
                throw new WebApplicationException("Resposta invalida da Bitget: " + (response != null ? response.msg : "null"), Response.Status.BAD_GATEWAY);
            }
            return montarCotacao("Bitget", response.data.asks, response.data.bids, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar Bitget: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarBitmart(BigDecimal valorBRL) {
        try {
            BitmartOrderBookResponse response = bitmartExchangeClient.getOrderBook(BITMART_SYMBOL);
            if (response == null || response.data == null || response.data.asks == null || response.data.bids == null) {
                throw new WebApplicationException("Resposta invalida da Bitmart.", Response.Status.BAD_GATEWAY);
            }
            
            // A Bitmart retorna preco em USDT/BRL (invertido) para o par BRLUSDT no mercado futuro
            // Exemplo: 0.1931 USDT por 1 BRL
            // Para obter BRL/USD (quanto BRL por 1 USD), invertemos: 1 / 0.1931 = 5.178 BRL/USD
            // Usar 20 niveis para Bitmart devido a menor liquidez por nivel no mercado futuro
            // Passar asks primeiro (para compra) e bids depois (para venda), depois inverter o resultado final
            ExchangeCotacaoResponse cotacao = montarCotacao("Bitmart", response.data.asks, response.data.bids, valorBRL, 20);
            BigDecimal precoCompra = cotacao.compra();
            
            // Inverte o preco: 1 / preco_usd_brl para obter brl_usd
            BigDecimal novaCompra = BigDecimal.ONE.divide(cotacao.venda(), SCALE, RoundingMode.HALF_UP);
            BigDecimal novaVenda = BigDecimal.ONE.divide(cotacao.compra(), SCALE, RoundingMode.HALF_UP);
            
            return new ExchangeCotacaoResponse(
                    cotacao.exchange(),
                    novaCompra,
                    novaVenda,
                    cotacao.timestamp(),
                    cotacao.linkNegociacao(),
                    cotacao.taxaTaker()
            );
        } catch (Exception e) {
            LOG.warn("Falha ao consultar Bitmart: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarBitpreco(BigDecimal valorBRL) {
        try {
            BitprecoOrderBookResponse response = bitprecoExchangeClient.getOrderBook();
            if (response == null || response.getAsks() == null || response.getBids() == null) {
                throw new WebApplicationException("Resposta invalida da BitPreco.", Response.Status.BAD_GATEWAY);
            }
            List<List<String>> asksStr = response.getAsks().stream()
                    .limit(TOP_LEVELS)
                    .map(o -> List.of(String.valueOf(o.getPrice()), String.valueOf(o.getAmount())))
                    .toList();
            List<List<String>> bidsStr = response.getBids().stream()
                    .limit(TOP_LEVELS)
                    .map(o -> List.of(String.valueOf(o.getPrice()), String.valueOf(o.getAmount())))
                    .toList();

            return montarCotacao("BitPreco", asksStr, bidsStr, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar BitPreco: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarKuCoin(BigDecimal valorBRL) {
        try {
            KuCoinOrderBookResponse response = kuCoinExchangeClient.getOrderBook(KUCOIN_SYMBOL);
            if (response == null || response.data == null) {
                throw new WebApplicationException("Resposta invalida da KuCoin.", Response.Status.BAD_GATEWAY);
            }
            return montarCotacao("KuCoin", response.data.asks, response.data.bids, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar KuCoin: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarMexc(BigDecimal valorBRL) {
        try {
            MexcOrderBookResponse response = mexcExchangeClient.getOrderBook(MEXC_SYMBOL, TOP_LEVELS);
            if (response == null || response.bids == null || response.asks == null) {
                throw new WebApplicationException("Resposta invalida da MEXC.", Response.Status.BAD_GATEWAY);
            }
            
            // A MEXC pode retornar preco invertido (USDT por BRL)
            // Se o preco medio for < 1, significa que eh o valor de 1 BRL em USDT
            // Nesse caso, precisamos inverter para obter o valor de 1 USDT em BRL
            ExchangeCotacaoResponse cotacao = montarCotacao("MEXC", response.asks, response.bids, valorBRL);
            BigDecimal precoCompra = cotacao.compra();
            
            if (precoCompra.compareTo(BigDecimal.ONE) < 0) {
                // Preco invertido: era USDT/BRL, precisa inverter para BRL/USDT
                BigDecimal novaCompra = BigDecimal.ONE.divide(cotacao.venda(), SCALE, RoundingMode.HALF_UP);
                BigDecimal novaVenda = BigDecimal.ONE.divide(cotacao.compra(), SCALE, RoundingMode.HALF_UP);
                
                return new ExchangeCotacaoResponse(
                        cotacao.exchange(),
                        novaCompra,
                        novaVenda,
                        cotacao.timestamp(),
                        cotacao.linkNegociacao(),
                        cotacao.taxaTaker()
                );
            }
            
            return cotacao;
        } catch (Exception e) {
            LOG.warn("Falha ao consultar MEXC: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarOkx(BigDecimal valorBRL) {
        try {
            OkxOrderBookResponse response = okxExchangeClient.getOrderBook(OKX_INST_ID, TOP_LEVELS);
            if (response == null || response.data == null || response.data.isEmpty() || response.data.get(0) == null) {
                throw new WebApplicationException("Resposta invalida da OKX.", Response.Status.BAD_GATEWAY);
            }
            return montarCotacao("OKX", response.data.get(0).asks, response.data.get(0).bids, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar OKX: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse consultarBybit(BigDecimal valorBRL) {
        try {
            BybitOrderBookResponse response = bybitExchangeClient.getOrderBook("spot", BYBIT_SYMBOL, TOP_LEVELS);
            if (response == null || response.result == null || response.result.b == null || response.result.a == null) {
                throw new WebApplicationException("Resposta invalida da Bybit.", Response.Status.BAD_GATEWAY);
            }
            return montarCotacao("Bybit", response.result.a, response.result.b, valorBRL);
        } catch (Exception e) {
            LOG.warn("Falha ao consultar Bybit: " + e.getMessage());
            throw e;
        }
    }

    private ExchangeCotacaoResponse montarCotacao(
            String exchange,
            List<List<String>> asks,
            List<List<String>> bids,
            BigDecimal valorBRL
    ) {
        return montarCotacao(exchange, asks, bids, valorBRL, TOP_LEVELS);
    }

    private ExchangeCotacaoResponse montarCotacao(
            String exchange,
            List<List<String>> asks,
            List<List<String>> bids,
            BigDecimal valorBRL,
            int topLevels
    ) {
        List<BookLevel> askLevels = converterLevels(asks, topLevels);
        List<BookLevel> bidLevels = converterLevels(bids, topLevels);

        CompraResult compra = calcularCompraPorValor(askLevels, valorBRL);
        BigDecimal venda = calcularVendaPorQuantidade(bidLevels, compra.quantidadeUsdt());
        String linkNegociacao = obterLinkNegociacao(exchange);
        
        String taxaTaker;
        if (exchange.equalsIgnoreCase("MEXC")) {
            taxaTaker = "0%";  // MEXC tem taxa taker zero desde 2026
        } else if (exchange.equalsIgnoreCase("Bitmart")) {
            taxaTaker = "0.1%";  // Bitmart futures taxa padrao
        } else if (exchange.equalsIgnoreCase("BitPreco") || exchange.equalsIgnoreCase("BitPreço") || exchange.equalsIgnoreCase("Bity")) {
            taxaTaker = "0.2%";
        } else {
            taxaTaker = "0.1%";
        }

        return new ExchangeCotacaoResponse(
                exchange,
                compra.precoMedio(),
                venda,
                Instant.now().toString(),
                linkNegociacao,
                taxaTaker
        );
    }

    private String obterLinkNegociacao(String exchange) {
        return switch (exchange) {
            case "Binance" -> "https://www.binance.com/pt/trade/USDT_BRL";
            case "Bitget" -> "https://www.bitget.com/spot/USDTBRL";
            case "Bitmart" -> "https://www.bitmart.com/futures/en?symbol=BRLUSDT";
            case "BitPreco" -> "https://bity.com.br/";
            case "KuCoin" -> "https://www.kucoin.com/trade/USDT-BRL";
            case "MEXC" -> "https://www.mexc.com/exchange/BRL_USDT";
            case "OKX" -> "https://www.okx.com/pt/trade-spot/usdt-brl";
            case "Bybit" -> "https://www.bybit.com/trade/spot/USDTBRL";
            default -> null;
        };
    }

    private CompraResult calcularCompraPorValor(List<BookLevel> asks, BigDecimal valorBRL) {
        BigDecimal restante = valorBRL;
        BigDecimal totalGasto = BigDecimal.ZERO;
        BigDecimal quantidadeComprada = BigDecimal.ZERO;

        for (BookLevel ask : asks) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal custoNivel = ask.preco().multiply(ask.quantidade());
            BigDecimal valorConsumido = restante.min(custoNivel);
            BigDecimal quantidadeConsumida = valorConsumido.divide(ask.preco(), SCALE, RoundingMode.HALF_UP);

            totalGasto = totalGasto.add(valorConsumido);
            quantidadeComprada = quantidadeComprada.add(quantidadeConsumida);
            restante = restante.subtract(valorConsumido);
        }

        if (quantidadeComprada.compareTo(BigDecimal.ZERO) <= 0 || restante.compareTo(BigDecimal.ZERO) > 0) {
            throw new WebApplicationException(
                    "Liquidez insuficiente nas ordens de compra.",
                    Response.Status.BAD_GATEWAY
            );
        }

        BigDecimal precoMedio = totalGasto.divide(quantidadeComprada, SCALE, RoundingMode.HALF_UP);
        return new CompraResult(precoMedio, quantidadeComprada);
    }

    private BigDecimal calcularVendaPorQuantidade(List<BookLevel> bids, BigDecimal quantidadeUsdt) {
        BigDecimal restante = quantidadeUsdt;
        BigDecimal totalRecebido = BigDecimal.ZERO;
        BigDecimal totalVendido = BigDecimal.ZERO;

        for (BookLevel bid : bids) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal quantidadeConsumida = restante.min(bid.quantidade());
            BigDecimal valorRecebido = quantidadeConsumida.multiply(bid.preco());

            totalVendido = totalVendido.add(quantidadeConsumida);
            totalRecebido = totalRecebido.add(valorRecebido);
            restante = restante.subtract(quantidadeConsumida);
        }

        if (totalVendido.compareTo(BigDecimal.ZERO) <= 0 || restante.compareTo(BigDecimal.ZERO) > 0) {
            throw new WebApplicationException(
                    "Liquidez insuficiente nas ordens de venda.",
                    Response.Status.BAD_GATEWAY
            );
        }

        return totalRecebido.divide(totalVendido, SCALE, RoundingMode.HALF_UP);
    }

    private List<BookLevel> converterLevels(List<List<String>> rawLevels) {
        return converterLevels(rawLevels, TOP_LEVELS);
    }

    private List<BookLevel> converterLevels(List<List<String>> rawLevels, int topLevels) {
        if (rawLevels == null || rawLevels.isEmpty()) {
            throw new WebApplicationException("Order book vazio.", Response.Status.BAD_GATEWAY);
        }

        List<BookLevel> levels = new ArrayList<>();
        for (List<String> rawLevel : rawLevels.stream().limit(topLevels).toList()) {
            if (rawLevel == null || rawLevel.size() < 2) {
                continue;
            }
            levels.add(new BookLevel(new BigDecimal(rawLevel.get(0)), new BigDecimal(rawLevel.get(1))));
        }

        if (levels.isEmpty()) {
            throw new WebApplicationException("Order book invalido.", Response.Status.BAD_GATEWAY);
        }

        return levels;
    }

    private ExchangeCotacaoResponse tratarFalha(String exchange, Throwable throwable) {
        Throwable causa = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        LOG.warnf("Falha ao consultar a exchange %s: %s", exchange, causa.getMessage());
        return null;
    }

    private BigDecimal validarValor(ExchangeCotacaoRequest request) {
        if (request == null || request.valorBRL() == null || request.valorBRL().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O campo valorBRL deve ser maior que zero.");
        }
        return request.valorBRL();
    }

    private record BookLevel(BigDecimal preco, BigDecimal quantidade) {
    }

    private record CompraResult(BigDecimal precoMedio, BigDecimal quantidadeUsdt) {
    }
}
