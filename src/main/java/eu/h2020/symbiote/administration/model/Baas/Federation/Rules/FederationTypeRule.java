package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FederationTypeRule {

    @JsonProperty("Type")
    private FederationType federationType;
    @JsonProperty("DataAvailability")
    private DataAvailability dataAvailability;
    @JsonProperty("ServiceType")
    private String serviceType;
    @JsonProperty("SupportedOntologies")
    private String supportedOntologies;

    public FederationType getFederationType() {
        return federationType;
    }

    public void setFederationType(FederationType federationType) {
        this.federationType = federationType;
    }

    public DataAvailability getDataAvailability() {
        return dataAvailability;
    }

    public void setDataAvailability(DataAvailability dataAvailability) {
        this.dataAvailability = dataAvailability;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getSupportedOntologies() {
        return supportedOntologies;
    }

    public void setSupportedOntologies(String supportedOntologies) {
        this.supportedOntologies = supportedOntologies;
    }

    public enum FederationType {
        Providers,
        Mixed
    }
    public enum DataAvailability{
        Closed,
        Hybrid
    }

    @Override
    public String toString() {
        return "FederationTypeRule{" +
                "federationType=" + federationType +
                ", dataAvailability=" + dataAvailability +
                ", serviceType='" + serviceType + '\'' +
                ", supportedOntologies='" + supportedOntologies + '\'' +
                '}';
    }
}
