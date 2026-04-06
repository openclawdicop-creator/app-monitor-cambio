package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.BybitOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bybit-exchange")
public interface BybitExchangeClient {

    @GET
    @Path("/v5/market/orderbook")
    BybitOrderBookResponse getOrderBook(@QueryParam("category") String category, @QueryParam("symbol") String symbol, @QueryParam("limit") int limit);
}
