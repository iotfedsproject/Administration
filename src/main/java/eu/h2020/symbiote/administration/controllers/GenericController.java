package eu.h2020.symbiote.administration.controllers;

import eu.h2020.symbiote.administration.model.ServerInformation;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.services.federation.FederationService;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/administration/generic")
@CrossOrigin
public class GenericController {

    private static Log log = LogFactory.getLog(GenericController.class);

    private final FederationService federationService;
    private final ServerInformation serverInformation;
    private final FederationVoteRequestService federationVoteRequestService;

    @Autowired
    public GenericController(FederationService federationService, ServerInformation serverInformation, FederationVoteRequestService federationVoteRequestService) {
        this.federationService = federationService;
        this.serverInformation = serverInformation;
        this.federationVoteRequestService = federationVoteRequestService;
    }

    @PostMapping("/joinedFederations")
    public ResponseEntity<?> joinedFederations(@RequestParam String platformId, @RequestHeader HttpHeaders httpHeaders) {
        log.debug("POST request on /administration/generic/joinedFederations for platformId = "
                + platformId);
        return federationService.joinedFederations(platformId, httpHeaders);
    }

    @GetMapping("/information")
    @ResponseBody
    public ServerInformation information() {
        log.debug("POST request on /administration/generic/information");
        return serverInformation;
    }

    @Operation(summary = "End-point for Baas to return a voting result")
    @PostMapping("/result")
    public ResponseEntity<?> receiveVotingResult(
            @RequestParam String votingId,
            @RequestParam RequestStatus status) {

        log.debug("Post request on /administration/federation_vote_request/result");
        return federationVoteRequestService.handleVotingResponse(votingId, status);
    }
}
