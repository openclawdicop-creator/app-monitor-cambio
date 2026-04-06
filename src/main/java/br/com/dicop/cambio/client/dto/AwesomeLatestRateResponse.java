package br.com.dicop.cambio.client.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AwesomeLatestRateResponse {

    private final Map<String, AwesomeRateItem> rates = new LinkedHashMap<>();

    @JsonAnySetter
    public void addRate(String key, AwesomeRateItem value) {
        rates.put(key, value);
    }

    public Map<String, AwesomeRateItem> getRates() {
        return rates;
    }
}
