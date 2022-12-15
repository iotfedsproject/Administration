package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import org.springframework.data.annotation.Id;

import java.util.List;

public class FedInfo {
    @Id
    @JsonProperty("id")
    private String id;
    @JsonProperty("member_ids")
    private List<String> memberIds;
    @JsonProperty("related_applications")
    private List<String> relatedApplications;

    @JsonProperty("information_model")
    private String informationModel;

    @JsonProperty("rules")
    private SmartContract smartContract;

    public FedInfo(String id, List<String> memberIds, List<String> relatedApplications, SmartContract smartContract) {
        this.id = id;
        this.memberIds = memberIds;
        this.relatedApplications = relatedApplications;
        this.smartContract = smartContract;
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
}
