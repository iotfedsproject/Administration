package eu.h2020.symbiote.administration.converters;

import eu.h2020.symbiote.administration.model.Baas.Federation.FedInfo;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithOrganization;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.RegisterFedToBc;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationWithInvitations;
import eu.h2020.symbiote.administration.repository.FederationRepository;
import eu.h2020.symbiote.model.mim.Federation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.h2020.symbiote.administration.converters.SmartContractToConstraintsConverter.convertSmartContractToConstrains;

@Component
public class BaasFederationToSymbioteFederation {

    @Autowired
    private FederationRepository federationRepository;

    public List<FederationWithOrganization> baasFederationToSymbioteFederation (List<FedInfo> baasFederationList){
        List<FederationWithOrganization> federationWithOrganizationList = new ArrayList<>();

        List<FederationWithInvitations> allFederations = federationRepository.findAll();

        Map<String, FederationWithInvitations> federationsById = allFederations.stream()
                .collect(Collectors.toMap(FederationWithInvitations::getId, fed-> fed));

        for (FedInfo baasFederation: baasFederationList){

            FederationWithInvitations localFed = federationsById.get(baasFederation.getId());

            federationWithOrganizationList.add( new FederationWithOrganization(
                    baasFederation.getId(),
                    localFed.getLastModified(),
                    localFed.getName(),
                    localFed.isPublic(),
                    localFed.getInformationModel(),
                    convertSmartContractToConstrains(localFed.getSmartContract()),
                    localFed.getSmartContract(),
                    baasFederation.getMemberIds(),
                    localFed.getOpenInvitations()
                    )
            );
        }

        return federationWithOrganizationList;
    }
}
