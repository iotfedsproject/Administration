package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Currency;

public class FederationMarketplaceMetric {

    @JsonProperty("ChargePolicy")
    private ChargePolicy chargePolicy;
    @JsonProperty("FedMarketCharge")
    private Integer fedMarketCharge;
    @JsonProperty("GlobalMarketCharge")
    private Integer globalMarketCharge;
    @JsonProperty("FedProduct")
    private FederationProduct federationProduct;
    @JsonProperty("ProfitPolicy")
    private ProfitPolicy profitPolicy;
    @JsonProperty("Coin")
    private Currency currency;

    public FederationMarketplaceMetric(ChargePolicy chargePolicy,Integer fedMarketCharge ,Integer globalMarketCharge , FederationProduct federationProduct, ProfitPolicy profitPolicy, Currency currency) {
        this.chargePolicy = chargePolicy;
        this.fedMarketCharge = fedMarketCharge;
        this.globalMarketCharge = globalMarketCharge;
        this.federationProduct = federationProduct;
        this.profitPolicy = profitPolicy;
        this.currency = currency;
    }

    public ChargePolicy getChargePolicy() {
        return chargePolicy;
    }

    public void setChargePolicy(ChargePolicy chargePolicy) {
        this.chargePolicy = chargePolicy;
    }

    public FederationProduct getFederationProduct() {
        return federationProduct;
    }

    public void setFederationProduct(FederationProduct federationProduct) {
        this.federationProduct = federationProduct;
    }

    public ProfitPolicy getProfitPolicy() {
        return profitPolicy;
    }

    public void setProfitPolicy(ProfitPolicy profitPolicy) {
        this.profitPolicy = profitPolicy;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Integer getFedMarketCharge() {
        return fedMarketCharge;
    }

    public void setFedMarketCharge(Integer fedMarketCharge) {
        this.fedMarketCharge = fedMarketCharge;
    }

    public Integer getGlobalMarketCharge() {
        return globalMarketCharge;
    }

    public void setGlobalMarketCharge(Integer globalMarketCharge) {
        this.globalMarketCharge = globalMarketCharge;
    }

    public enum ChargePolicy {
        Free, PerProduct, PerUsage
    }

    public enum FederationProduct {
        Packaging, Matching, Both
    }

    public enum ProfitPolicy {
        PerSource, Contribution, Both
    }

    public enum Currency {
        IoTFeds, Euro, Both, FedCoin
    }

    public FederationMarketplaceMetric() {
    }

    @Override
    public String toString() {
        return "FederationMarketplaceMetric{" +
                "chargePolicy=" + chargePolicy +
                ", fedMarketCharge=" + fedMarketCharge +
                ", globalMarketCharge=" + globalMarketCharge +
                ", federationProduct=" + federationProduct +
                ", profitPolicy=" + profitPolicy +
                ", currency=" + currency +
                '}';
    }
}
