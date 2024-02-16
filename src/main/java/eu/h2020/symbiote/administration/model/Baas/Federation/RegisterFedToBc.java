package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;

import java.util.List;

public class RegisterFedToBc {

    @JsonProperty("fed_id")
    private String fed_id;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("related_applications")
    private List<String> relatedApplications;

    @JsonProperty("inf_model")
    private String informationModel;

    @JsonProperty("role")
    private final String role = "federation";

    @JsonProperty("mail")
    private String mail;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("rules")
    private SmartContract smartContract;

    public String getFed_id() {
        return fed_id;
    }

    public void setFed_id(String fed_id) {
        this.fed_id = fed_id;
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
        return informationModel.toString();
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

    public String getRole() {
        return role;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public RegisterFedToBc(String fed_id, String creatorId, List<String> relatedApplications, String informationModel, SmartContract smartContract, String organization, String email) {
        this.fed_id = fed_id;
        this.creatorId = creatorId;
        this.relatedApplications = relatedApplications;
        this.smartContract = smartContract;
        this.informationModel = informationModel;
        this.organization = organization;
        this.mail = email;
    }
}
