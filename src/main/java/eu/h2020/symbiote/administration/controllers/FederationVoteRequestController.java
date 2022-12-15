package eu.h2020.symbiote.administration.controllers;

import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.services.federation.FederationService;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
        HttpHeaders httpHeaders = new HttpHeaders();
        log.debug("GET request on /administration/federation_vote_request/joinFederation");
        return federationVoteRequestService.makeFederationJoinRequest(federationId, httpHeaders, principal);
    }

    @DeleteMapping("/removeUserFromFed")
    public ResponseEntity<?> requestToDeleteFederationPlatform(Principal principal, @RequestParam String federationId, @RequestParam String userNameToRemove) throws ServiceValidationException {

        log.debug("GET request on /administration/federation_vote_request/removeUserFromFed");
        return federationVoteRequestService.deleteUserFromFederationRequest(userNameToRemove,federationId, principal);
    }

    @PostMapping("/deleteFederation")
    public ResponseEntity<?> requestToDeleteFederation(Principal principal, @RequestParam String federationId) throws ServiceValidationException {

        log.debug("GET request on /administration/federation_vote_request/deleteFederation");
        return federationVoteRequestService.deleteFederationRequest(federationId, principal);
    }

    @PostMapping("/updateFederationRules")
    @ApiOperation(value = "End-point to initiate a vote to change Federation Rules")
    public ResponseEntity<?> requestToDeleteFederation(Principal principal, @RequestBody SmartContract smartContract, @RequestParam String federationId, @RequestParam String username) throws ServiceValidationException {

        log.debug("GET request on /administration/federation_vote_request/updateFederationRules");
        return federationVoteRequestService.updateFederationRulesRequest(smartContract, principal, federationId, username);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRequests(@RequestHeader HttpHeaders httpHeaders) {
        log.debug("GET request on /administration/federation_join_request/all");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findAll(), HttpStatus.OK) ;
    }

    @GetMapping("/allJoinRequests")
    public ResponseEntity<?> getAllRequests(Principal principal) {
        log.debug("GET request on /administration/federation_join_request/allJoinRequests");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findAll(), HttpStatus.OK) ;
    }

    @GetMapping("/allForUser")
    public ResponseEntity<?> getAllRequestsForUser(Principal principal) {
        log.debug("GET request on /administration/federation_join_request/allForUser");
        return federationVoteRequestService.getAllRequestsForUser(principal);
    }

    @GetMapping("/join/all")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful get", response = FederationVoteRequest.class, responseContainer = "List")})
    public ResponseEntity<?> getAllPendingJoinRequestsForUser(Principal principal) {
        log.debug("GET request on /administration/federation_vote_request/join/all");
        return federationVoteRequestService.getAllPendingJoinRequestsForUser(principal) ;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingRequests(@RequestHeader HttpHeaders httpHeaders) {
        log.debug("GET request on /administration/federation_join_request/pending");
        return new ResponseEntity<List<FederationVoteRequest>>(federationVoteRequestRepository.findByStatus(RequestStatus.PENDING), HttpStatus.OK) ;
    }

    @ApiOperation(value = "End-point for Baas to return a voting result")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Voting Result received successfully")})
    @PostMapping("/result")
    public ResponseEntity<?> receiveVotingResult(
            @RequestParam String votingId,
            @RequestParam RequestStatus status) {

        log.debug("Post request on /administration/federation_vote_request/result");
        return federationVoteRequestService.handleVotingResponse(votingId, status);
    }
}
