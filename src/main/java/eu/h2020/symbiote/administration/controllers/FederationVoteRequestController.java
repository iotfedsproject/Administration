package eu.h2020.symbiote.administration.controllers;

import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.services.federation.FederationService;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import eu.h2020.symbiote.model.mim.Federation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/administration/federation_vote_request")
@CrossOrigin
public class FederationVoteRequestController {
    private static Log log = LogFactory.getLog(FederationVoteRequestController.class);
    @Autowired
    private FederationService federationService;
    @Autowired
    private FederationVoteRequestService federationVoteRequestService;

    @Autowired
    private FederationVoteRequestRepository federationVoteRequestRepository;

    @PostMapping("/joinFederation")
    public ResponseEntity<?> requestToJoinFederation(Principal principal, @RequestParam String federationId) throws ServiceValidationException {
        log.debug("POST request on /administration/federation_vote_request/joinFederation");
        return federationVoteRequestService.makeFederationJoinRequest(federationId, principal);
    }

    @PostMapping("/removeUserFromFed")
    public ResponseEntity<?> requestToDeleteFederationPlatform(Principal principal, @RequestParam String federationId, @RequestParam String platformId) throws ServiceValidationException {

        log.debug("POST request on /administration/federation_vote_request/removeUserFromFed");
        return federationVoteRequestService.deleteUserFromFederationRequest(platformId, federationId, principal);
    }

    @PostMapping("/deleteFederation")
    public ResponseEntity<?> requestToDeleteFederation(Principal principal, @RequestParam String federationId) throws ServiceValidationException {

        log.debug("POST request on /administration/federation_vote_request/deleteFederation");
        return federationVoteRequestService.deleteFederationRequest(federationId, principal);
    }

    @PostMapping("/updateFederationRules")
    @Operation(summary = "End-point to initiate a vote to change Federation Rules")
    public ResponseEntity<?> requestToUpdateFedRules(
            Principal principal,
            @RequestBody FederationWithSmartContract federation
//            @RequestBody SmartContract smartContract,
//            @RequestParam String federationId,
//            @RequestParam String username
    )
            throws ServiceValidationException {

        log.debug("POST request on /administration/federation_vote_request/updateFederationRules");
//        return federationVoteRequestService.updateFederationRulesRequest(smartContract, principal, federationId, username);
        return federationVoteRequestService.updateFederationRulesRequest(principal, federation);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRequests(@RequestHeader HttpHeaders httpHeaders) {
        log.debug("GET request on /administration/federation_join_request/all");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/allJoinRequests")
    public ResponseEntity<?> getAllRequests(Principal principal) {
        log.debug("GET request on /administration/federation_vote_request/allJoinRequests");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/allForUser")
    public ResponseEntity<?> getAllRequestsForUser(Principal principal) {
        log.debug("GET request on /administration/federation_vote_request/allForUser");
        return federationVoteRequestService.getAllRequestsForUser(principal);
    }

    @GetMapping("/join/all")
    @Operation(responses = {@ApiResponse(responseCode = "200", description = "Successful get", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FederationVoteRequest.class)))})})
    public ResponseEntity<?> getAllPendingJoinRequestsForUser(Principal principal) {
        log.debug("GET request on /administration/federation_vote_request/join/all");
        return federationVoteRequestService.getAllPendingJoinRequestsForUser(principal);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingRequests(@RequestHeader HttpHeaders httpHeaders) {
        log.debug("GET request on /administration/federation_vote_request/pending");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findByStatus(RequestStatus.PENDING), HttpStatus.OK);
    }

}
