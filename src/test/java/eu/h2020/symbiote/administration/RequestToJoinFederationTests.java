package eu.h2020.symbiote.administration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.AddFederationBaasResponse;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.security.commons.enums.AccountStatus;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserDetailsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RequestToJoinFederationTests extends AdministrationBaseTestClass {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FederationVoteRequestRepository federationVoteRequestRepository;

    @Autowired
    FederationVoteRequestService federationVoteRequestService;

    private UserDetailsResponse response;

    private VoteRequest voteRequest;

    private ResponseEntity<?> baasResponse;

    @Before
    public void setup() {


        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilters(springSecurityFilterChain)
                .build();

        SecurityMockMvcConfigurers.springSecurity();

        MockitoAnnotations.initMocks(this);

        UserDetails details = new UserDetails(new Credentials("test", "test"), "test@mail.com", UserRole.USER, AccountStatus.ACTIVE, null, null, true, true);
        response = new UserDetailsResponse(HttpStatus.OK, details);
        voteRequest = new VoteRequest("federation1", "testplatform");

        String votingId = UUID.randomUUID().toString();
        baasResponse = new ResponseEntity<>(new AddFederationBaasResponse(votingId, new ArrayList<>()), HttpStatus.OK);

        addTestFeds();
        addTestFederationJoinRequests("federation1", new Date());
    }

    @After
    public void tearDown() {
        federationRepository.deleteAll();
        federationVoteRequestRepository.deleteAll();
    }

    @Test
    public void postFederationDoesntExist() throws Exception {

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeAddMemberToFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/joinFederation")
                .headers(getBasicAuthHeaders())
                .param("federationId", "not_a_federation")
                .content(""))
                .andExpect(status().isBadRequest())
//                .andExpect(content().string("The requested federation does not exist!"))
                .andExpect(content().json("{\"validationErrors\":{\"error\":\"The requested federation does not exist!\"}}"));
//                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getVoteRequestsOk() throws Exception {

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeAddMemberToFederationBaasRequest(any());

        mockMvc.perform(get("/administration/federation_vote_request/allForUser")
                .headers(getBasicAuthHeaders())
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void postRequestToJoinOk() throws Exception {

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeAddMemberToFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/joinFederation?federationId=federation1")
                        .headers(getBasicAuthHeaders())
                        .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void postRequestToJoinRequestExists() throws Exception {
        UserDetails details = new UserDetails(new Credentials("testuser", "testpassword"), "test@mail.com", UserRole.USER, AccountStatus.ACTIVE, null, null, true, true);
        UserDetailsResponse response = new UserDetailsResponse(HttpStatus.OK, details);

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeAddMemberToFederationBaasRequest(any());


        mockMvc.perform(post("/administration/federation_vote_request/joinFederation?federationId=federation1")
                        .headers(getBasicAuthHeadersExistingVoteRequest())
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"validationErrors\":{\"error\":\"Such a request exists already!\"}}"));
    }

    @Test
    public void postRequestToDeleteMemberOk() throws Exception{
        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeDeleteMemberOfFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/deletePlatform")
                .headers(getBasicAuthHeaders())
                .param("federationId", "federation1")
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void postRequestToDeleteMemberThatDoesntExist() throws Exception {

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeDeleteMemberOfFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/deletePlatform")
                .headers(getBasicAuthHeaders())
                .param("federationId", "federation1")
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Platform platform1 is not a member of this federation federation1"));
    }

    @Test
    public void deleteFederation() throws Exception{

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeDeleteFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/deleteFederation")
                    .headers(getBasicAuthHeaders())
                    .param("federationId", "federation1"))
                .andExpect(status().isOk());

    }

    @Test
    public void deleteFederationDoesntExist() throws Exception{

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(baasResponse).when(baasService).makeDeleteFederationBaasRequest(any());

        mockMvc.perform(post("/administration/federation_vote_request/deleteFederation")
                        .headers(getBasicAuthHeaders())
                        .param("federationId", "fed"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The requested federation does not exist!"));

    }

    @Test
    public void sout() throws Exception{

        System.out.println("Test");

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private void addTestFeds() {
        HashMap<String, FederationInvitation> invitations = new HashMap<>();
        ArrayList<FederationMember> members = new ArrayList<>();
        members.add(new FederationMember("alreadyMember","notAValidUrl.com"));

        FederationWithInvitations testfed = new FederationWithInvitations(
                "federation1",
                new Date(),
                "federation1",
                true,
                null,
                null,
                new SmartContract(),
                members,
                invitations
        );
        System.out.println(testfed);
        federationRepository.save(testfed);
    }

    private void addTestFederationJoinRequests(String federationId, Date handledDate) {
        CoreUser coreUser = getTestUser();
        coreUser.setValidPassword("password");
        FederationVoteRequest federationVoteRequest = new FederationVoteRequest(federationId,
                coreUser, "", RequestStatus.PENDING, VoteAction.ADD_MEMBER, new SmartContract(), new Date(), handledDate);
        federationVoteRequestRepository.save(federationVoteRequest);
    }

    private CoreUser getTestUser() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new CoreUser("testUser", "testUser", true, true,
                true, true, grantedAuthorities,
                "test@mail.com", UserRole.USER, true, true, true);
    }

//    private UsernamePasswordAuthenticationToken getTestAuthenticationToken() {
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(getTestUser(),
//                new Credentials("username", "password"));
//        return token;
//    }

    private HttpHeaders getBasicAuthHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("testUser:testPassword".getBytes()));
        return headers;
    }
    private HttpHeaders getBasicAuthHeadersExistingVoteRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("testUser:testPassword".getBytes()));
        return headers;
    }
}
