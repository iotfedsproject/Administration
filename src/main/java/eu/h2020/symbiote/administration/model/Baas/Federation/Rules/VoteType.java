package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoteType {
    @JsonProperty("ApprovalPercentage")
    private Integer approvalPercentage;
    @JsonProperty("Base")
    private Base voteTypeBase;

    public VoteType(int approvalPercentage, Base voteTypeBase) {
        this.approvalPercentage = approvalPercentage;
        this.voteTypeBase = voteTypeBase;
    }

    public int getApprovalPercentage() {
        return approvalPercentage;
    }

    public void setApprovalPercentage(int approvalPercentage) {
        this.approvalPercentage = approvalPercentage;
    }

    public Base getVoteTypeBase() {
        return voteTypeBase;
    }

    public void setVoteTypeBase(Base voteTypeBase) {
        this.voteTypeBase = voteTypeBase;
    }

    public enum Base{
        Board,
        Voters
    }

    public VoteType() {
    }

    @Override
    public String toString() {
        return "VoteType{" +
                "approvalPercentage=" + approvalPercentage +
                ", voteTypeBase=" + voteTypeBase +
                '}';
    }
}
