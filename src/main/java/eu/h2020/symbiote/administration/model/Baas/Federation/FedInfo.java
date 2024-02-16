package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import org.springframework.data.annotation.Id;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FedInfo {
    @Id
    @JsonProperty("ID")
    private String id;

    @JsonProperty("balance")
    private Float balance;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("members_ids")
    private List<String> memberIds;

    @JsonProperty("related_applications")
    private List<String> relatedApplications;

    @JsonProperty("inf_model")
    private String informationModel;

    @JsonProperty("reputation")
    private Float reputation;

    @JsonProperty("rules")
    private SmartContract smartContract;

    public FedInfo(String id, List<String> memberIds, List<String> relatedApplications, SmartContract smartContract, String creatorId, String informationModel, Float reputation, Float balance) {
        this.id = id;
        this.memberIds = memberIds;
        this.relatedApplications = relatedApplications;
        this.smartContract = smartContract;
        this.creatorId = creatorId;
        this.informationModel = informationModel;
        this.reputation = reputation;
        this.balance = balance;
    }

    public FedInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getRelatedApplications() {
        return relatedApplications;
    }

    public void setRelatedApplications(List<String> relatedApplications) {
        this.relatedApplications = relatedApplications;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getInformationModel() {
        return informationModel;
    }

    public void setInformationModel(String informationModel) {
        this.informationModel = informationModel;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public Float getReputation() {
        return reputation;
    }

    public void setReputation(Float reputation) {
        this.reputation = reputation;
    }
}
