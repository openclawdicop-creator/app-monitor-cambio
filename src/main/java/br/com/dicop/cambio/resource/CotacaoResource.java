package br.com.dicop.cambio.resource;

import br.com.dicop.cambio.dto.CalculoCotacaoRequest;
import br.com.dicop.cambio.dto.CalculoCotacaoResponse;
import br.com.dicop.cambio.dto.CotacaoAtualResponse;
import br.com.dicop.cambio.dto.CotacaoHistoricoResponse;
import br.com.dicop.cambio.service.CambioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;

@Path("/api/cotacao")
@Produces(MediaType.APPLICATION_JSON)
public class CotacaoResource {

    @Inject
    CambioService cambioService;

    @GET
    @Path("/atual")
    public CotacaoAtualResponse buscarAtual(@QueryParam("moeda") String moeda) {
        return cambioService.buscarCotacaoAtual(moeda);
    }

    @GET
    @Path("/historico/30dias")
    public CotacaoHistoricoResponse buscarHistorico(@QueryParam("moeda") String moeda) {
        return cambioService.buscarHistorico(moeda);
    }

    @POST
    @Path("/calcular")
    public CalculoCotacaoResponse calcular(CalculoCotacaoRequest request) {
        return cambioService.calcularConversao(
                request.moeda(),
                request.valor(),
                request.moedaOrigem(),
                request.moedaDestino()
        );
    }
}
