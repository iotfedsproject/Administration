package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quality {

    @JsonProperty("MinValueFed")
    private Integer minValueFed;

    @Override
    public String toString() {
        return "Quality{" +
                "minValueFed=" + minValueFed +
                '}';
    }
}
