package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OkxOrderBookResponse {
    public String code;
    public String msg;
    public List<OkxOrderBookData> data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OkxOrderBookData {
        public List<List<String>> asks;
        public List<List<String>> bids;
    }
}
