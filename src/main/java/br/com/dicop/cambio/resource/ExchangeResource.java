package br.com.dicop.cambio.resource;

import br.com.dicop.cambio.dto.ExchangeCotacaoRequest;
import br.com.dicop.cambio.dto.ExchangeCotacaoResponse;
import br.com.dicop.cambio.service.ExchangeCotacaoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/exchange")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExchangeResource {

    @Inject
    ExchangeCotacaoService exchangeCotacaoService;

    @POST
    @Path("/cotacao")
    public List<ExchangeCotacaoResponse> buscarCotacoes(ExchangeCotacaoRequest request) {
        return exchangeCotacaoService.buscarCotacoes(request);
    }
}
