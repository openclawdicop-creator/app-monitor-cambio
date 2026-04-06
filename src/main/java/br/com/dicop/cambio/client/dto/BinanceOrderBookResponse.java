package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceOrderBookResponse {
    public Long lastUpdateId;
    public List<List<String>> bids;
    public List<List<String>> asks;
}
