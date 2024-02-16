package eu.h2020.symbiote.administration.services.baas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.Baas.Federation.FedInfo;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithOrganization;
import eu.h2020.symbiote.administration.model.Baas.Federation.RegisterFedToBc;
import eu.h2020.symbiote.administration.model.Baas.User.BaasUser;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.UserCreationRequest;
import eu.h2020.symbiote.security.communication.payloads.UserManagementRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.sql.Array;
import java.util.*;

@Service
public class BaasService {

    public BaasService() {
    }

    @Value("${baas.baseUrl}")
    private String baasBaseUrl;

    @Value("${baas.addFedMemberRequest.url}")
    private String addFedMemberRequestUrl;

    @Value("${baas.removeFedMemberRequest.url}")
    private String removeFedMemberRequestUrl;

    @Value("${baas.getAllUsers.url}")
    private String getAllUsersRequestUrl;

    @Value("${baas.getUserInfo.url}")
    private String getUserInfoRequestUrl;

    @Value("${baas.registerPlatform.url}")
    private String registerPlatformToBC;

    @Value("${baas.deletePlatform.url}")
    private String deletePlatformFromBC;

    @Value("${baas.registerResource.url}")
    private String registerResourceToBC;

    @Value("${baas.deleteResource.url}")
    private String deleteResourceFromBC;

    @Value("${baas.getAllFeds.url}")
    private String getAllFedsFromBC;

    @Value("${baas.getFeds.url}")
    private String getFedsFromBC;

    @Value("${baas.leaveFedFromBC.url}")
    private String leaveFedFromBC;

    @Value("${baas.registerFedToBc.url}")
    private String registerFedToBcUrl;

    @Value("${baas.updateRulesRequestUrl.url}")
    private String updateRulesRequestUrl;

    @Value("${baas.registerUserUrl.url}")
    private String registerUserUrl;

    @Value("${baas.deleteUserUrl.url}")
    private String deleteUserUrl;

    @Value("${baas.deleteFederation.url}")
    private String deleteFederation;

    @Autowired
    private BaasClient baasClient;

    private static final Log log = LogFactory.getLog(BaasService.class);

