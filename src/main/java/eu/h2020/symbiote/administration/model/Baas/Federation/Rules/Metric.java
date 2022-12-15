package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.enums.UnderperformanceAction;

public class Metric {

    @JsonProperty("QosPercentage")
    private Integer qoSPercentage;

    @JsonProperty("ReputationPercentage")
    private Integer reputationPercentage;

    @JsonProperty("Quality")
    private Quality quality;

    @JsonProperty("Underperformance")
    private UnderperformanceAction underperformanceAction;

    public Integer getQoSPercentage() {
        return qoSPercentage;
    }

    public void setQoSPercentage(Integer qoSPercentage) {
        this.qoSPercentage = qoSPercentage;
    }

    public Integer getReputationPercentage() {
        return reputationPercentage;
    }

    public void setReputationPercentage(Integer reputationPercentage) {
        this.reputationPercentage = reputationPercentage;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public UnderperformanceAction getUnderperformanceAction() {
        return underperformanceAction;
    }

    public void setUnderperformanceAction(UnderperformanceAction underperformanceAction) {
        this.underperformanceAction = underperformanceAction;
    }
}
