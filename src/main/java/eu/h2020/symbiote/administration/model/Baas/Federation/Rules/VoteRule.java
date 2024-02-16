package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoteRule {
    @JsonProperty("Tokens")
    private Integer tokens;
    @JsonProperty("Type")
    private VoteType type;

    public VoteRule(int tokens, VoteType type) {
        this.tokens = tokens;
        this.type = type;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public VoteType getType() {
        return type;
    }

    public void setType(VoteType type) {
        this.type = type;
    }

    public VoteRule() {
    }

    @Override
    public String toString() {
        return "VoteRule{" +
                "tokens=" + tokens +
                ", type=" + type +
                '}';
    }
}
