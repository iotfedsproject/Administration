package eu.h2020.symbiote.administration.services.federationVoteRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.communication.rabbit.RabbitManager;
import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.Baas.Federation.AddFederationBaasResponse;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.administration.repository.FederationRepository;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.services.baas.BaasService;
import eu.h2020.symbiote.administration.services.federation.FederationNotificationService;
import eu.h2020.symbiote.administration.services.ownedservices.CheckServiceOwnershipService;
import eu.h2020.symbiote.administration.services.ownedservices.OwnedServicesService;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.security.communication.payloads.OwnedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static eu.h2020.symbiote.administration.converters.SmartContractToConstraintsConverter.convertSmartContractToConstrains;

@Component
public class FederationVoteRequestService {

    private static final Log log = LogFactory.getLog(FederationVoteRequestService.class);
    private final RabbitManager rabbitManager;
    private final FederationVoteRequestRepository federationVoteRequestRepository;
    private final BaasService baasService;
    private final FederationRepository federationRepository;
    private final CheckServiceOwnershipService checkServiceOwnershipService;
    private final FederationNotificationService federationNotificationService;
    private final OwnedServicesService ownedServicesService;
    private final boolean baasIntegration;

    @Autowired
    public FederationVoteRequestService(RabbitManager rabbitManager, FederationVoteRequestRepository federationVoteRequestRepository,
                                        BaasService baasService, FederationRepository federationRepository, CheckServiceOwnershipService checkServiceOwnershipService,
                                        FederationNotificationService federationNotificationService, OwnedServicesService ownedServicesService,
                                        @Value("${symbiote.baas.integration}") boolean baasIntegration) {
        Assert.notNull(baasService, "baasService can not be null!");
        this.baasService = baasService;
        Assert.notNull(federationVoteRequestRepository, "federationVoteRequestRepository can not be null!");
        this.federationVoteRequestRepository = federationVoteRequestRepository;
        Assert.notNull(federationRepository, "federationRepository can not be null!");
        this.federationRepository = federationRepository;
        Assert.notNull(checkServiceOwnershipService, "checkServiceOwnershipService can not be null!");
        this.checkServiceOwnershipService = checkServiceOwnershipService;
        Assert.notNull(rabbitManager, "rabbitManager can not be null!");
        this.rabbitManager = rabbitManager;
        Assert.notNull(federationNotificationService, "federationNotificationService can not be null!");
        this.federationNotificationService = federationNotificationService;
        Assert.notNull(ownedServicesService, "ownedServicesService can not be null!");
        this.ownedServicesService = ownedServicesService;
        Assert.notNull(baasIntegration, "baasIntegration can not be null!");
        this.baasIntegration = baasIntegration;
    }

    public boolean checkIfUserIsInFederation(String userName, String federationId) {
        List<FederationVoteRequest> voteRequestsAccepted = federationVoteRequestRepository.findAllForUserByStatusFederationAndVoteAction(userName, RequestStatus.ACCEPTED, VoteAction.ADD_MEMBER, federationId);
        //Check if the user has ever been accepted in the federation
        if (voteRequestsAccepted.isEmpty())
            return false;
        List<FederationVoteRequest> voteRequestsRemoved = voteRequestsAccepted.stream().filter(voteRequest ->
                voteRequest.getVoteAction().equals(VoteAction.REMOVE_MEMBER)).collect(Collectors.toList());
        if (voteRequestsRemoved.isEmpty())
            return true;
        List<FederationVoteRequest> voteRequestsAdded = voteRequestsAccepted.stream().filter(voteRequest ->
                voteRequest.getVoteAction().equals(VoteAction.ADD_MEMBER)).collect(Collectors.toList());
        Collections.sort(voteRequestsRemoved);
        Collections.sort(voteRequestsAdded);
        return voteRequestsAdded.get(voteRequestsAdded.size() - 1).compareTo(voteRequestsRemoved.get(voteRequestsRemoved.size() - 1)) > 0;
    }

