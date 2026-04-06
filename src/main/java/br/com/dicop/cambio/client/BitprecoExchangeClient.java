package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.BitprecoOrderBookResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bitpreco-exchange")
public interface BitprecoExchangeClient {

    @GET
    @Path("/usdt-brl/orderbook")
    BitprecoOrderBookResponse getOrderBook();
}
