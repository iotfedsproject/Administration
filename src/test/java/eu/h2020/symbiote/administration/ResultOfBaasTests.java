package eu.h2020.symbiote.administration;

import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.AddFederationBaasResponse;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.administration.services.baas.BaasClient;
import eu.h2020.symbiote.administration.services.baas.BaasService;
import eu.h2020.symbiote.administration.services.ownedservices.CheckServiceOwnershipService;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.security.commons.enums.AccountStatus;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.OwnedService;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserDetailsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResultOfBaasTests extends AdministrationBaseTestClass {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private FederationVoteRequestRepository federationVoteRequestRepository;

    private UserDetailsResponse response;

    private VoteRequest voteRequest;

    private ResponseEntity<?> baasResponse;

    private String votingId;

    @Mock
    private CheckServiceOwnershipService checkServiceOwnershipService;
    private ResponseEntity<?> checkOwnershipResponse;

    private OwnedService ownedService;

    @Autowired
    private BaasClient baasClient;

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

        votingId = UUID.randomUUID().toString();
        baasResponse = new ResponseEntity<>(new AddFederationBaasResponse(votingId, Collections.emptyMap()), HttpStatus.OK);

        ownedService = new OwnedService(
                "platform1",
                null,
                null,
                "notAValidUrl",
                null,
                false,
                null,
                null,
                null);

        checkOwnershipResponse = new ResponseEntity<>(ownedService, HttpStatus.OK);

        addTestFeds();
        addTestFederationJoinRequests();
    }

    @After
    public void tearDown() {
        federationRepository.deleteAll();
        federationVoteRequestRepository.deleteAll();
    }

    @Test
    public void HelloWorldTest() {
        assert true;
    }

    @Test
    public void addPlatformToFederationAccepted() throws Exception {

        Set<OwnedService> hash_Set = new HashSet<>();
        hash_Set.add(ownedService);

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
        doReturn(checkOwnershipResponse).when(checkServiceOwnershipService).checkIfUserOwnsService(any(), any(), any());
        doReturn(hash_Set).when(rabbitManager).sendOwnedServiceDetailsRequest(any());
        doNothing().when(rabbitManager).publishFederationUpdate(any());

        mockMvc.perform(post("/administration/federation_vote_request/result")
//                        .header("Content-Type", "application/json")
                .headers(getBasicAuthHeaders())
                .param("votingId", votingId)
                .param("status", String.valueOf(RequestStatus.ACCEPTED)))
                .andExpect(status().isOk());
    }

    @Test
    public void addPlatformToFederationRejected() throws Exception {

        doReturn(response).when(rabbitManager).sendLoginRequest(any());
//        doNothing().when(rabbitManager).publishFederationUpdate(any());

        mockMvc.perform(post("/administration/federation_vote_request/result")
//                        .header("Content-Type", "application/json")
                .headers(getBasicAuthHeaders())
                .param("votingId", votingId)
                .param("status", String.valueOf(RequestStatus.REJECTED)))
                .andExpect(status().isOk())
                .andExpect(content().string("The voting request was rejected"));
    }

    @Test
    public void noCommunicationWithBaas() throws Exception {

        String baseUrl = "https://intracom-telecom.com/not/a/valid/url";
        String getFedInfo = "getFedInfo";

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("fed_id", federationId);

        HashMap<String, String> body = new HashMap<>();

        ResponseEntity responseEntity = baasClient.makeBaasHttpRequest(baseUrl, getFedInfo, HttpMethod.GET, body, parameters);
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getBody());
    }

    private void addTestFeds() {
        HashMap<String, FederationInvitation> invitations = new HashMap<>();
        ArrayList<FederationMember> members = new ArrayList<>();
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
        federationRepository.save(testfed);
    }

    private void addTestFederationJoinRequests() {
        CoreUser coreUser = getTestUser();
        coreUser.setValidPassword("password");
        FederationVoteRequest federationVoteRequest = new FederationVoteRequest("federation1",
                coreUser.getValidUsername(), votingId, RequestStatus.PENDING, VoteAction.ADD_MEMBER, new SmartContract(), new Date(), new Date(), "");
        federationVoteRequestRepository.save(federationVoteRequest);
    }

    private CoreUser getTestUser() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new CoreUser("testuser", "testpassword", true, true,
                true, true, grantedAuthorities,
                "test@mail.com", UserRole.USER, true, true, true, "icom", "intern");
    }

//    private UsernamePasswordAuthenticationToken getTestAuthenticationToken() {
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(getTestUser(),
//                new Credentials("username", "password"));
//        return token;
//    }

    private HttpHeaders getBasicAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("test:test".getBytes()));
        return headers;
    }
}