    private ResponseEntity<?> isPlatformMemberOfFederation(Federation federation, String platformId) {

        // If found, return the index of the platform member
        for (int i = 0; i < federation.getMembers().size(); i++) {
            if (federation.getMembers().get(i).getPlatformId().equals(platformId))
                return new ResponseEntity<>(i, HttpStatus.OK);
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

    public ResponseEntity<?> makeFederationAddRequest(String federationId, Principal principal, String userToAdd) throws ServiceValidationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        if (baasIntegration && connectionResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(connectionResponse.getBody(), new HttpHeaders(), connectionResponse.getStatusCode());
        }

        //Check that the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(federationId);
        federationExistsCheckThrowException(federation);

        //Check if the platform is already a member of the federation
//        platformCheckThrowException(federation.get(), voteRequest.getRequestingPlatformId());

        //Check if there is such a request pending
        requestCheckThrowException(federationId, user.getValidUsername(), VoteAction.ADD_MEMBER);

        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(federationId, user.getValidUsername(), VoteAction.ADD_MEMBER);
        federationVoteRequest.setUsername(userToAdd);

        ResponseEntity<String> baasResponse = baasService.makeAddMemberToFederationBaasRequest(federationVoteRequest);
        log.info("Baas status for the request is " + baasResponse.getStatusCode());
        return getResponseEntity(federationVoteRequest, baasResponse);
    }

    public ResponseEntity<?> makeFederationJoinRequest(String federationId, Principal principal) throws ServiceValidationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(federationId, user.getValidUsername(), VoteAction.ADD_MEMBER);
        federationVoteRequest.setUsername(user.getUsername());

        ResponseEntity<String> baasResponse = baasService.makeJoinMemberToFederationBaasRequest(federationVoteRequest);
        log.info("Baas status for the request is " + baasResponse.getStatusCode());
        return getResponseEntity(federationVoteRequest, baasResponse);
    }

    public ResponseEntity<?> deleteUserFromFederationRequest(String userNameToRemove, String federationId, Principal principal) throws ServiceValidationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        if (baasIntegration && connectionResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(connectionResponse.getBody(), new HttpHeaders(), connectionResponse.getStatusCode());
        }

        //Check that the user to remove exists in Baas
        baasService.userExistsCheckThrowException(userNameToRemove);

        //Check that the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(federationId);
        federationExistsCheckThrowException(federation);

        //Check if there is such a request pending
        requestCheckThrowException(federationId, userNameToRemove, VoteAction.REMOVE_MEMBER);

        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(federationId, user.getValidUsername(), VoteAction.REMOVE_MEMBER);
        federationVoteRequest.setUsername(userNameToRemove);//The action refers to a different user than the one making the request

        ResponseEntity<String> baasResponse = baasService.makeDeleteMemberOfFederationBaasRequest(federationVoteRequest);
        log.info("Baas status for the request is " + baasResponse.getStatusCode());
        return getResponseEntity(federationVoteRequest, baasResponse);
    }

    public ResponseEntity<?> deleteFederationRequest(String federationId, Principal principal) throws ServiceValidationException {

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        if (baasIntegration && connectionResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(connectionResponse.getBody(), new HttpHeaders(), connectionResponse.getStatusCode());
        }

        //Check that the federation exists
        Optional<FederationWithInvitations> federation = federationRepository.findById(federationId);
        federationExistsCheckThrowException(federation);

        //Check if there is such a request pending
        requestCheckThrowException(federationId, user.getValidUsername(), VoteAction.DELETE_FEDERATION);

        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(federationId, user.getValidUsername(), VoteAction.REMOVE_MEMBER);

        ResponseEntity<String> baasResponse = baasService.makeDeleteFederationBaasRequest(federationVoteRequest);
        log.info("Baas status for the request is " + baasResponse.getStatusCode());
        return getResponseEntity(federationVoteRequest, baasResponse);
    }

