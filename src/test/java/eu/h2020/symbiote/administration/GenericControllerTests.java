package eu.h2020.symbiote.administration;

import eu.h2020.symbiote.administration.helpers.AuthorizationServiceHelper;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.model.CoreUser;
import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.FederationWithInvitations;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import eu.h2020.symbiote.administration.repository.FederationVoteRequestRepository;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for use in testing MVC and form validation.
 */
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GenericControllerTests extends AdministrationBaseTestClass {

    @Value("${symbiote.core.administration.serverInformation.name}")
    private String serverInfoName;

    @Value("${symbiote.core.administration.serverInformation.dataProtectionOrganization}")
    private String dataProtectionOrganization;

    @Value("${symbiote.core.administration.serverInformation.address}")
    private String address;

    @Value("${symbiote.core.administration.serverInformation.country}")
    private String country;

    @Value("${symbiote.core.administration.serverInformation.phoneNumber}")
    private String phoneNumber;

    @Value("${symbiote.core.administration.serverInformation.email}")
    private String email;

    @Value("${symbiote.core.administration.serverInformation.website}")
    private String website;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FederationVoteRequestRepository federationVoteRequestRepository;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        String currentDirectory = System.getProperty("user.dir");

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilters(springSecurityFilterChain)
                .build();

        MockitoAnnotations.initMocks(this);
        federationRepository.deleteAll();
    }

    @Test
    public void joinedFederationsGenerateServiceResponseError() throws Exception {
        String error = "error";
        when(authorizationService.generateServiceResponse()).thenReturn(
                new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/administration/generic/joinedFederations")
                .param("platformId", platform1Id))
                .andExpect(status().isInternalServerError())
                .andExpect(header().doesNotExist(SecurityConstants.SECURITY_RESPONSE_HEADER))
                .andExpect(content().string(error));

        verify(authorizationService, times(1)).generateServiceResponse();
    }

    @Test
    public void joinedFederationsUnAuthorized() throws Exception {
        String error = "The stored resource access policy was not satisfied";

        when(authorizationService.generateServiceResponse()).thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));
        when(authorizationService.checkJoinedFederationsRequest(any(), any(), eq(serviceResponse)))
                .thenReturn(AuthorizationServiceHelper.addSecurityService(
                        error, new HttpHeaders(), HttpStatus.UNAUTHORIZED, serviceResponse));

        mockMvc.perform(post("/administration/generic/joinedFederations")
                .param("platformId", platform1Id))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(SecurityConstants.SECURITY_RESPONSE_HEADER, serviceResponse));

        verify(authorizationService, times(1)).generateServiceResponse();
        verify(authorizationService, times(1)).checkJoinedFederationsRequest(any(), any(), eq(serviceResponse));

    }

    @Test
    public void joined1FederationSuccess() throws Exception {

        storeFederations();

        when(authorizationService.generateServiceResponse()).thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));
        when(authorizationService.checkJoinedFederationsRequest(eq(platform2Id), any(), eq(serviceResponse)))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        mockMvc.perform(post("/administration/generic/joinedFederations")
                .param("platformId", platform2Id))
                .andExpect(status().isOk())
                .andExpect(header().string(SecurityConstants.SECURITY_RESPONSE_HEADER, serviceResponse))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", contains(federationId)));

        verify(authorizationService, times(1)).generateServiceResponse();
        verify(authorizationService, times(1)).checkJoinedFederationsRequest(eq(platform2Id), any(), eq(serviceResponse));
    }

    @Test
    public void joined2FederationsSuccess() throws Exception {
        storeFederations();

        when(authorizationService.generateServiceResponse()).thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));
