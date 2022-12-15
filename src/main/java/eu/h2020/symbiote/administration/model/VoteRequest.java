package eu.h2020.symbiote.administration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class VoteRequest {

    @NotNull
    private final String federationId;

    private final String userId;

    public String getFederationId() {
        return federationId;
    }

    public String getUserId() { return userId; }


    @PersistenceConstructor
    @JsonCreator
    public VoteRequest(@JsonProperty("federationId") String federationId,
                       @JsonProperty("userId") String userId) {
        this.federationId = federationId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "FederationJoinRequest{" +
                "federationId='" + federationId + '\'' +
                ", userId=" + userId +
                '}';
    }
}
