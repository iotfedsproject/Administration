package eu.h2020.symbiote.administration.model.Baas.Federation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationInvitation;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.model.mim.QoSConstraint;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FederationWithSmartContract extends Federation {


    private SmartContract smartContract;

    public FederationWithSmartContract(@JsonProperty("id") String id,
                                       @JsonProperty("lastModified") Date lastModified,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("public") Boolean isPublic,
                                       @JsonProperty("informationModel") InformationModel informationModel,
                                       @JsonProperty("slaConstraints") List<QoSConstraint> slaConstraints,
                                       @JsonProperty("smartContract") SmartContract smartContract,
                                       @JsonProperty("members") List<FederationMember> members
    ) {


        setId(id);
        setName(name);
        setLastModified(lastModified);
        setPublic(isPublic);
        setInformationModel(informationModel);
        setSlaConstraints(slaConstraints);
        setMembers(members);
        this.smartContract = smartContract;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }
}
