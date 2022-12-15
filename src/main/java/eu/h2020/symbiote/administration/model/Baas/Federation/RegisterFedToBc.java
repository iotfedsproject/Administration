package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;

import java.util.List;

public class RegisterFedToBc {

    @JsonProperty("id")
    private String id;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("related_applications")
    private List<String> relatedApplications;

    @JsonProperty("information_model")
    private String informationModel;

    @JsonProperty("rules")
    private SmartContract smartContract;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public List<String> getRelatedApplications() {
        return relatedApplications;
    }

    public void setRelatedApplications(List<String> relatedApplications) {
        this.relatedApplications = relatedApplications;
    }

    public String getInformationModel() {
        return informationModel;
    }

    public void setInformationModel(String informationModel) {
        this.informationModel = informationModel;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public RegisterFedToBc(String id, String creatorId, List<String> relatedApplications, String informationModel, SmartContract smartContract) {
        this.id = id;
        this.creatorId = creatorId;
        this.relatedApplications = relatedApplications;
        this.smartContract = smartContract;
        this.informationModel = informationModel;
    }
}
