package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FederationGovernance {

    @JsonProperty("BoardGov")
    private List<String> boardOfGovernors;

    @JsonProperty("Proposals")
    private List<String> proposals;

    @JsonProperty("VoteRules")
    private VoteRule voteRule;

    public FederationGovernance(List<String> boardOfGovernors, List<String> proposals, VoteRule voteRule) {
        this.boardOfGovernors = boardOfGovernors;
        this.proposals = proposals;
        this.voteRule = voteRule;
    }

    public FederationGovernance() {
    }

    @Override
    public String toString() {
        return "FederationGovernance{" +
                "boardOfGovernors=" + boardOfGovernors +
                ", proposals=" + proposals +
                ", voteRule=" + voteRule +
                '}';
    }
}
