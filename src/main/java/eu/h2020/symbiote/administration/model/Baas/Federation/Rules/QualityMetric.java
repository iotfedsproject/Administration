package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.enums.UnderperformanceAction;

public class QualityMetric {

    @JsonProperty("QoEWeights")
    public QoEWeights qoEWeights;

    @JsonProperty("QoSPercentage")
    public Integer qoSPercentage;

    @JsonProperty("QoSWeights")
    public QoSWeights qoSWeights;

    @JsonProperty("Quality")
    public Quality quality;

    @JsonProperty("ReputationPercentage")
    public Integer reputationPercentage;

    @JsonProperty("UnderPerformance")
    public UnderperformanceAction underperformanceAction;

    public Integer getQoSPercentage() {
        return qoSPercentage;
    }

    public void setQoSPercentage(Integer qoSPercentage) {
        this.qoSPercentage = qoSPercentage;
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

    public Integer getReputationPercentage() {
        return reputationPercentage;
    }

    public void setReputationPercentage(Integer reputationPercentage) {
        this.reputationPercentage = reputationPercentage;
    }

    public QualityMetric() {
    }

    public QoEWeights getQoEWeights() {
        return qoEWeights;
    }

    public void setQoEWeights(QoEWeights qoEWeights) {
        this.qoEWeights = qoEWeights;
    }

    public QoSWeights getQoSWeights() {
        return qoSWeights;
    }

    public void setQoSWeights(QoSWeights qoSWeights) {
        this.qoSWeights = qoSWeights;
    }

    public QualityMetric(UnderperformanceAction underperformanceAction, Integer reputationPercentage) {
        this.underperformanceAction = underperformanceAction;
        this.reputationPercentage = reputationPercentage;
    }

    @Override
    public String toString() {
        return "QualityMetric{" +
                "underperformanceAction=" + underperformanceAction +
                ", reputationPercentage=" + reputationPercentage +
                ", qoSPercentage=" + qoSPercentage +
                ", quality=" + quality +
                '}';
    }
}
