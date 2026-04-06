package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.MexcOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "mexc-exchange")
public interface MexcExchangeClient {

    @GET
    @Path("/api/v3/depth")
    MexcOrderBookResponse getOrderBook(@QueryParam("symbol") String symbol, @QueryParam("limit") int limit);
}
