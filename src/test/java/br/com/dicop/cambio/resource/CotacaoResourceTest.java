package br.com.dicop.cambio.resource;

import br.com.dicop.cambio.client.AwesomeApiClient;
import br.com.dicop.cambio.client.dto.AwesomeLatestRateResponse;
import br.com.dicop.cambio.client.dto.AwesomeRateItem;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class CotacaoResourceTest {

    @InjectMock
    @RestClient
    AwesomeApiClient awesomeApiClient;

    @Test
    void deveResponderEndpointAtual() {
        AwesomeLatestRateResponse response = new AwesomeLatestRateResponse();
        AwesomeRateItem item = new AwesomeRateItem();
        item.bid = "5.10";
        item.ask = "5.12";
        item.high = "5.15";
        item.low = "5.05";
        item.pctChange = "0.10";
        item.timestamp = "1712275200";
        response.addRate("USDBRL", item);

        doReturn(response).when(awesomeApiClient).getLatest("USD-BRL");

        given()
                .queryParam("moeda", "USD")
                .when()
                .get("/api/cotacao/atual")
                .then()
                .statusCode(200);
    }
}
