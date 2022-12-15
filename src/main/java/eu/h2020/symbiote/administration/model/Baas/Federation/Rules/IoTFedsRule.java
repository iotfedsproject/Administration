package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IoTFedsRule {
    @JsonProperty("FedTypeRules")
    private FederationTypeRule federationTypeRule;
    @JsonProperty("FedGov")
    private FederationGovernance federationGovernance;
    @JsonProperty("QualityAssurance")
    private QualityMetric qualityMetric;
    @JsonProperty("FedMarketplace")
    private FederationMarketplaceMetric federationMarketplaceMetric;

    public IoTFedsRule(FederationTypeRule federationTypeRule, FederationGovernance federationGovernance, QualityMetric qualityMetric, FederationMarketplaceMetric federationMarketplaceMetric) {
        this.federationTypeRule = federationTypeRule;
        this.federationGovernance = federationGovernance;
        this.qualityMetric = qualityMetric;
        this.federationMarketplaceMetric = federationMarketplaceMetric;
    }

    public IoTFedsRule() {
    }

    public FederationTypeRule getFederationTypeRule() {
        return federationTypeRule;
    }

    public void setFederationTypeRule(FederationTypeRule federationTypeRule) {
        this.federationTypeRule = federationTypeRule;
    }

    public FederationGovernance getFederationGovernance() {
        return federationGovernance;
    }

    public void setFederationGovernance(FederationGovernance federationGovernance) {
        this.federationGovernance = federationGovernance;
    }

    public QualityMetric getQualityMetric() {
        return qualityMetric;
    }

    public void setQualityMetric(QualityMetric qualityMetric) {
        this.qualityMetric = qualityMetric;
    }

    public FederationMarketplaceMetric getFederationMarketplaceMetric() {
        return federationMarketplaceMetric;
    }

    public void setFederationMarketplaceMetric(FederationMarketplaceMetric federationMarketplaceMetric) {
        this.federationMarketplaceMetric = federationMarketplaceMetric;
    }
}
