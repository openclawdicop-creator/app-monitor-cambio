package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.KuCoinOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "kucoin-exchange")
@Path("/api/v1/market/orderbook")
public interface KuCoinExchangeClient {

    @GET
    @Path("/level2_20")
    KuCoinOrderBookResponse getOrderBook(@QueryParam("symbol") String symbol);
}
