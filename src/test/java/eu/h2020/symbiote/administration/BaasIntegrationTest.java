package eu.h2020.symbiote.administration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.model.Baas.Federation.FedInfo;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithOrganization;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.Baas.User.BaasUser;
import eu.h2020.symbiote.administration.model.CoreUser;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.UserCreationRequest;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BaasIntegrationTest extends AdministrationBaseTestClass{

    private FederationWithSmartContract federationWithSmartContract;

    private FederationWithOrganization federationWithOrganization;

    private SmartContract smartContract;

//    @Qualifier("objectMapper")
    @Autowired
    ObjectMapper mapper;

    @Before
    public void setup() throws IOException {

        federationWithSmartContract = mapper.readValue(Paths.get("src/test/resources/federationWithSmartContract.json").toFile(), FederationWithSmartContract.class);
        federationWithOrganization = mapper.readValue(Paths.get("src/test/resources/federationWithOrganization.json").toFile(), FederationWithOrganization.class);
        smartContract = mapper.readValue(Paths.get("src/test/resources/smartContract.json").toFile(), SmartContract.class);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void getAllFedsInfoFromBaas() throws IOException {

        ResponseEntity<String> baasResponse = baasService.getAllFedsFromBaas();
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<FedInfo> fedInfoList = mapper.readValue(baasResponse.getBody(), new TypeReference<List<FedInfo>>(){});
        Assertions.assertThat(fedInfoList.size() > 0);

    }

    @Test
    public void getFedInfoFromBaas() throws IOException {

        String federationId = "fed_test";

        ResponseEntity<String> baasResponse = baasService.getFederationInfoBaas(federationId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        FedInfo fedInfo = mapper.readValue(baasResponse.getBody(), FedInfo.class);
        Assertions.assertThat(fedInfo.getId().equals(federationId));
    }

    @Test
    public void registerFedToBaas(){

        String creatorId = "anasiou";

        ResponseEntity baasResponse = baasService.registerFedToBc(federationWithOrganization, creatorId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

//Not done
    @Test
    public void addFedMemberRequestBaas(){

        FederationVoteRequest federationVoteRequest  = new FederationVoteRequest(
                "fdtadd1",
                getTestUser("icom1").getValidUsername(),
                VoteAction.ADD_MEMBER,
                "icom1"
        );

        ResponseEntity<String> baasResponse = baasService.makeAddMemberToFederationBaasRequest(federationVoteRequest);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    //Not done
    @Test
    public void deleteFedMemberRequestBaas(){

        FederationVoteRequest federationVoteRequest  = new FederationVoteRequest(
                "fed1",
                getTestUser("icom1").getValidUsername(),
                VoteAction.ADD_MEMBER,
                "icom1"
        );

        ResponseEntity baasResponse = baasService.makeDeleteMemberOfFederationBaasRequest(federationVoteRequest);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateRulesOfFederationBaasRequestTest(){

        FederationVoteRequest federationVoteRequest  = new FederationVoteRequest(
                "fed1",
                getTestUser("icom1").getValidUsername(),
                smartContract,
                VoteAction.ADD_MEMBER
        );

        ResponseEntity baasResponse = baasService.makeUpdateRulesOfFederationBaasRequest(federationVoteRequest);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void registerUserToBcBaasRequestTest(){

        UserCreationRequest userCreationRequest = new UserCreationRequest(
                "anasiou",
                "test",
                "anasiou@test.com",
                UserRole.USER,
                true,
                true,
                true,
                "ICOM",
                "QA Tester");

        ResponseEntity baasResponse = baasService.registerUserToBcBaasRequest(userCreationRequest);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteUserToBcBaasRequestTest(){
        String userId = "anasiou";

        ResponseEntity baasResponse = baasService.deleteUserToBcBaasRequest(userId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    public void deletePlatformFromBaassRequestTest(){
        String userId = "anasiou";
        String platformId= "test";

        ResponseEntity baasResponse = baasService.deletePlatformFromBaas(userId, platformId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUserInfoBaasResponseTest() throws IOException {

        String username = "anasiou";

        ResponseEntity<String> baasResponse = baasService.getUserInfoBaasResponse(username);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        BaasUser baasUser = mapper.readValue(baasResponse.getBody(), BaasUser.class);
        Assertions.assertThat(baasUser.getId().equals(username));
    }

    @Test
    public void getAllUserInfoBaasResponseTest() throws IOException {


        ResponseEntity<String> baasResponse = baasService.getAllUsersBaasResponse();
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<BaasUser> baasUserList = mapper.readValue(baasResponse.getBody(), new TypeReference<List<BaasUser>>(){});
        Assertions.assertThat(baasUserList.size()>0);
    }

    @Test
    public void registerPlatformToBaasTest(){

        String username = "anasiou";
        String platformId = "test";

        ResponseEntity baasResponse = baasService.registerPlatformToBaas(username, platformId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void registerResourceToBaasTest(){

        String platformId = "test";
        String resourceId = "test_resource2";

        ResponseEntity baasResponse = baasService.registerResourceToBaas(platformId, resourceId);
        Assertions.assertThat(baasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void checkConnectionTest(){

        ResponseEntity<String> connectionResponse = baasService.checkConnection();
        Assertions.assertThat(connectionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    private CoreUser getTestUser(String username) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new CoreUser(username, "testpassword", true, true,
                true, true, grantedAuthorities,
                "mail@mail.gr", UserRole.USER, true, true, true, username, "java-test");

    }
}
