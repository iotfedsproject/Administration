package eu.h2020.symbiote.administration.services.federation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.communication.rabbit.RabbitManager;
import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.helpers.AuthorizationServiceHelper;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithOrganization;
import eu.h2020.symbiote.administration.model.Baas.Federation.FedInfo;
import eu.h2020.symbiote.administration.model.Baas.User.BaasUser;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.repository.FederationRepository;
import eu.h2020.symbiote.administration.services.authorization.AuthorizationService;
import eu.h2020.symbiote.administration.services.baas.BaasService;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import eu.h2020.symbiote.administration.services.infomodel.InformationModelService;
import eu.h2020.symbiote.administration.services.ownedservices.CheckServiceOwnershipService;
import eu.h2020.symbiote.administration.services.ownedservices.OwnedServicesService;
import eu.h2020.symbiote.administration.services.platform.PlatformService;
import eu.h2020.symbiote.administration.services.validation.ValidationService;
import eu.h2020.symbiote.core.cci.PlatformRegistryResponse;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.security.communication.payloads.OwnedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static eu.h2020.symbiote.administration.converters.SmartContractToConstraintsConverter.convertSmartContractToConstrains;

@Service
public class FederationService {
    private static Log log = LogFactory.getLog(FederationService.class);

    private final RabbitManager rabbitManager;
    private final FederationRepository federationRepository;
    private final PlatformService platformService;
    private final OwnedServicesService ownedServicesService;
    private final CheckServiceOwnershipService checkServiceOwnershipService;
    private final InformationModelService informationModelService;
    private final ValidationService validationService;
    private final FederationNotificationService federationNotificationService;
    private final AuthorizationService authorizationService;
    private final FederationVoteRequestRepository federationVoteRequestRepository;
    private final BaasService baasService;
    private final boolean baasIntegration;
    private final FederationVoteRequestService federationVoteRequestService;

    @Qualifier("objectMapper")
    @Autowired
    ObjectMapper mapper;

    @Autowired
    public FederationService(RabbitManager rabbitManager,
                             FederationRepository federationRepository,
                             PlatformService platformService,
                             OwnedServicesService ownedServicesService,
                             CheckServiceOwnershipService checkServiceOwnershipService,
                             InformationModelService informationModelService,
                             ValidationService validationService,
                             FederationNotificationService federationNotificationService,
                             AuthorizationService authorizationService,
                             FederationVoteRequestRepository federationVoteRequestRepository,
                             BaasService baasService,
                             FederationVoteRequestService federationVoteRequestService,
                             @Value("${symbiote.baas.integration}") boolean baasIntegration) {

        Assert.notNull(rabbitManager, "RabbitManager can not be null!");
        this.rabbitManager = rabbitManager;

        Assert.notNull(federationRepository, "FederationRepository can not be null!");
        this.federationRepository = federationRepository;

        Assert.notNull(platformService, "PlatformService can not be null!");
        this.platformService = platformService;

        Assert.notNull(ownedServicesService, "OwnedServicesService can not be null!");
        this.ownedServicesService = ownedServicesService;

        Assert.notNull(checkServiceOwnershipService, "CheckServiceOwnershipService can not be null!");
        this.checkServiceOwnershipService = checkServiceOwnershipService;

        Assert.notNull(informationModelService, "InformationModelService can not be null!");
        this.informationModelService = informationModelService;

        Assert.notNull(validationService, "ValidationService can not be null!");
        this.validationService = validationService;

        Assert.notNull(federationNotificationService, "FederationNotificationService can not be null!");
        this.federationNotificationService = federationNotificationService;

        Assert.notNull(authorizationService, "AuthorizationService can not be null!");
        this.authorizationService = authorizationService;

        Assert.notNull(federationVoteRequestRepository, "FederationVoteRequestRepository can not be null!");
        this.federationVoteRequestRepository = federationVoteRequestRepository;

        Assert.notNull(federationVoteRequestService, "FederationVoteRequestService can not be null!");
        this.federationVoteRequestService = federationVoteRequestService;

        Assert.notNull(baasService, "BaasService can not be null!");
        this.baasService = baasService;

        Assert.notNull(baasIntegration, "baasIntegration can not be null!");
        this.baasIntegration = baasIntegration;
    }


