package eu.h2020.symbiote.administration.model.Baas.Federation;

import java.util.List;

public class AddFederationBaasResponse {
    private String voting_id;
    private List<String> voter_ids;

    public AddFederationBaasResponse(String voting_id, List<String> voter_ids) {
        this.voting_id = voting_id;
        this.voter_ids = voter_ids;
    }

    public AddFederationBaasResponse() {
    }

    public String getVoting_id() {
        return voting_id;
    }

    public void setVoting_id(String voting_id) {
        this.voting_id = voting_id;
    }

    public List<String> getVoter_ids() {
        return voter_ids;
    }

    public void setVoter_ids(List<String> voter_ids) {
        this.voter_ids = voter_ids;
    }
}
