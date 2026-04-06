package br.com.dicop.cambio.client;

import br.com.dicop.cambio.client.dto.AwesomeHistoricalRateResponse;
import br.com.dicop.cambio.client.dto.AwesomeLatestRateResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "awesome-api")
@Path("/json")
public interface AwesomeApiClient {

    @GET
    @Path("/last/{pair}")
    AwesomeLatestRateResponse getLatest(@PathParam("pair") String pair);

    @GET
    @Path("/daily/{pair}/{days}")
    AwesomeHistoricalRateResponse[] getHistory(@PathParam("pair") String pair, @PathParam("days") int days);
}
