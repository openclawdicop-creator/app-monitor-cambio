package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.BitgetOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bitget-exchange")
@Path("/api/v2/spot/market")
public interface BitgetExchangeClient {

    @GET
    @Path("/orderbook")
    BitgetOrderBookResponse getOrderBook(
            @QueryParam("symbol") String symbol,
            @QueryParam("limit") String limit
    );
}
