package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KuCoinOrderBookResponse {
    public String code;
    public KuCoinOrderBookData data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KuCoinOrderBookData {
        public Long time;
        public String sequence;
        public List<List<String>> bids;
        public List<List<String>> asks;
    }
}
