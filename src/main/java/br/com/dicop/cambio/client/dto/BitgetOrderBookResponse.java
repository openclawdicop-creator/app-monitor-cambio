package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitgetOrderBookResponse {
    public String code;
    public String msg;
    public Long requestTime;
    public BitgetOrderBookData data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BitgetOrderBookData {
        public List<List<String>> asks;
        public List<List<String>> bids;
        public String ts;
    }
}
