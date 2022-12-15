package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationInvitation;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.model.mim.QoSConstraint;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FederationWithOrganization extends Federation {
    private List<String> organizationMembers;
    private SmartContract smartContract;
    private Map<String, FederationInvitation> openInvitations;

    public FederationWithOrganization(@JsonProperty("id") String id,
                                      @JsonProperty("lastModified") Date lastModified,
                                      @JsonProperty("name") String name,
                                      @JsonProperty("public") Boolean isPublic,
                                      @JsonProperty("informationModel") InformationModel informationModel,
                                      @JsonProperty("slaConstraints") List<QoSConstraint> slaConstraints,
                                      @JsonProperty("smartContract") SmartContract smartContract,
                                      @JsonProperty("members") List<String> members,
                                      @JsonProperty("openInvitations") Map<String, FederationInvitation> openInvitations
    ) {


        setId(id);
        setName(name);
        setLastModified(lastModified);
        setPublic(isPublic);
        setInformationModel(informationModel);
        setSlaConstraints(slaConstraints);
        this.smartContract = smartContract;
        this.organizationMembers = members;
        this.openInvitations = openInvitations != null ? openInvitations : new HashMap<>();
    }

    public List<String> getOrganizationMembers() {
        return organizationMembers;
    }

    public void setOrganizationMembers(List<String> organizationMembers) {
        this.organizationMembers = organizationMembers;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }
}
