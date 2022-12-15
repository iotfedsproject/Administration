package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QualityMetric {

    @JsonProperty("Metrics")
    private Metric metric;

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }
}
