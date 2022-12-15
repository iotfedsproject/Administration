package eu.h2020.symbiote.administration.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VoteAction {
    ADD_MEMBER("Add Member"),
    REMOVE_MEMBER("Remove Member"),
    DELETE_FEDERATION("Delete Federation"),
    UPDATE_FEDERATION_RULES("Update Federation Rules");

    private final String voteAction;

    VoteAction(String voteAction) {
        this.voteAction = voteAction;
    }

    public String getVoteAction() {
        return voteAction;
    }

    @Override
    @JsonValue
    public String toString() {
        switch (this) {
            case ADD_MEMBER:
                return "Add Member";
            case REMOVE_MEMBER:
                return "Remove Member";
            case DELETE_FEDERATION:
                return "Delete Federation";
            case UPDATE_FEDERATION_RULES:
                return "Update Federation Rules";
            default:
                throw new IllegalArgumentException();
            }
        }
    }