//    public ResponseEntity<?> updateFederationRulesRequest(SmartContract smartContract, Principal principal, String federationId, String username) throws ServiceValidationException {
    public ResponseEntity<?> updateFederationRulesRequest(Principal principal, FederationWithSmartContract federation) throws ServiceValidationException {

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        VoteRequest voteRequest = new VoteRequest(federation.getId(), user.getValidUsername());

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        if (baasIntegration && connectionResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(connectionResponse.getBody(), new HttpHeaders(), connectionResponse.getStatusCode());
        }

        //Check that the federation exists
//        Optional<FederationWithInvitations> federation = federationRepository.findById(voteRequest.getFederationId());
//        federationExistsCheckThrowException(federation);

        //Check if there is such a request pending
        requestCheckThrowException(voteRequest.getFederationId(), user.getValidUsername(), VoteAction.UPDATE_FEDERATION_RULES);

        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(
                voteRequest.getFederationId(),
                user.getValidUsername(),
                federation.getSmartContract(),
                VoteAction.REMOVE_MEMBER);

        ResponseEntity<?> baasResponse = baasService.makeUpdateRulesOfFederationBaasRequest(federationVoteRequest);
        log.info("Baas status for the request is " + baasResponse.getStatusCode());
//        return getResponseEntity(federationVoteRequest, baasResponse);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Your change federation rules request is forwarded to voting procedure");
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);

    }

    public ResponseEntity<?> handleVotingResponse(String votingId, RequestStatus status) {

        // Check if the votingId exists
        Optional<FederationVoteRequest> federationVoteRequest = federationVoteRequestRepository.findByVotingId(votingId);
        if (!federationVoteRequest.isPresent()) {
            log.debug("The votingId "+ votingId +" does not exist");
            return responseEntityObject("error", "The votingId does not exist", HttpStatus.NOT_FOUND);
        }

        Optional<FederationWithInvitations> federation = federationRepository.findById(federationVoteRequest.get().getFederationId());
        if (!federation.isPresent()) {
            log.debug("The federation does not exist");
            return responseEntityObject("error", "The federation does not exist", HttpStatus.NOT_FOUND);
        }

        VoteAction votingRequest = federationVoteRequest.get().getVoteAction();
        // Handle the invitation
        if (status.equals(RequestStatus.ACCEPTED)) {

            switch (votingRequest) {
                case ADD_MEMBER:
                    return handleAddUserAfterVote(status, federationVoteRequest.get(), federation.get());

                case REMOVE_MEMBER:
                    return handleRemoveUserAfterVote(status, federationVoteRequest.get(), federation.get());

                case UPDATE_FEDERATION_RULES:
                    return handleUpdateRulesAfterVote(status, federationVoteRequest.get(), federation.get());

                case DELETE_FEDERATION:
                    return handleRemoveFederationAfterVote(status, federationVoteRequest.get(), federation.get());

            }
        } else if (status.equals(RequestStatus.REJECTED)) {

            federationVoteRequest.get().setStatus(status);
            federationVoteRequest.get().setHandledDate(new Date());
            federationVoteRequestRepository.save(federationVoteRequest.get());
            return responseEntityObject(federation.get().getId(), "The voting request was rejected", HttpStatus.OK);
        }

        return responseEntityObject("error", "Not valid status", HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> responseEntityObject(String key, Object message, HttpStatus httpStatusCode) {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(key, message);
        return new ResponseEntity<>(responseBody, new HttpHeaders(), httpStatusCode);
    }

    private void federationExistsCheckThrowException(Optional<FederationWithInvitations> federation) throws ServiceValidationException {
        Map<String, String> responseBody = new HashMap<>();

        if (!federation.isPresent()) {
            log.debug("The requested federation does not exist!");
            responseBody.put("error", "The requested federation does not exist!");
            throw new ServiceValidationException(null, responseBody);
        }
    }

    private void requestCheckThrowException(String federationId, String username, VoteAction voteAction) throws ServiceValidationException {
        Map<String, String> responseBody = new HashMap<>();

        List<FederationVoteRequest> requests = federationVoteRequestRepository.findAllForUserByStatusFederationAndVoteAction(username, RequestStatus.PENDING, voteAction, federationId);
//        Optional<FederationVoteRequest> requests = federationVoteRequestRepository.findAllForPlatformByStatusAndFederation(voteRequest.getRequestingPlatformId(), RequestStatus.PENDING, voteRequest.getFederationId());
        if (!requests.isEmpty()) {
            log.debug("Such a request exists already!");
            responseBody.put("error", "Such a request exists already!");
            throw new ServiceValidationException(null, responseBody);
        }
    }

    private ResponseEntity<?> getResponseEntity(FederationVoteRequest federationVoteRequest, ResponseEntity<String> baasResponse) {
        if (!(baasResponse.getStatusCode() == HttpStatus.OK))
            return new ResponseEntity<>(baasResponse.getBody(), HttpStatus.BAD_REQUEST);

        ObjectMapper mapper = new ObjectMapper();
        AddFederationBaasResponse response = null;
        try {
            response = mapper.readValue(baasResponse.getBody(), AddFederationBaasResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        federationVoteRequest.setVotingId(response.getVotingId());

        federationVoteRequestRepository.save(federationVoteRequest);
        return new ResponseEntity<FederationVoteRequest>(federationVoteRequest, HttpStatus.OK);
    }

    private ResponseEntity<?> handleAddUserAfterVote(RequestStatus status, FederationVoteRequest federationVoteRequest, FederationWithInvitations federation) {

        ResponseEntity<ListUserServicesResponse> userServicesResponse = ownedServicesService.listUserServices(federationVoteRequest.getUsername());
        if (userServicesResponse.getStatusCode() != HttpStatus.OK)
            return userServicesResponse;
        List<PlatformDetails> availablePlatforms = userServicesResponse.getBody().getAvailablePlatforms();
//        String federationId = federationVoteRequest.getFederationId();

        try {
            for (PlatformDetails platform : availablePlatforms) {

                federation.getMembers().add(
                        new FederationMember(
                                platform.getId(),
                                platform.getInterworkingServices().get(0).getUrl())//The list of interworking services only holds one url, even though it's a list
                );

                // Update the lastModified field
            }
            federation.setLastModified(new Date());
            // Publish to federation queue
            rabbitManager.publishFederationUpdate(federation);
            // Save the changes to the database
            federationRepository.save(federation);

            federationVoteRequest.setStatus(status);
            federationVoteRequest.setHandledDate(new Date());
            federationVoteRequestRepository.save(federationVoteRequest);

        } catch (Exception e) {
            return responseEntityObject("error", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
//        return responseEntityObject(federation.getId(), federation, HttpStatus.OK);
        return responseEntityObject("message", "Vote successfully registered", HttpStatus.OK);
    }

    private ResponseEntity<?> handleRemoveUserAfterVote(RequestStatus status, FederationVoteRequest federationVoteRequest, FederationWithInvitations federation) {

        ResponseEntity<ListUserServicesResponse> userServicesResponse = ownedServicesService.listUserServices(federationVoteRequest.getUsername());
        if (userServicesResponse.getStatusCode() != HttpStatus.OK)
            return userServicesResponse;
        List<PlatformDetails> availablePlatforms = userServicesResponse.getBody().getAvailablePlatforms();
        String federationId = federationVoteRequest.getFederationId();

        for (PlatformDetails platform : availablePlatforms) {
            // Check if the platform is member of the federations
            ResponseEntity<?> isPlatformMember = isPlatformMemberOfFederation(federation, platform.getId());
            if (isPlatformMember.getStatusCode() != HttpStatus.OK)
                return isPlatformMember;

            int memberIndex = (Integer) isPlatformMember.getBody();

            // Check if the platform is the only member of the federation
            if (isPlatformTheOnlyMemberOfFederation(federation, platform.getId())) {
                federationRepository.deleteById(federationId);
                federationNotificationService.notifyAboutFederationDeletion(federation);

                // Publish to federation queue
                rabbitManager.publishFederationDeletion(federation.getId());
            }

            // Remove platform member
            FederationMember memberLeft = federation.getMembers().remove(memberIndex);

            // Update the lastModified field
            federation.setLastModified(new Date());

            federationVoteRequest.setStatus(status);
            federationVoteRequest.setHandledDate(new Date());

            // Inform the Federation Managers of the remaining platform members
            federationNotificationService.notifyAboutFederationUpdate(federation, federation.getMembers());

            // Inform the Federation Manager of the platform member that left
            federationNotificationService.notifyAboutFederationDeletion(federation, new ArrayList<>(Collections.singletonList(memberLeft)));

            // Publish to federation queue
            rabbitManager.publishFederationUpdate(federation);

            federationRepository.save(federation);

        }
        federationVoteRequest.setHandledDate(new Date());
        federationVoteRequestRepository.save(federationVoteRequest);

        return responseEntityObject(federation.getId(), federation, HttpStatus.OK);
    }

    private ResponseEntity<?> handleRemoveFederationAfterVote(RequestStatus status, FederationVoteRequest federationVoteRequest, FederationWithInvitations federation) {

        String federationIdToDelete = federationVoteRequest.getFederationId();

        log.debug("POST request for deleting federation with id = " + federationIdToDelete);

        Optional<FederationWithInvitations> federationToDelete = federationRepository.findById(federationIdToDelete);
        if (!federationToDelete.isPresent()) {

            return responseEntityObject("error", "The federation does not exist", HttpStatus.NOT_FOUND);
        } else if (federationToDelete.get().getMembers().size() > 1) {

            return responseEntityObject("error", "There are more than 1 platform in the federations", HttpStatus.BAD_REQUEST);
        }

        federationVoteRequest.setStatus(status);
        federationVoteRequest.setHandledDate(new Date());

        federationRepository.deleteById(federationIdToDelete);

        // Inform the Federation Managers of the platform members
        federationNotificationService.notifyAboutFederationDeletion(federationToDelete.get());

        // Publish to federation queue
        rabbitManager.publishFederationDeletion(federationToDelete.get().getId());
        federationVoteRequestRepository.save(federationVoteRequest);

        return responseEntityObject(federation.getId(), federation, HttpStatus.OK);
    }

    private ResponseEntity<?> handleUpdateRulesAfterVote(RequestStatus status, FederationVoteRequest federationVoteRequest, FederationWithInvitations federation) {
        federationVoteRequest.setStatus(status);
        federationVoteRequest.setHandledDate(new Date());

        federation.setSlaConstraints(convertSmartContractToConstrains(federationVoteRequest.getSmartContract()));
        federationVoteRequestRepository.save(federationVoteRequest);
        federationRepository.save(federation);

        return responseEntityObject(federation.getId(), federation, HttpStatus.OK);
    }

    public ResponseEntity<?> addPlatformToFederation(String federationId, String platformId, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        if (!baasService.checkIfUserIsInFederationInBaas(federationId, user.getUsername()))
            return new ResponseEntity("You are not a member of " + federationId + " federation!", HttpStatus.UNAUTHORIZED);

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

        federation.get().getMembers().add(
                new FederationMember(
                        platformId,
                        ((OwnedService) ownedPlatformDetailsResponse.getBody()).getPlatformInterworkingInterfaceAddress()
                ));

        // Update the lastModified field
        federation.get().setLastModified(new Date());

        // Inform the Federation Managers of the platform members
        federationNotificationService.notifyAboutFederationUpdate(federation.get(), federation.get().getMembers());

        // Publish to federation queue
        rabbitManager.publishFederationUpdate(federation.get());

        // Save the changes to the database
        federationRepository.save(federation.get());

        responseBody.put(federation.get().getId(), federation.get());
        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);
    }

    public ResponseEntity<?> getAllPendingJoinRequestsForUser(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        List<FederationVoteRequest> voteRequests = federationVoteRequestRepository.findAllForUserByStatusAndVoteAction(user.getValidUsername(),
                RequestStatus.PENDING, VoteAction.ADD_MEMBER);
        return new ResponseEntity<>(voteRequests, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllRequestsForUser(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();

        List<FederationVoteRequest> voteRequests = federationVoteRequestRepository.findAllForUser(user.getValidUsername());
        return new ResponseEntity<>(voteRequests, HttpStatus.OK);
    }
}

