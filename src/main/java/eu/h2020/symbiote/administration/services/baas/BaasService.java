package eu.h2020.symbiote.administration.services.baas;

import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.Baas.Federation.AddFederationBaasResponse;
import eu.h2020.symbiote.administration.model.Baas.Federation.FedInfo;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.RegisterFedToBc;
import eu.h2020.symbiote.administration.model.Baas.User.BaasUser;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.UserCreationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    @Value("${baas.leaveFedFromBC.url}")
    private String leaveFedFromBC;

    @Value("${baas.registerFedToBc.url}")
    private String registerFedToBcUrl;

    @Autowired
    private BaasClient baasClient;

    private static Log log = LogFactory.getLog(BaasService.class);

    public ResponseEntity<?> makeAddMemberToFederationBaasRequest(FederationVoteRequest voteRequest){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("user_id", voteRequest.getRequestingUser().getUsername());
        parameters.add("fed_id", voteRequest.getFederationId());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, addFedMemberRequestUrl, HttpMethod.POST , body, headers, parameters, AddFederationBaasResponse.class);
    }

    public ResponseEntity<?> makeDeleteMemberOfFederationBaasRequest(FederationVoteRequest voteRequest){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("user_id", voteRequest.getRequestingUser().getUsername());
        parameters.add("fed_id", voteRequest.getFederationId());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.POST , body, headers, parameters, AddFederationBaasResponse.class);
    }

    public ResponseEntity<?> makeUpdateRulesOfFederationBaasRequest(FederationVoteRequest voteRequest){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("user_id", voteRequest.getRequestingUser().getUsername());
        parameters.add("fed_id", voteRequest.getFederationId());
        parameters.add("new_rules",voteRequest.getSmartContract().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.POST , body, headers, parameters, AddFederationBaasResponse.class);
    }

    public ResponseEntity<?> makeDeleteFederationBaasRequest(FederationVoteRequest voteRequest){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
//        parameters.add("platform_id", voteRequest.getRequestingPlatformId());
        parameters.add("fed_id", voteRequest.getFederationId());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.DELETE , body, headers, parameters, AddFederationBaasResponse.class);
    }

    public ResponseEntity<?> getFederationInfoBaas(String federationId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("fed_id", federationId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, removeFedMemberRequestUrl, HttpMethod.GET , body, headers, parameters, AddFederationBaasResponse.class);
    }

    private ResponseEntity<?> getAllUsersBaasResponse(){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, getAllUsersRequestUrl, HttpMethod.GET , body, headers, parameters, BaasUser[].class);
        return response;
    }

    public ResponseEntity<?> getUserInfoBaasResponse(String username){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("user_id", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, getUserInfoRequestUrl, HttpMethod.GET , body, headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> registerUserToBcBaasRequest(UserCreationRequest request){

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("id", request.getValidUsername());
        parameters.add("role", request.getIotfedsrole());
        parameters.add("mail", request.getRecoveryMail());
        parameters.add("organization", request.getOrganization());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";

        return baasClient.makeBaasHttpRequest(baasBaseUrl, addFedMemberRequestUrl, HttpMethod.POST , body, headers, parameters, AddFederationBaasResponse.class);
    }

    public boolean checkIfUserIsInFederationInBaas(String username, String federationId){
        FedInfo fedInfo = (FedInfo)getFederationInfoBaas(federationId).getBody();
        return fedInfo.getMemberIds().contains(username);
    }

    public ResponseEntity<?> registerPlatformToBaas(String username, String platformId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("platform_id ", platformId);
        parameters.add("assoc_user_id ", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, registerPlatformToBC, HttpMethod.PATCH , body, headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> deletePlatformFromBaas(String username, String platformId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("platform_id ", platformId);
        parameters.add("assoc_user_id ", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, deletePlatformFromBC, HttpMethod.PATCH , body, headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> registerResourceToBaas(String username, String platformId, String resourceId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("device_id  ", resourceId);
        parameters.add("platform_id ", platformId);
        parameters.add("associated_user_id  ", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        HashMap<String, List<String>> assosiated_users = new HashMap<String, List<String>>();
        assosiated_users.put(username, new ArrayList<>());
        body = assosiated_users.toString();
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, registerResourceToBC, HttpMethod.PATCH , body, headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> deleteResourceFromBaas(String resourceId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("device_id  ", resourceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, registerResourceToBC, HttpMethod.PATCH , body, headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> getAllFedsFromBaas(){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String body = "";
        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, getAllFedsFromBC, HttpMethod.GET , body, headers, parameters, FedInfo[].class);
        return response;
    }

    public ResponseEntity<?> leaveFedToBaas(String username, String federationId){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("fed_id ", federationId);
        parameters.add("user_id", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, leaveFedFromBC, HttpMethod.DELETE , "", headers, parameters, BaasUser.class);
        return response;
    }

    public ResponseEntity<?> registerFedToBc(FederationWithSmartContract federation, String username){

        RegisterFedToBc registerFedToBc = new RegisterFedToBc(federation.getId(), username, new ArrayList<>(),federation.getSmartContract().toString(), federation.getSmartContract());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<?>  response = baasClient.makeBaasHttpRequest(baasBaseUrl, registerFedToBcUrl, HttpMethod.POST , registerFedToBc.toString(), headers, null, BaasUser.class);
        return response;
    }
    //Checks
    public void userExistsCheckThrowException(String userName) throws ServiceValidationException {
        Map<String, String> responseBody = new HashMap<>();
        ResponseEntity<?> response = getUserInfoBaasResponse(userName);
        if (!(response.getStatusCode() == HttpStatus.OK)){
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST){
                log.debug("The user " + userName + " does not exist in Baas!");
                responseBody.put("error", "The requested federation does not exist!");
            }
            else {
                log.debug(response.getBody());
                responseBody.put("error", response.getBody().toString());
            }
            throw new ServiceValidationException(null, responseBody);
        }
    }
}
