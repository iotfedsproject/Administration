package eu.h2020.symbiote.administration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.communication.rabbit.RabbitManager;
import eu.h2020.symbiote.administration.config.CustomAuthenticationProvider;
import eu.h2020.symbiote.administration.exceptions.rabbit.CommunicationException;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.administration.repository.FederationRepository;
import eu.h2020.symbiote.administration.services.authorization.AuthorizationService;
import eu.h2020.symbiote.administration.services.baas.BaasService;
import eu.h2020.symbiote.administration.services.ownedservices.CheckServiceOwnershipService;
import eu.h2020.symbiote.core.cci.*;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.mim.Comparator;
import eu.h2020.symbiote.model.mim.*;
import eu.h2020.symbiote.security.commons.Certificate;
import eu.h2020.symbiote.security.commons.enums.AccountStatus;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.*;
import eu.h2020.symbiote.security.communication.payloads.OwnedService.ServiceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;


/**
 * Abstract class with sample objects, acts as parent to all tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource("/ValidationMessages.properties")
public abstract class AdministrationBaseTestClass {

    private static Log log = LogFactory.getLog(AdministrationBaseTestClass.class);

    @Autowired
    protected FederationRepository federationRepository;

    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected RabbitManager rabbitManager;

    @Autowired
    protected CheckServiceOwnershipService checkServiceOwnershipService;

    @Autowired
    protected BaasService baasService;

    protected ClientHttpRequestFactory originalRequestFactory;

    @Autowired
    @InjectMocks
    protected CustomAuthenticationProvider provider;

    @Value("${aam.deployment.owner.username}")
    protected String AAMOwnerUsername;
    @Value("${aam.deployment.owner.password}")
    protected String AAMOwnerPassword;
    @Value("${paam.deployment.token.validityMillis}")
    protected String paamValidityMillis;
    @Value("${aam.environment.coreInterfaceAddress}")
    protected String coreInterfaceAddress;

    @Value("${javax.validation.constraints.NotNull.message}")
    protected String notNullValidationMessage;

    @Value("${validation.https.url}")
    protected String httpsUrlValidationMessage;

    @Value("${validation.service.id}")
    protected String serviceIdValidationMessage;

    @Value("${validation.user.id}")
    protected String userIdValidationMessage;

    @Value("${verificationToken.expirationTime.hours}")
    protected Integer tokenExpirationTimeInHours;

    // ===== Helper Values & Methods ====

    protected String username = "Test1";
    protected String password = "Test1$";
    String email = "test@email.com";
    protected String clientId1 = "clientId1";

    protected String platform1Id = "test1Plat";
    protected String platformPort = "8102";
    protected String platform1Name = platform1Id + "_name";
    protected String platform1Url = "https://platform.test:" + platformPort + "/paam/" + platform1Id;
    private String platformDescription = "This is a test platform.";

    protected String platform2Id = "test2Plat";
    protected String platform2Url = createPlatformUrl(platform2Id);
    protected String platform2Name = createPlatformName(platform2Id);

    protected String platform3Id = "test3Plat";
    protected String platform3Url = createPlatformUrl(platform3Id);
    protected String platform3Name = createPlatformName(platform3Id);

    protected String platform4Id = "test4Plat";
    protected String platform4Url = createPlatformUrl(platform4Id);
    protected String platform4Name = createPlatformName(platform4Id);

    protected String ssp1Id = "test1SSP";
    protected String ssp1Name = "test1SSPName";
    private String sspDescription = "This is a test ssp.";
    private String sspExternalAddress = "https://www.external.com";
    private String sspSiteLocalAddress = "https://www.local.com";
    protected String ssp2Id = "test2SSP";
    protected String ssp2Name = "test2SSPName";


    protected String informationModelId = "model_id";
    protected String informationModelName = "model_name";
    private String informationModelOwner = username;
    protected String informationModelUri = "http://model-uri.com";
    private RDFFormat informationModelFormat = RDFFormat.JSONLD;
    protected String informationModelRdf = "model_rdf";

    protected String informationModelId2 = "model_id2";
    protected String ontologyMappingId = "ontologyMappingId";
    protected String ontologyMappingName = "ontologyMappingName";
    protected String ontologyMappingDefinition = "ontologyMappingDefinition";

    String resourceId = "resource_id";

    protected String federationId = "federationId";
    String federationIdSinglePlatformId = "federationIdSinglePlatformId";

    protected String componentsKeystorePassword = "comp_pass";
    protected String aamKeystoreName = "keystore";
    protected String aamKeystorePassword = "aampass";
    protected Long tokenValidity = 100L;
    String serviceResponse = "serviceResponse";

    protected String serialize(Object o) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(o);
    }

    private List<GrantedAuthority> sampleUserAuthorities() {

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return grantedAuths;
    }

    private List<GrantedAuthority> sampleAdminAuthorities() {

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return grantedAuths;
    }

    CoreUser sampleCoreUser(UserRole role) {
        return new CoreUser(username, password, true, true,
                true, true, sampleUserAuthorities(), email, role,
                true, true, true, "icom", "intern");
    }

    private CoreUser sampleAdminUser(UserRole role) {
        return new CoreUser(username, password, true, true,
                true, true, sampleUserAuthorities(), email, role,
                true, true, true, "icom", "intern");
    }

    public Authentication sampleUserAuth(UserRole role) {

        return new UsernamePasswordAuthenticationToken(sampleCoreUser(role), password, sampleUserAuthorities());
    }

    protected Authentication sampleAdminAuth(UserRole role) {

        return new UsernamePasswordAuthenticationToken(sampleAdminUser(role), null, sampleAdminAuthorities());
    }

    Platform sampleEmptyPlatform() {


        Platform platform = new Platform();
        platform.setId(platform1Id);

        return platform;
    }

    protected ChangePasswordRequest sampleChangePasswordRequest() {
        return new ChangePasswordRequest(password, "newPassword", "newPassword");
    }

    protected ChangeEmailRequest sampleChangeEmailRequest() {
        return new ChangeEmailRequest("new@email.com", "new@email.com");
    }

    Platform samplePlatform() {
        return samplePlatform(platform1Id);
    }

    private Platform samplePlatform(String platformId) {

        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setInformationModelId(informationModelId);
        interworkingService.setUrl(createPlatformUrl(platformId));

        Platform platform = new Platform();
        platform.setId(platformId);
        platform.setName(createPlatformName(platformId));
        platform.setDescription(Collections.singletonList(platformDescription));
        platform.setInterworkingServices(Collections.singletonList(interworkingService));

        return platform;
    }

    protected PlatformDetails samplePlatformDetails() {

        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setInformationModelId(informationModelId);
        interworkingService.setUrl(platform1Url);

        List<Description> descriptions = new ArrayList<>();
        descriptions.add(new Description(platformDescription));

        PlatformDetails platformDetails = new PlatformDetails();
        platformDetails.setId(platform1Id);
        platformDetails.setInterworkingServices(Collections.singletonList(interworkingService));
        platformDetails.setName(platform1Name);
        platformDetails.setDescription(descriptions);
        platformDetails.setIsEnabler(false);

        return platformDetails;
    }

    SmartSpace sampleSmartSpace() {
        return sampleSmartSpace(ssp1Id);
    }

    private SmartSpace sampleSmartSpace(String sspId) {

        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setInformationModelId(informationModelId);
        interworkingService.setUrl(sspExternalAddress + "/" + sspId);

        SmartSpace smartSpace = new SmartSpace();
        smartSpace.setId(sspId);
        smartSpace.setName(ssp1Name);
        smartSpace.setDescription(Collections.singletonList(sspDescription));
        smartSpace.setInterworkingServices(Collections.singletonList(interworkingService));

        return smartSpace;
    }

    protected SSPDetails sampleSSPDetails() {
        return sampleSSPDetails(ssp1Id);
    }

    protected SSPDetails sampleSSPDetails(String id) {
        return new SSPDetails(
                id,
                ssp1Name,
                new ArrayList<>(Collections.singleton(new Description(sspDescription))),
                sspExternalAddress,
                sspSiteLocalAddress,
                informationModelId,
                true
        );
    }

    protected InformationModel sampleInformationModel() {
        InformationModel model = new InformationModel();
        model.setId(informationModelId);
        model.setUri(informationModelUri);
        model.setOwner(informationModelOwner);
        model.setName(informationModelName);
        model.setRdfFormat(informationModelFormat);
        model.setRdf(informationModelRdf);

        return model;
    }

    protected OntologyMapping sampleOntologyMapping() {
        OntologyMapping mapping = new OntologyMapping();
        mapping.setId(ontologyMappingId);
        mapping.setName(ontologyMappingName);
        mapping.setOwner(username);
        mapping.setSourceModelId(informationModelId);
        mapping.setDestinationModelId(informationModelId);
        mapping.setDefinition(ontologyMappingDefinition);
        return mapping;
    }

    protected PlatformRegistryResponse samplePlatformRegistryResponseSuccess() {

        PlatformRegistryResponse platformResponse = new PlatformRegistryResponse();
        platformResponse.setStatus(200);
        platformResponse.setMessage("Success");
        platformResponse.setBody(samplePlatform());
        return platformResponse;
    }

    protected PlatformRegistryResponse samplePlatformRegistryResponseSuccess(String platformId) {

        PlatformRegistryResponse platformResponse = samplePlatformRegistryResponseSuccess();
        platformResponse.setBody(samplePlatform(platformId));
        return platformResponse;
    }

    protected PlatformRegistryResponse samplePlatformResponseFail() {

        PlatformRegistryResponse platformResponse = new PlatformRegistryResponse();
        platformResponse.setStatus(400);
        platformResponse.setMessage("Fail");
        platformResponse.setBody(null);
        return platformResponse;
    }

    protected SspRegistryResponse sampleSspRegistryResponseSuccess() {

        SspRegistryResponse sspResponse = new SspRegistryResponse();
        sspResponse.setStatus(200);
        sspResponse.setMessage("Success");
        sspResponse.setBody(sampleSmartSpace());
        return sspResponse;
    }

    protected SspRegistryResponse sampleSspRegistryResponseSuccess(String sspId) {

        SspRegistryResponse sspResponse = sampleSspRegistryResponseSuccess();
        sspResponse.getBody().setId(sspId);
        return sspResponse;
    }

    public SspRegistryResponse sampleSspRegistryResponseFail() {

        SspRegistryResponse sspResponse = new SspRegistryResponse();
        sspResponse.setStatus(400);
        sspResponse.setMessage("Fail");
        sspResponse.setBody(null);
        return sspResponse;
    }

    protected PlatformConfigurationMessage samplePlatformConfigurationMessage(PlatformConfigurationMessage.Level level,
                                                                              PlatformConfigurationMessage.DeploymentType deploymentType) {

        return new PlatformConfigurationMessage(platform1Id, username, password, componentsKeystorePassword,
                aamKeystoreName, aamKeystorePassword, "aamPrivateKeyPassword", tokenValidity, true,
                level, deploymentType);
    }

    protected InformationModelListResponse sampleInformationModelListResponseSuccess() {
        InformationModelListResponse response = new InformationModelListResponse();
        List<InformationModel> modelList = new ArrayList<>();
        InformationModel model = sampleInformationModel();
        modelList.add(model);
        response.setBody(modelList);
        response.setStatus(200);
        return response;
    }

    protected InformationModelListResponse sampleInformationModelListResponseFail() {
        InformationModelListResponse response = new InformationModelListResponse();
        response.setBody(null);
        response.setMessage("Fail");
        response.setStatus(400);
        return response;
    }

    InformationModelRequest sampleInformationModelRequest() {
        InformationModelRequest request = new InformationModelRequest();
        request.setBody(sampleInformationModel());
        return request;
    }

    protected InformationModelResponse sampleInformationModelResponseSuccess() {
        InformationModelResponse response = new InformationModelResponse();
        response.setStatus(200);
        response.setBody(sampleInformationModel());
        return response;
    }

    protected InformationModelResponse sampleInformationModelResponseFail() {
        InformationModelResponse response = new InformationModelResponse();
        response.setBody(null);
        response.setMessage("Fail");
        response.setStatus(400);
        return response;
    }

    protected InfoModelMappingResponse sampleMappingResponseSuccess() {
        InfoModelMappingResponse response = new InfoModelMappingResponse();
        response.setStatus(200);
        response.setBody(sampleOntologyMapping());
        return response;
    }

    protected InfoModelMappingResponse sampleMappingResponseFail() {
        InfoModelMappingResponse response = new InfoModelMappingResponse();
        response.setBody(null);
        response.setMessage("Fail");
        response.setStatus(400);
        return response;
    }

    protected MappingListResponse sampleMappingListResponseSuccess() {
        MappingListResponse response = new MappingListResponse();
        response.setStatus(200);
        response.setBody(new HashSet<>(Collections.singletonList(sampleOntologyMapping())));
        return response;
    }

    protected MappingListResponse sampleMappingListResponseFail() {
        MappingListResponse response = new MappingListResponse();
        response.setBody(null);
        response.setMessage("Fail");
        response.setStatus(400);
        return response;
    }

    CoreResourceRegistryRequest sampleCoreResourceRegistryRequest() {
        return new CoreResourceRegistryRequest();

    }

    ResourceListResponse sampleResourceListResponseSuccess() {
        Resource resource = new Resource();
        resource.setId(resourceId);
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(resource);

        return new ResourceListResponse(200, "Success!", resourceList);
    }

    public ResourceListResponse sampleResourceListResponseFail() {
        return new ResourceListResponse(400, "Fail!", null);
    }

    Credentials sampleCredentials() {

        CoreUser user = sampleCoreUser(UserRole.SERVICE_OWNER);
        return new Credentials(user.getValidUsername(), user.getValidPassword());
    }

    UserManagementRequest sampleUserManagementRequest(UserRole role) {

        return new UserManagementRequest(
                new Credentials(AAMOwnerUsername, AAMOwnerPassword),
                new Credentials(username, password),
                new UserDetails(
                        new Credentials(username, password),
                        email,
                        role,
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        true,
                        true
                ),
                OperationType.CREATE
        );
    }

    protected UserDetailsResponse sampleActiveUserDetailsResponse(HttpStatus status) {
        return sampleUserDetailsResponse(status, AccountStatus.ACTIVE);
    }

    protected UserDetailsResponse sampleUserDetailsResponse(HttpStatus status, AccountStatus accountStatus) {
        Map<String, Certificate> clients = new HashMap<>();
        try {
            clients.put(clientId1, new Certificate("certificate1String"));
            clients.put("clientId2", new Certificate("certificate2String"));
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return new UserDetailsResponse(status, new UserDetails(
                new Credentials(username, password),
                email,
                UserRole.SERVICE_OWNER,
                accountStatus,
                new HashMap<>(),
                clients,
                true,
                true
        ));
    }

    PlatformManagementRequest samplePlatformManagementRequest(OperationType operationType) {

        return new PlatformManagementRequest(
                new Credentials(AAMOwnerUsername, AAMOwnerPassword),
                new Credentials(username, password),
                platform1Url,
                platform1Name,
                operationType
        );
    }

    protected PlatformManagementResponse samplePlatformManagementResponse(ManagementStatus status) {

        return new PlatformManagementResponse(
                platform1Id,
                status
        );
    }

    SmartSpaceManagementRequest sampleSmartSpaceManagementRequest(OperationType operationType) {

        try {
            return new SmartSpaceManagementRequest(
                    new Credentials(AAMOwnerUsername, AAMOwnerPassword),
                    new Credentials(username, password),
                    sspExternalAddress,
                    sspSiteLocalAddress,
                    ssp1Name,
                    operationType,
                    ssp1Id,
                    true
            );
        } catch (InvalidArgumentsException e) {
            log.warn(e);
        }
        return null;
    }

    protected SmartSpaceManagementResponse sampleSmartSpaceManagementResponse(ManagementStatus status) {

        return new SmartSpaceManagementResponse(
                ssp1Id,
                status
        );
    }

    protected Set<OwnedService> sampleOwnedServiceDetails() {

        Map<String, Certificate> componentCertificates = new HashMap<>();
        Set<OwnedService> ownedServiceSet = new HashSet<>();

        // Platforms
        ownedServiceSet.add(new OwnedService(
                platform1Id, platform1Name, ServiceType.PLATFORM, platform1Url, null,
                false, null, new Certificate(), componentCertificates));
        ownedServiceSet.add(new OwnedService(
                platform2Id, platform2Name, ServiceType.PLATFORM, platform2Url, null,
                false, null, new Certificate(), componentCertificates));
        ownedServiceSet.add(new OwnedService(
                platform3Id, platform3Name, ServiceType.PLATFORM, platform3Url, null,
                false, null, new Certificate(), componentCertificates));
        ownedServiceSet.add(new OwnedService(
                platform4Id, platform4Name, ServiceType.PLATFORM, platform4Url, null,
                false, null, new Certificate(), componentCertificates));

        // SSPs
        ownedServiceSet.add(new OwnedService(
                ssp1Id, ssp1Name, ServiceType.SMART_SPACE, null, sspExternalAddress,
                false, sspSiteLocalAddress, new Certificate(), componentCertificates));

        ownedServiceSet.add(new OwnedService(
                ssp2Id, ssp2Name, ServiceType.SMART_SPACE, null, sspExternalAddress,
                true, sspSiteLocalAddress, new Certificate(), componentCertificates));

        return ownedServiceSet;
    }

    protected FederationWithSmartContract sampleFederationRequest() throws IOException, ParseException {

        // Create a smart contract
        JSONParser parser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();

        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("src/test/resources/smartContract.json"));
        SmartContract smartContract = mapper.convertValue(jsonObject, SmartContract.class);

//        Federation federation = new Federation();
        FederationWithSmartContract federation = new FederationWithSmartContract();
        federation.setId(federationId);
        federation.setInformationModel(sampleInformationModel());
        federation.setName("federationName");
        federation.setPublic(true);
        federation.setSmartContract(smartContract);

        QoSConstraint qosConstraint1 = new QoSConstraint();
        qosConstraint1.setMetric(QoSMetric.availability);
        qosConstraint1.setComparator(Comparator.equal);
        qosConstraint1.setThreshold(1.2);
        QoSConstraint qosConstraint2 = new QoSConstraint();
        qosConstraint2.setMetric(QoSMetric.load);
        qosConstraint2.setComparator(Comparator.greaterThan);
        qosConstraint2.setThreshold(1.2);
        federation.setSlaConstraints(new ArrayList<>(Arrays.asList(qosConstraint1, qosConstraint2)));

        List<QoSConstraint> qosConstraints = new ArrayList<>(Arrays.asList(qosConstraint1, qosConstraint2));
        federation.setSlaConstraints(qosConstraints);
        FederationMember member1 = new FederationMember(platform1Id, null);
        FederationMember member2 = new FederationMember(platform2Id, null);
        FederationMember member3 = new FederationMember(platform3Id, null);
        federation.setMembers(new ArrayList<>(Arrays.asList(member1, member2, member3)));

        return federation;
    }

    protected FederationWithInvitations sampleSavedFederation() throws IOException, ParseException {
        FederationWithSmartContract federation = sampleFederationRequest();

        federation.getMembers().get(0).setInterworkingServiceURL(platform1Url);
        federation.getMembers().get(1).setInterworkingServiceURL(platform2Url);
        federation.getMembers().get(2).setInterworkingServiceURL(platform3Url);

        Map<String,FederationInvitation> federationInvitationMap = new HashMap<>();
        federationInvitationMap.put("test", new FederationInvitation("platform-invited", FederationInvitation.InvitationStatus.PENDING,new Date()));

        return new FederationWithInvitations(
                federation.getId(),
                new Date(),
                federation.getName(),
                federation.isPublic(),
                federation.getInformationModel(),
                federation.getSlaConstraints(),
                new SmartContract(),
                federation.getMembers(),
                federationInvitationMap);
    }

    protected FederationWithInvitations sampleSavedFederationWithSinglePlatform() throws IOException, ParseException {
        FederationWithSmartContract federation = sampleFederationRequest();

        federation.getMembers().get(0).setInterworkingServiceURL(platform1Url);
        federation.getMembers().remove(2);
        federation.getMembers().remove(1);

        return new FederationWithInvitations(
                federationIdSinglePlatformId,
                new Date(),
                federation.getName(),
                federation.isPublic(),
                federation.getInformationModel(),
                federation.getSlaConstraints(),
                new SmartContract(),
                federation.getMembers(),
                new HashMap<>());
    }

    ErrorResponseContainer sampleErrorResponse() {

        return new ErrorResponseContainer("SAMPLE_ERROR", 400);
    }

    protected CommunicationException sampleCommunicationException() {

        return new CommunicationException("SAMPLE_ERROR");
    }

    private String createPlatformUrl(String platformId) {
        return "https://platform.test:" + platformPort + "/paam/" + platformId;
    }

    private String createPlatformName(String platformId) {
        return platformId + "_name";
    }
}
