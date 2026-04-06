package br.com.dicop.cambio.resource;

import br.com.dicop.cambio.client.BinanceExchangeClient;
import br.com.dicop.cambio.client.BitgetExchangeClient;
import br.com.dicop.cambio.client.KuCoinExchangeClient;
import br.com.dicop.cambio.client.dto.BinanceOrderBookResponse;
import br.com.dicop.cambio.client.dto.BitgetOrderBookResponse;
import br.com.dicop.cambio.client.dto.KuCoinOrderBookResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class ExchangeResourceTest {

    @InjectMock
    @RestClient
    BinanceExchangeClient binanceExchangeClient;

    @InjectMock
    @RestClient
    BitgetExchangeClient bitgetExchangeClient;

    @InjectMock
    @RestClient
    KuCoinExchangeClient kuCoinExchangeClient;

    @Test
    void deveRetornarCotacoesOrdenadasPeloMelhorPrecoDeCompra() {
        doReturn(criarBinanceResponse()).when(binanceExchangeClient).getOrderBook("USDTBRL", 5);
        doReturn(criarBitgetResponse()).when(bitgetExchangeClient).getOrderBook("USDTBRL", "5");
        doReturn(criarKucoinResponse()).when(kuCoinExchangeClient).getOrderBook("USDT-BRL");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "valorBRL": 1000
                        }
                        """)
                .when()
                .post("/api/exchange/cotacao")
                .then()
                .statusCode(200);
    }

    private BinanceOrderBookResponse criarBinanceResponse() {
        BinanceOrderBookResponse response = new BinanceOrderBookResponse();
        response.asks = List.of(
                List.of("5.30", "500"),
                List.of("5.31", "500"),
                List.of("5.32", "500"),
                List.of("5.33", "500"),
                List.of("5.34", "500")
        );
        response.bids = List.of(
                List.of("5.29", "500"),
                List.of("5.28", "500"),
                List.of("5.27", "500"),
                List.of("5.26", "500"),
                List.of("5.25", "500")
        );
        return response;
    }

    private BitgetOrderBookResponse criarBitgetResponse() {
        BitgetOrderBookResponse response = new BitgetOrderBookResponse();
        response.data = new BitgetOrderBookResponse.BitgetOrderBookData();
        response.data.asks = List.of(
                List.of("5.20", "500"),
                List.of("5.21", "500"),
                List.of("5.22", "500"),
                List.of("5.23", "500"),
                List.of("5.24", "500")
        );
        response.data.bids = List.of(
                List.of("5.19", "500"),
                List.of("5.18", "500"),
                List.of("5.17", "500"),
                List.of("5.16", "500"),
                List.of("5.15", "500")
        );
        return response;
    }

    private KuCoinOrderBookResponse criarKucoinResponse() {
        KuCoinOrderBookResponse response = new KuCoinOrderBookResponse();
        response.data = new KuCoinOrderBookResponse.KuCoinOrderBookData();
        response.data.asks = List.of(
                List.of("5.25", "500"),
                List.of("5.26", "500"),
                List.of("5.27", "500"),
                List.of("5.28", "500"),
                List.of("5.29", "500")
        );
        response.data.bids = List.of(
                List.of("5.24", "500"),
                List.of("5.23", "500"),
                List.of("5.22", "500"),
                List.of("5.21", "500"),
                List.of("5.20", "500")
        );
        return response;
    }
}