//        when(authorizationService.checkJoinedFederationsRequest(eq(platform1Id), any(), eq(serviceResponse)))
//                .thenReturn(new ResponseEntity(HttpStatus.OK));

        mockMvc.perform(post("/administration/generic/joinedFederations")
                .param("platformId", platform1Id))
                .andExpect(status().isOk())
                .andExpect(header().string(SecurityConstants.SECURITY_RESPONSE_HEADER, serviceResponse))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(federationIdSinglePlatformId, federationId)));

        verify(authorizationService, times(1)).generateServiceResponse();
        verify(authorizationService, times(1)).checkJoinedFederationsRequest(eq(platform1Id), any(), eq(serviceResponse));
    }

    @Test
    public void joined0FederationsSuccess() throws Exception {
        storeFederations();
        String dummyPlatformId = "dummyPlatformId";

        when(authorizationService.generateServiceResponse()).thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));
        when(authorizationService.checkJoinedFederationsRequest(eq(dummyPlatformId), any(), eq(serviceResponse)))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        mockMvc.perform(post("/administration/generic/joinedFederations")
                .param("platformId", dummyPlatformId))
                .andExpect(status().isOk())
                .andExpect(header().string(SecurityConstants.SECURITY_RESPONSE_HEADER, serviceResponse))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorizationService, times(1)).generateServiceResponse();
        verify(authorizationService, times(1)).checkJoinedFederationsRequest(eq(dummyPlatformId), any(), eq(serviceResponse));
    }

    @Test
    public void information() throws Exception {
        mockMvc.perform(get("/administration/generic/information"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(serverInfoName))
                .andExpect(jsonPath("$.dataProtectionOrganization").value(dataProtectionOrganization))
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.country").value(country))
                .andExpect(jsonPath("$.phoneNumber").value(phoneNumber))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.website").value(website));
    }

    @Test
    public void voteResults() throws Exception {
        storeVoteRequests();
//        String dummyPlatformId = "dummyPlatformId";

//        when(authorizationService.generateServiceResponse()).thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));
//        when(authorizationService.checkJoinedFederationsRequest(eq(dummyPlatformId), any(), eq(serviceResponse)))
//                .thenReturn(new ResponseEntity(HttpStatus.OK));

        mockMvc.perform(post("/administration/generic/result")
                .param("votingId", "dummy1")
                .param("status", RequestStatus.ACCEPTED.name()))
                .andExpect(status().isOk());

//        verify(authorizationService, times(1)).generateServiceResponse();
//        verify(authorizationService, times(1)).checkJoinedFederationsRequest(eq(dummyPlatformId), any(), eq(serviceResponse));
    }

    private void storeFederations() throws IOException, ParseException {
        FederationWithInvitations federationWithInvitations1 = sampleSavedFederationWithSinglePlatform();
        FederationWithInvitations federationWithInvitations2 = sampleSavedFederation();

        federationRepository.save(new ArrayList<>(Arrays.asList(federationWithInvitations1, federationWithInvitations2)));
    }

    private void storeVoteRequests() {

        FederationVoteRequest federationVoteRequestAdd = new FederationVoteRequest(
                "test-federation-add",
                "dummyUser",
                "dummy1",
                RequestStatus.PENDING,
                VoteAction.ADD_MEMBER,
                new SmartContract(),
                new Date(),
                null,
                "tolis"
        );
        FederationVoteRequest federationVoteRequestRemove = new FederationVoteRequest(
                "test-federation-remove",
                "dummyUser",
                "dummy2",
                RequestStatus.PENDING,
                VoteAction.ADD_MEMBER,
                new SmartContract(),
                new Date(),
                null,
                "tolis"
        );
        FederationVoteRequest federationVoteRequestUpdate = new FederationVoteRequest(
                "test-federation-update",
                "dummyUser",
                "dummy3",
                RequestStatus.PENDING,
                VoteAction.ADD_MEMBER,
                new SmartContract(),
                new Date(),
                null,
                "tolis"
        );
        FederationVoteRequest federationVoteRequestDelete = new FederationVoteRequest(
                "test-federation-delete",
                "dummyUser",
                "dummy4",
                RequestStatus.PENDING,
                VoteAction.ADD_MEMBER,
                new SmartContract(),
                new Date(),
                null,
                "tolis"
        );

        federationVoteRequestRepository.save(federationVoteRequestAdd);
        federationVoteRequestRepository.save(federationVoteRequestRemove);
        federationVoteRequestRepository.save(federationVoteRequestUpdate);
        federationVoteRequestRepository.save(federationVoteRequestDelete);

    }

}