package eu.h2020.symbiote.administration.model.FederationVoteRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.CoreUser;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class FederationVoteRequest implements Comparable {
    @NotNull
    private final String federationId;
    @NotNull
//    private final CoreUser requestingUser;
    private final String requestingUser;

    @JsonIgnore
    private SmartContract smartContract;

    @NotNull
    private String votingId;

    private RequestStatus status;
    private VoteAction voteAction;
    private final Date requestDate;
    private Date handledDate;

    private String username; //this holds the username of the user, the action refers to

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFederationId() {
        return federationId;
    }


    public String getRequestingUser() {
        return requestingUser;
    }

    public String getVotingId() {
        return votingId;
    }

    public void setVotingId(String votingId) {
        this.votingId = votingId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public VoteAction getVoteAction() {
        return voteAction;
    }

    public void setVoteAction(VoteAction voteAction) {
        this.voteAction = voteAction;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public Date getHandledDate() {
        return handledDate;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setHandledDate(Date handledDate) {
        this.handledDate = handledDate;
    }

    @PersistenceConstructor
    @JsonCreator
    public FederationVoteRequest(@JsonProperty("federationId") String federationId,
                                 @JsonProperty("requestingUser") String requestingUser,
                                 @JsonProperty("votingId") String votingId,
                                 @JsonProperty("status") RequestStatus status,
                                 @JsonProperty("voteAction") VoteAction voteAction,
                                 @JsonProperty("updatedRules") SmartContract smartContract,
                                 @JsonProperty("requestDate") Date requestDate,
                                 @JsonProperty("handledDate") Date handledDate,
                                 @JsonProperty("username") String username) {
        this.federationId = federationId;
        this.votingId = votingId;
        this.status = status;
        this.voteAction = voteAction;
        this.requestDate = requestDate;
        this.handledDate = handledDate;
        this.requestingUser = requestingUser;
        this.smartContract = smartContract;
        this.username = username;
    }

    public FederationVoteRequest(String federationId, String requestingUser, VoteAction voteAction) {
        this(federationId, requestingUser, "", RequestStatus.PENDING, voteAction, new SmartContract(), new Date(), null, "");
    }

    public FederationVoteRequest(String federationId, String requestingUser, SmartContract smartContract, VoteAction voteAction) {
        this(federationId, requestingUser, "", RequestStatus.PENDING, voteAction, smartContract, new Date(), null, "");
    }

    public FederationVoteRequest(String federationId, String requestingUser,VoteAction voteAction, String username) {
        this(federationId, requestingUser, "", RequestStatus.PENDING, voteAction, new SmartContract(), new Date(), null, username);
    }

    @Override
    public String toString() {
        return "FederationJoinRequest{" +
                "federationId='" + federationId + '\'' +
                "requestingUser='" + requestingUser + '\'' +
                "RequestStatus='" + status.toString() + '\'' +
                "requestDate='" + requestDate + '\'' +
                "handledDate='" + handledDate + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        FederationVoteRequest voteRequest = (FederationVoteRequest) o;
        if (getHandledDate() == null && voteRequest.getHandledDate() == null)
            return 0;
        if (getHandledDate() == null)
            return -1;
        if (voteRequest.getHandledDate() == null)
            return 1;
        return getHandledDate().compareTo(voteRequest.getHandledDate());
    }
}