    //    Vote request to add a member to a federation
//    not tested
    public ResponseEntity<String> makeAddMemberToFederationBaasRequest(FederationVoteRequest voteRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("member_id", voteRequest.getUsername());
        body.put("requestor_id", voteRequest.getRequestingUser());
        body.put("fed_id", voteRequest.getFederationId());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, addFedMemberRequestUrl, HttpMethod.POST, body, parameters);
    }
    public ResponseEntity<String> makeJoinMemberToFederationBaasRequest(FederationVoteRequest voteRequest) {

        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> response = getFederationInfoBaas(voteRequest.getFederationId());
        FedInfo fedInfo;
        try {
            fedInfo = mapper.readValue(response.getBody(), FedInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("member_id", voteRequest.getUsername());
        body.put("requestor_id", fedInfo.getCreatorId());
        body.put("fed_id", voteRequest.getFederationId());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, addFedMemberRequestUrl, HttpMethod.POST, body, parameters);
    }

    //    Vote request to delete a member from a federation
//    Requested comments
//    not tested
    public ResponseEntity<String> makeDeleteMemberOfFederationBaasRequest(FederationVoteRequest voteRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("user_id", voteRequest.getUsername());
        body.put("requestor_id", voteRequest.getRequestingUser());
        body.put("fed_id", voteRequest.getFederationId());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.POST, body, parameters);
    }

    //    Vote request to change fed rules

    public ResponseEntity<String> makeUpdateRulesOfFederationBaasRequest(FederationVoteRequest voteRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("user_id", voteRequest.getRequestingUser());
        body.put("fed_id", voteRequest.getFederationId());
        body.put("new_rules", voteRequest.getSmartContract().toString());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, updateRulesRequestUrl, HttpMethod.POST, body, parameters);
    }

    // Make a vote request to delete a member
    public ResponseEntity<String> makeDeleteFederationBaasRequest(FederationVoteRequest voteRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("fed_id", voteRequest.getFederationId());
        body.put("request_user_id", voteRequest.getRequestingUser());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.DELETE, body, parameters);
    }
    // Delete a federation from baas
    public ResponseEntity<String> deleteFederationBaasRequest(String federationId, String userId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("fed_id", federationId);
        body.put("request_user_id", userId);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, deleteFederation, HttpMethod.DELETE, body, parameters);
    }

    //Get info for a federation
    public ResponseEntity<String> getFederationInfoBaas(String federationId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("fed_id", federationId);

        HashMap<String, String> body = new HashMap<>();

        return baasClient.makeBaasHttpRequest(baasBaseUrl, getFedsFromBC, HttpMethod.GET, body, parameters);
    }

    //Info for all users
    public ResponseEntity<String> getAllUsersBaasResponse() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        return baasClient.makeBaasHttpRequest(baasBaseUrl, getAllUsersRequestUrl, HttpMethod.GET, body, parameters);
    }

    // Info for a user
    public ResponseEntity<String> getUserInfoBaasResponse(String username) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("user_id", username);

        HashMap<String, String> body = new HashMap<>();

        return baasClient.makeBaasHttpRequest(baasBaseUrl, getUserInfoRequestUrl, HttpMethod.GET, body, parameters);
    }

    // Register the user
    public ResponseEntity<String> registerUserToBcBaasRequest(UserCreationRequest request) {

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("id", request.getValidUsername());
        body.put("role", request.getIotfedsrole());
        body.put("mail", request.getRecoveryMail());
        body.put("organization", request.getOrganization());

        return baasClient.makeBaasHttpRequest(baasBaseUrl, registerUserUrl, HttpMethod.POST, body, parameters);
    }

    // Delete the user
    public ResponseEntity<String> deleteUserToBcBaasRequest(String userId) {

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("user_id", userId);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, deleteUserUrl, HttpMethod.DELETE, body, parameters);
    }
    // Register the user
    public ResponseEntity<String> registerUserToBcBaasRequest(UserManagementRequest request) {

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("id", request.getUserDetails().getCredentials().getUsername());
        body.put("role", "tester");
        body.put("mail", request.getUserDetails().getRecoveryMail());
        body.put("organization", "ICOM");

        return baasClient.makeBaasHttpRequest(baasBaseUrl, registerUserUrl, HttpMethod.POST, body, parameters);
    }

    public ResponseEntity<String> registerPlatformToBaas(String username, String platformId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("platform_id", platformId);
        body.put("assoc_user_id", username);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, registerPlatformToBC, HttpMethod.POST, body, parameters);
    }

    public ResponseEntity<String> deletePlatformFromBaas(String username, String platformId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();
        body.put("platform_id", platformId);
        body.put("assoc_user_id", username);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, deletePlatformFromBC, HttpMethod.DELETE, body, parameters);
    }

    public ResponseEntity<String> registerResourceToBaas(String platformId, String resourceId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();
        body.put("device_id", resourceId);
        body.put("platform_id", platformId);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, registerResourceToBC, HttpMethod.POST, body, parameters);
    }

    public ResponseEntity<String> deleteResourceFromBaas(String platformId, String resourceId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();
        body.put("device_id", resourceId);
        body.put("platform_id", platformId);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, registerResourceToBC, HttpMethod.DELETE, body, parameters);
    }

    //done
    public ResponseEntity<String> getAllFedsFromBaas() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        return baasClient.makeBaasHttpRequest(baasBaseUrl, getAllFedsFromBC, HttpMethod.GET, body, parameters);
    }

    public ResponseEntity<String> leaveFedToBaas(String username, String federationId) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        HashMap<String, String> body = new HashMap<>();

        body.put("fed_id", federationId);
        body.put("user_id", username);

        return baasClient.makeBaasHttpRequest(baasBaseUrl, leaveFedFromBC, HttpMethod.DELETE, body, parameters);
    }

    //TODO: not validated yet
    public ResponseEntity<String> registerFedToBc(FederationWithOrganization federation, String username) {
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> baasResponse = getUserInfoBaasResponse(username);
        if (baasResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Baas status for the request is " + baasResponse.getStatusCode());
            return new ResponseEntity<String>(baasResponse.getBody(), HttpStatus.BAD_REQUEST);
        }
        BaasUser baasUser;
        try {
            baasUser = mapper.readValue(baasResponse.getBody(), BaasUser.class);
        } catch (IOException e) {
            return new ResponseEntity<String>("Error decoding user", HttpStatus.BAD_REQUEST);
        }
        RegisterFedToBc registerFedToBc = new RegisterFedToBc(
                federation.getId(),
                username,
                new ArrayList<>(),
                federation.getInformationModel().getId(),
                federation.getSmartContract(),
                baasUser.getOrganization(),
                baasUser.getEmail()
        );

        ObjectWriter ow = new ObjectMapper().writer();
        ResponseEntity<String> response = null;

        try {
            response = baasClient.makeBaasHttpRequest2(baasBaseUrl, registerFedToBcUrl, HttpMethod.POST, ow.writeValueAsString(registerFedToBc), null);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    //Checks
    public void userExistsCheckThrowException(String userName) throws ServiceValidationException {
        Map<String, String> responseBody = new HashMap<>();
        ResponseEntity<String> response = getUserInfoBaasResponse(userName);
        if (!(response.getStatusCode() == HttpStatus.OK)) {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.debug("The user " + userName + " does not exist in Baas!");
                responseBody.put("error", "The user " + userName + " does not exist in Baas!");
            } else {
                log.debug(response.getBody());
                responseBody.put("error", response.getBody());
            }
            throw new ServiceValidationException(null, responseBody);
        }
    }

    public ResponseEntity<String> checkConnection() {

        return baasClient.makeBaasHttpRequest(baasBaseUrl, getUserInfoRequestUrl, HttpMethod.OPTIONS, null, null);
    }

    public boolean checkIfUserIsInFederationInBaas(String username, String federationId) {

        FedInfo fedInfo = new FedInfo();
        ObjectMapper mapper = new ObjectMapper();
        try {
            fedInfo = mapper.readValue(getFederationInfoBaas(federationId).getBody(), FedInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fedInfo.getMemberIds().contains(username);
    }

}
