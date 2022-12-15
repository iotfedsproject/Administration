package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Currency;

public class FederationMarketplaceMetric {

    @JsonProperty("ChargePolicy")
    private ChargePolicy chargePolicy;
    @JsonProperty("FedProduct")
    private FederationProduct federationProduct;
    @JsonProperty("ProfitPolicy")
    private ProfitPolicy profitPolicy;
    @JsonProperty("Coin")
    private Currency currency;

    public FederationMarketplaceMetric(ChargePolicy chargePolicy, FederationProduct federationProduct, ProfitPolicy profitPolicy, Currency currency) {
        this.chargePolicy = chargePolicy;
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
        IoTFeds, Euro, Both
    }
}
