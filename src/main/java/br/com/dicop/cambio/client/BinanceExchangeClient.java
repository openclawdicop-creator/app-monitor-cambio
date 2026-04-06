package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.BinanceOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "binance-exchange")
@Path("/api/v3")
public interface BinanceExchangeClient {

    @GET
    @Path("/depth")
    BinanceOrderBookResponse getOrderBook(@QueryParam("symbol") String symbol, @QueryParam("limit") int limit);
}
