package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmartOrderBookResponse {

    @JsonProperty("data")
    public Data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("asks")
        public List<List<String>> asks;

        @JsonProperty("bids")
        public List<List<String>> bids;
    }
}