    public ResponseEntity<?> listFederations() {
        // Todo: limit the results only to public federations?
//        Map<String, Federation> federationMap = federationRepository.findAll().stream()
//                .collect(Collectors.toMap(Federation::getId, federation -> federation));

        Map<String, FederationWithOrganization> federationMap = new HashMap<>();
        List<FederationWithInvitations> federationWithInvitationsList = federationRepository.findAll();
        for (FederationWithInvitations federationWithInvitations : federationWithInvitationsList) {

            ResponseEntity<String> baasResponse = baasService.getFederationInfoBaas(federationWithInvitations.getId());
            FedInfo fedInfo = new FedInfo();
            if (baasResponse.getStatusCode() == HttpStatus.OK) {
                try {
                    fedInfo = mapper.readValue(baasResponse.getBody(), FedInfo.class);
                    log.info(fedInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            FederationWithOrganization federationWithOrganization = new FederationWithOrganization(
                    federationWithInvitations.getId(),
                    federationWithInvitations.getLastModified(),
                    federationWithInvitations.getName(),
                    true,
                    federationWithInvitations.getInformationModel(),
                    federationWithInvitations.getSlaConstraints(),
                    fedInfo.getSmartContract(),
                    fedInfo.getMemberIds(),
                    federationWithInvitations.getOpenInvitations(),
                    fedInfo.getBalance(),
                    fedInfo.getReputation()
            );
            log.info("Balance: " + federationWithOrganization.getBalance());
            log.info("Reputation: " + federationWithOrganization.getReputation());
            log.info("OpenInvitations: " + federationWithOrganization.getOpenInvitations());
            federationWithOrganization.setMembers(federationWithInvitations.getMembers());

            federationMap.put(
                    federationWithInvitations.getId(),
                    federationWithOrganization
            );
        }


        return new ResponseEntity<>(federationMap, new HttpHeaders(), HttpStatus.OK);
    }

    //    TODO: not done, test it
    public ResponseEntity<?> createFederation(FederationWithOrganization federation,
                                              BindingResult bindingResult,
                                              Principal principal) throws ServiceValidationException {

        Map<String, Object> responseBody = new HashMap<>();

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        if (baasIntegration && connectionResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(connectionResponse.getBody(), new HttpHeaders(), connectionResponse.getStatusCode());
        }

        baasService.userExistsCheckThrowException(user.getValidUsername());

        if (bindingResult.hasErrors())
            return validationService.getRequestErrors(bindingResult);

        Optional<FederationWithInvitations> existingFederation = federationRepository.findById(federation.getId());
        if (existingFederation.isPresent()) {
            responseBody.put("error", "The federation with id '" + federation.getId() +
                    "' already exists!");
            return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        // Get user services
        ResponseEntity ownedPlatformsResponse = ownedServicesService.getOwnedPlatformDetails(principal);
        if (ownedPlatformsResponse.getStatusCode() != HttpStatus.OK) {
            responseBody.put("error", ownedPlatformsResponse.getBody());
            return new ResponseEntity<>(responseBody, new HttpHeaders(), ownedPlatformsResponse.getStatusCode());
        }
        Set<String> ownedPlatforms = ((Set<OwnedService>) ownedPlatformsResponse.getBody()).stream()
                .map(OwnedService::getServiceInstanceId)
                .collect(Collectors.toSet());
        Map<String, List<String>> organizationPlatforms = new HashMap<>();
        // Checking if organizations exist
        for (String member : federation.getOrganizationMembers()) {
            if (member.equals(user.getValidUsername())){
                continue;
            }
            ResponseEntity<String> registryResponse = baasService.getUserInfoBaasResponse(member);

            if (registryResponse.getStatusCode() != HttpStatus.OK) {
                responseBody.put("error", "The organization " + member + " was not found");
                return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);

            }
            BaasUser baasUser;
            try {
                baasUser = mapper.readValue(registryResponse.getBody(), BaasUser.class);
            } catch (IOException e) {
                return new ResponseEntity<>("Couldn't read user from baas", new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }
            organizationPlatforms.put(
                    baasUser.getId(),
                    new ArrayList<>(baasUser.getAssociated_platforms().keySet())
            );
        }

        // Checking if the information model exist
        if (federation.getInformationModel() != null) {
            ResponseEntity informationModelsResponse = informationModelService.getInformationModels();
            if (informationModelsResponse.getStatusCode() != HttpStatus.OK) {
                responseBody.put("error", informationModelsResponse.getBody());
                return new ResponseEntity<>(responseBody, new HttpHeaders(), informationModelsResponse.getStatusCode());
            }

            List<String> informationModels = ((List<InformationModel>) informationModelsResponse.getBody()).stream()
                    .map(InformationModel::getId).collect(Collectors.toList());

            if (!informationModels.contains(federation.getInformationModel().getId())) {
                responseBody.put("error", "The information model with id " + federation.getInformationModel().getId()
                        + " was not found");
                return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }
        }


//        // Filtering the same platforms
//        //TODO: convert this check
//        InvitationRequest invitationRequest = new InvitationRequest(
//                invitationRequestWithOrganization.getFederationId(),
//                platformFromOrganization
//        );

        // Creating the FederationWithInvitation
        HashMap<String, FederationInvitation> invitations = new HashMap<>();
        ArrayList<FederationMember> newMembers = new ArrayList<>();
        for (String platform : ownedPlatforms) {
            ResponseEntity registryResponse = platformService.getPlatformDetailsFromRegistry(platform);

            newMembers.add(new FederationMember(
                    platform,
                    ((PlatformRegistryResponse) registryResponse.getBody()).getBody().getInterworkingServices().get(0).getUrl()
            ));
        }
        for (Map.Entry<String, List<String>> set :
                organizationPlatforms.entrySet()) {
            for (String platform : set.getValue()) {
                invitations.put(platform,
                        new FederationInvitation(
                                platform,
                                FederationInvitation.InvitationStatus.PENDING,
                                new Date()));
            }

        }

        federation.setMembers(newMembers);
        FederationWithInvitations federationWithInvitations = new FederationWithInvitations(
                federation.getId(),
                new Date(),
                federation.getName(),
                federation.isPublic(),
                federation.getInformationModel(),
                convertSmartContractToConstrains(federation.getSmartContract()),
                federation.getSmartContract(),
                newMembers,
                invitations
        );

        ResponseEntity<?> baasResponse = baasService.registerFedToBc(federation, user.getValidUsername());
        if (baasIntegration && baasResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Baas status for the request is " + baasResponse.getStatusCode());
            return new ResponseEntity<>(baasResponse.getBody(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        // Inform the Federation Managers of the platform members
        federationNotificationService.notifyAboutFederationUpdate(federationWithInvitations);

        // Publish to federation queue
        rabbitManager.publishFederationCreation(federationWithInvitations);

        // Storing the new federation
        FederationWithInvitations federationReturned = federationRepository.save(federationWithInvitations);

        responseBody.put("message", "Federation Registration was successful!");
        responseBody.put("federation", federation);
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.CREATED);
    }


    public ResponseEntity<?> deleteFederation(String federationIdToDelete, boolean isAdmin, Principal principal) {

        log.debug("POST request for deleting federation with id = " + federationIdToDelete);

        Map<String, Object> response = new HashMap<>();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        Optional<FederationWithInvitations> federationToDelete = federationRepository.findById(federationIdToDelete);
        if (!federationToDelete.isPresent()) {
            response.put("error", "The federation does not exist");
            return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.NOT_FOUND);
        } else if (!isAdmin) {
            if (federationToDelete.get().getMembers().size() > 1) {
                response.put("error", "There are more than 1 platform in the federations");
                return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }

            // Check if the user owns the platform
            ResponseEntity<?> ownedPlatformDetailsResponse = checkServiceOwnershipService.checkIfUserOwnsService(
                    federationToDelete.get().getMembers().get(0).getPlatformId(), user, OwnedService.ServiceType.PLATFORM);
            if (ownedPlatformDetailsResponse.getStatusCode() != HttpStatus.OK) {
                response.put("error", "You do not own the single platform in the federation");
                return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }
        }

        ResponseEntity<?> baasResponse = baasService.deleteFederationBaasRequest(federationIdToDelete, user.getValidUsername());
        if (baasIntegration && baasResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Baas status for the request is " + baasResponse.getStatusCode());
            return new ResponseEntity<>(baasResponse.getBody(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        federationRepository.deleteById(federationIdToDelete);

        // Inform the Federation Managers of the platform members
        federationNotificationService.notifyAboutFederationDeletion(federationToDelete.get());

        // Publish to federation queue
        rabbitManager.publishFederationDeletion(federationToDelete.get().getId());

        response.put(federationToDelete.get().getId(), federationToDelete.get());
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);

    }

    public ResponseEntity<?> leaveFederation(String federationId, String organization, Principal principal, boolean isAdmin) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        Map<String, Object> responseBody = new HashMap<>();

        // Get platforms from organization
        ResponseEntity<String> response = baasService.getUserInfoBaasResponse(organization);
        if (response.getStatusCode() != HttpStatus.OK) {
            responseBody.put("error", response.getBody());
            return new ResponseEntity<>(responseBody, new HttpHeaders(), response.getStatusCode());
        }

        BaasUser baasUser = null;
        try {
            baasUser = mapper.readValue(response.getBody(), BaasUser.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        Set<String> platformFromOrganization = baasUser.getAssociated_platforms().keySet();

        if (!isAdmin) {
            // Check if the user owns the platform
            for (String platformId : platformFromOrganization) {
                ResponseEntity<?> ownedPlatformDetailsResponse = checkServiceOwnershipService.checkIfUserOwnsService(
                        platformId, user, OwnedService.ServiceType.PLATFORM);
                if (ownedPlatformDetailsResponse.getStatusCode() != HttpStatus.OK)
                    return ownedPlatformDetailsResponse;
            }
        }

        // Check if the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(federationId);
        if (!federation.isPresent()) {
            responseBody.put("error", "The federation does not exist");
            return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NOT_FOUND);
        }

        List<Integer> memberIndexList = new ArrayList<>();
        // Check if the platform is member of the federations
        for (String platformId : platformFromOrganization) {
            ResponseEntity<?> isPlatformMember = isPlatformMemberOfFederation(federation.get(), platformId);
            if (isPlatformMember.getStatusCode() != HttpStatus.OK)
                return isPlatformMember;

            memberIndexList.add((Integer) isPlatformMember.getBody());

        }


        // Check if the platform is the only member of the federation
        // Refactored for Organization
        if (platformFromOrganization.size() == 1 && isPlatformTheOnlyMemberOfFederation(federation.get(), platformFromOrganization.iterator().next())) {
            federationRepository.deleteById(federationId);
            federationNotificationService.notifyAboutFederationDeletion(federation.get());

            // Publish to federation queue
            rabbitManager.publishFederationDeletion(federation.get().getId());

            responseBody.put("deleted", true);
            return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NO_CONTENT);
        }

        // Save the whole list of members before removing the member that left
        List<FederationMember> initialMembers = new ArrayList<>(federation.get().getMembers());

        // Remove platform member
//        int memberIndex = 1;
        List<FederationMember> platformsLeft = new ArrayList<>();
        for (int memberIndex : memberIndexList) {
            platformsLeft.add(federation.get().getMembers().remove(memberIndex));
        }

        // Update the lastModified field
        federation.get().setLastModified(new Date());

        // Inform the Federation Managers of the remaining platform members
        federationNotificationService.notifyAboutFederationUpdate(federation.get(), federation.get().getMembers());

        // Inform the Federation Manager of the platform member that left
        federationNotificationService.notifyAboutFederationDeletion(federation.get(), platformsLeft);
//        federationNotificationService.notifyAboutFederationDeletion(federation.get(), new ArrayList<>(Collections.singletonList(memberLeft)));

        // Publish to federation queue
        rabbitManager.publishFederationUpdate(federation.get());

        federationRepository.save(federation.get());

        responseBody.put(federation.get().getId(), federation.get());
        ResponseEntity<?> baasResponse = baasService.leaveFedToBaas(user.getValidUsername(), federationId);
        if (baasResponse.getStatusCode() != HttpStatus.OK) {
            log.warn("Request on Baas was unsuccessful");
            responseBody.put("error", "Request on Baas was unsuccessful");
        }
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);
    }

    public ResponseEntity<?> inviteToFederation(InvitationRequest invitationRequest, Principal principal, boolean isAdmin) throws ServiceValidationException {

        Map<String, Object> responseBody = new HashMap<>();
        String organizationId = null;

        // Check if the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(invitationRequest.getFederationId());
        if (!federation.isPresent()) {
            responseBody.put("error", "The federation does not exist");
            return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NOT_FOUND);
        }

        Set<String> platformsFromOrganization = new HashSet<>();
        for (String organization :invitationRequest.getInvitedPlatforms()) {
            // Get platforms from organization
            organizationId = organization;
            ResponseEntity<String> response = baasService.getUserInfoBaasResponse(organization);
            if (response.getStatusCode() != HttpStatus.OK) {
                responseBody.put("error", response.getBody());
                return new ResponseEntity<>(responseBody, new HttpHeaders(), response.getStatusCode());
            }

            BaasUser baasUser = null;
            try {
                baasUser = mapper.readValue(response.getBody(), BaasUser.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            platformsFromOrganization.addAll(baasUser.getAssociated_platforms().keySet());
        }

        invitationRequest = new InvitationRequest(
                invitationRequest.getFederationId(),
                platformsFromOrganization
        );

        // Check if the user owns a platform in federation
        if (!isAdmin) {

            // Get user services
            ResponseEntity ownedPlatformsResponse = ownedServicesService.getOwnedPlatformDetails(principal);
            if (ownedPlatformsResponse.getStatusCode() != HttpStatus.OK) {
                responseBody.put("error", ownedPlatformsResponse.getBody());
                return new ResponseEntity<>(responseBody, new HttpHeaders(), ownedPlatformsResponse.getStatusCode());
            }

            Set<String> ownedPlatforms = ((Set<OwnedService>) ownedPlatformsResponse.getBody()).stream()
                    .map(OwnedService::getServiceInstanceId)
                    .collect(Collectors.toSet());
            Set<String> federationMembers = federation.get().getMembers().stream()
                    .map(FederationMember::getPlatformId).collect(Collectors.toSet());
            Set<String> intersection = new HashSet<>(federationMembers);
            intersection.retainAll(ownedPlatforms);

            if (intersection.isEmpty()) {
                String message = "You do not own any of the federation members in order to invite other platforms";
                responseBody.put("error", message);
                return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }

            // If the user owns the invited platforms add them immediately to the federation members and remove
            // the invitation
            HashSet<String> newInvitedPlatforms = new HashSet<>(invitationRequest.getInvitedPlatforms());
            Map<String, OwnedService> ownedPlatformsMap = ((Set<OwnedService>) ownedPlatformsResponse.getBody()).stream()
                    .collect(Collectors.toMap(OwnedService::getServiceInstanceId, ownedService -> ownedService));
            ArrayList<FederationMember> newMembers = new ArrayList<>(federation.get().getMembers());

            boolean federationUpdated = false;
            for (String invitedMemberId : invitationRequest.getInvitedPlatforms()) {
                if (ownedPlatforms.contains(invitedMemberId)) {
                    federationUpdated = true;
                    newMembers.add(new FederationMember(
                            invitedMemberId,
                            ownedPlatformsMap.get(invitedMemberId).getPlatformInterworkingInterfaceAddress()));
                    newInvitedPlatforms.remove(invitedMemberId);
                }
            }
            federation.get().setMembers(newMembers);
            invitationRequest = new InvitationRequest(invitationRequest.getFederationId(), newInvitedPlatforms);

            // if the federation members have been updated, inform the participating platforms
            if (federationUpdated)
                federationNotificationService.notifyAboutFederationUpdate(federation.get());
        }

        // Create the new invitations
        federation.get().openInvitations(invitationRequest.getInvitedPlatforms().stream()
                .map(invitedMember -> new FederationInvitation(invitedMember,
                        FederationInvitation.InvitationStatus.PENDING,
                        new Date()))
                .collect(Collectors.toSet()));

        ResponseEntity<?> fedVoteResponse = federationVoteRequestService.makeFederationAddRequest(federation.get().getId(), principal, organizationId);
        if (baasIntegration && fedVoteResponse.getStatusCode() != HttpStatus.OK) {
            responseBody.put("error", fedVoteResponse.getBody());
            return new ResponseEntity<>(fedVoteResponse.getBody(), new HttpHeaders(), fedVoteResponse.getStatusCode());
        }

        federationRepository.save(federation.get());

        ResponseEntity<String> fedResponse = baasService.getFederationInfoBaas(federation.get().getId());
        if (baasIntegration && fedResponse.getStatusCode() != HttpStatus.OK) {
            responseBody.put("error", fedResponse.getBody());
            return new ResponseEntity<>(responseBody, new HttpHeaders(), fedResponse.getStatusCode());
        }
        FedInfo fedInfo;
        try {
            fedInfo = mapper.readValue(fedResponse.getBody(), FedInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FederationWithOrganization returnFederation = new FederationWithOrganization(
                federation.get().getId(),
                new Date(),
                federation.get().getName(),
                federation.get().isPublic(),
                federation.get().getInformationModel(),
                federation.get().getSlaConstraints(),
                fedInfo.getSmartContract(),
                fedInfo.getMemberIds(),
                federation.get().getOpenInvitations(),
                fedInfo.getBalance(),
                fedInfo.getReputation()
        );
        returnFederation.setMembers(federation.get().getMembers());

        responseBody.put(federation.get().getId(), returnFederation);
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);
    }

    public ResponseEntity handleInvitationResponse(String federationId, String platformId, boolean accepted, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        Map<String, Object> responseBody = new HashMap<>();

        // Check if the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(federationId);
        if (!federation.isPresent()) {
            responseBody.put("error", "The federation does not exist");
            return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NOT_FOUND);
        }

        // Check if the user owns the platform
        ResponseEntity<?> ownedPlatformDetailsResponse = checkServiceOwnershipService.checkIfUserOwnsService(
                platformId, user, OwnedService.ServiceType.PLATFORM);
        if (ownedPlatformDetailsResponse.getStatusCode() != HttpStatus.OK) {
            responseBody.put("error", ownedPlatformDetailsResponse.getBody());
            return new ResponseEntity<>(responseBody, new HttpHeaders(), ownedPlatformDetailsResponse.getStatusCode());
        }

        // Handle the invitation
        federation.get().closeInvitation(platformId);
        if (accepted) {
            federation.get().getMembers().add(
                    new FederationMember(
                            platformId,
                            ((OwnedService) ownedPlatformDetailsResponse.getBody()).getPlatformInterworkingInterfaceAddress()
                    )
            );

            // Update the lastModified field
            federation.get().setLastModified(new Date());

            // Inform the Federation Managers of the platform members
            federationNotificationService.notifyAboutFederationUpdate(federation.get(), federation.get().getMembers());

            // Publish to federation queue
            rabbitManager.publishFederationUpdate(federation.get());
        }

        // Save the changes to the database
        federationRepository.save(federation.get());

        responseBody.put(federation.get().getId(), federation.get());
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);

    }

    public ResponseEntity joinedFederations(String platformId, HttpHeaders httpHeaders) {
        log.trace("Joined federations request for platformId = " + platformId + " and httpHeaders = " + httpHeaders);
        HttpHeaders test = authorizationService.getHttpHeadersWithSecurityRequest();
        ResponseEntity securityChecks = AuthorizationServiceHelper.checkJoinedFederationsRequestAndCreateServiceResponse(
                authorizationService, platformId, httpHeaders);

        if (securityChecks.getStatusCode() != HttpStatus.OK)
            return securityChecks;

        List<FederationWithInvitations> response = federationRepository.findAllByPlatformMember(platformId);

        return AuthorizationServiceHelper.addSecurityService(response, new HttpHeaders(),
                HttpStatus.OK, (String) securityChecks.getBody());
    }

    private ResponseEntity<?> isPlatformMemberOfFederation(Federation federation, String platformId) {

        // If found, return the index of the platform member
        for (int i = 0; i < federation.getMembers().size(); i++) {
            if (federation.getMembers().get(i).getPlatformId().equals(platformId))
                return new ResponseEntity<>(i, new HttpHeaders(), HttpStatus.OK);
        }

        Map<String, Object> responseBody = new HashMap<>();
        String message = "Platform " + platformId + " is not a member of federation " + federation.getId();
        log.warn(message);
        responseBody.put("error", message);
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);

    }

    private boolean isPlatformTheOnlyMemberOfFederation(Federation federation, String platformId) {
        if (federation.getMembers().size() > 1)
            return false;

        String message = "Platform " + platformId + " is the only a member of federation " + federation.getId();
        log.warn(message);
        return true;
    }

    public ResponseEntity<?> listFederationsByVertical(String vertical) {
        Map<String, Federation> symbioteFederationMap = federationRepository.findAll().stream()
                .collect(Collectors.toMap(Federation::getId, federation -> federation));

        ResponseEntity<String> baasResponse = baasService.getAllFedsFromBaas();
        if (baasResponse.getStatusCode() != HttpStatus.OK)
            return baasResponse;
        List<FedInfo> fedInfoList = null;
        try {
            fedInfoList = mapper.readValue(baasResponse.getBody(), new TypeReference<List<FedInfo>>() {
            });
        } catch (IOException e) {
            log.error("Mapping values has an error");
            e.printStackTrace();
        }


        List<FedInfo> fedInfosOfVertical = fedInfoList.stream().filter(fed ->
                fed.getRelatedApplications().contains(vertical)).collect(Collectors.toList());

        List<String> verticalFedsIdList = fedInfosOfVertical
                .stream()
                .map(FedInfo::getId)
                .collect(Collectors.toList());

        Map<String, Federation> verticalFederationMap = symbioteFederationMap.entrySet().stream().filter(fed ->
                verticalFedsIdList.contains(fed.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return new ResponseEntity<>(verticalFederationMap, HttpStatus.OK);
    }
}
