package eu.h2020.symbiote.administration.model;

import eu.h2020.symbiote.model.mim.QoSConstraint;

import java.util.Date;
import java.util.List;

public class UpdateRules extends VoteRequest {
    private List<QoSConstraint> slaConstraints;

    public UpdateRules(String federationId, String invitedUserId, List<QoSConstraint> slaConstraints) {
        super(federationId, invitedUserId);
        this.slaConstraints = slaConstraints;
    }

//    public UpdateRules(String federationId, String invitedUserId, List<QoSConstraint> slaConstraints) {
//        super(federationId, invitedUserId);
//        this.slaConstraints = slaConstraints;
//    }

    public List<QoSConstraint> getSlaConstraints() {
        return slaConstraints;
    }

    public void setSlaConstraints(List<QoSConstraint> slaConstraints) {
        this.slaConstraints = slaConstraints;
    }
}
