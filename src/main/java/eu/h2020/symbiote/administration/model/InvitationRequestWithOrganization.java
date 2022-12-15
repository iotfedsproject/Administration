package eu.h2020.symbiote.administration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class InvitationRequestWithOrganization {

    @NotNull
    private final String federationId;

    @NotNull
    private final String organization;

    @JsonCreator
    public InvitationRequestWithOrganization(@JsonProperty("federationId") String federationId,
                             @JsonProperty("organization") String organization) {
        this.federationId = federationId;
        this.organization = organization;
    }

    public String getFederationId() { return federationId; }
    public String getOrganization() { return organization; }

    @Override
    public String toString() {
        return "InvitationRequest{" +
                "federationId='" + federationId + '\'' +
                ", organization=" + organization +
                '}';
    }
}
