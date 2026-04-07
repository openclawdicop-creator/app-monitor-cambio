package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.BitmartOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bitmart-exchange")
public interface BitmartExchangeClient {

    @GET
    @Path("/contract/public/depth")
    BitmartOrderBookResponse getOrderBook(@QueryParam("symbol") String symbol);
}
