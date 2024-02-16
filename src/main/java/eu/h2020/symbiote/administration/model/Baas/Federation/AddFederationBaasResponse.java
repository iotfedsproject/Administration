package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddFederationBaasResponse {

    @JsonProperty("ID")
    private String votingId;

    @JsonProperty("votes")
    private Map<String,String> votersStatus;

    public AddFederationBaasResponse(String votingId, Map<String,String> votersStatus) {
        this.votingId = votingId;
        this.votersStatus = votersStatus;
    }

    public AddFederationBaasResponse() {
    }

    public String getVotingId() {
        return votingId;
    }

    public void setVotingId(String votingId) {
        this.votingId = votingId;
    }

    public Map<String, String> getVotersStatus() {
        return votersStatus;
    }

    public void setVotersStatus(Map<String, String> votersStatus) {
        this.votersStatus = votersStatus;
    }
}
