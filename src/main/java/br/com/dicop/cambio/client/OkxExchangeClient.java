package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.OkxOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "okx-exchange")
public interface OkxExchangeClient {

    @GET
    @Path("/api/v5/market/books")
    OkxOrderBookResponse getOrderBook(@QueryParam("instId") String instId, @QueryParam("sz") int sz);
}
