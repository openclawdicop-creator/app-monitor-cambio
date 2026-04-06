package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitOrderBookResponse {
    public int retCode;
    public String retMsg;
    public BybitResult result;
    public long time;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BybitResult {
        public List<List<String>> b;  // bids
        public List<List<String>> a;  // asks
    }
}
