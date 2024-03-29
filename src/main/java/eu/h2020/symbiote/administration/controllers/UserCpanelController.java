package eu.h2020.symbiote.administration.controllers;

import eu.h2020.symbiote.administration.exceptions.generic.GenericHttpErrorException;
import eu.h2020.symbiote.administration.exceptions.rabbit.CommunicationException;
import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithOrganization;
import eu.h2020.symbiote.administration.model.Baas.Federation.FederationWithSmartContract;
import eu.h2020.symbiote.administration.services.federationVoteRequest.FederationVoteRequestService;
import eu.h2020.symbiote.administration.services.federation.FederationService;
import eu.h2020.symbiote.administration.services.infomodel.InformationModelService;
import eu.h2020.symbiote.administration.services.ownedservices.OwnedServicesService;
import eu.h2020.symbiote.administration.services.platform.PlatformService;
import eu.h2020.symbiote.administration.services.ssp.SSPService;
import eu.h2020.symbiote.administration.services.user.UserService;
import eu.h2020.symbiote.core.internal.GetAllMappings;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.OntologyMapping;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Set;

/**
 * Spring controller for the User control panel, handles management views and form validation.
 *
 * @author Vasileios Glykantzis (ICOM)
 * @author Tilemachos Pechlivanoglou (ICOM)
 */
@Controller
@RequestMapping("/administration/user")
@CrossOrigin
public class UserCpanelController {
    private static Log log = LogFactory.getLog(UserCpanelController.class);

    private UserService userService;
    private OwnedServicesService ownedServicesService;
    private PlatformService platformService;
    private SSPService sspService;
    private InformationModelService informationModelService;
    private FederationService federationService;
    private FederationVoteRequestService federationVoteRequestService;

    @Autowired
    public UserCpanelController(UserService userService,
                                OwnedServicesService ownedServicesService,
                                PlatformService platformService,
                                SSPService sspService,
                                InformationModelService informationModelService,
                                FederationService federationService, FederationVoteRequestService federationVoteRequestService) {

        Assert.notNull(userService, "federationVoteRequestService can not be null!");
        this.federationVoteRequestService = federationVoteRequestService;

        Assert.notNull(userService, "UserService can not be null!");
        this.userService = userService;

        Assert.notNull(ownedServicesService, "OwnedServicesService can not be null!");
        this.ownedServicesService = ownedServicesService;

        Assert.notNull(platformService, "PlatformService can not be null!");
        this.platformService = platformService;

        Assert.notNull(sspService, "SSPService can not be null!");
        this.sspService = sspService;

        Assert.notNull(informationModelService, "InformationModelService can not be null!");
        this.informationModelService = informationModelService;

        Assert.notNull(federationService, "FederationService can not be null!");
        this.federationService = federationService;
    }


    /**
     * Gets the default view. If the user is a platform owner, tries to fetch their details.
     * Registry is first polled and, if the platform isn't activated there, AAM is polled for them.
     */
    @GetMapping("/cpanel")
    public String userCPanel() {

        log.debug("GET request on /cpanel");
        return "index";
    }

    @GetMapping("/information")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserDetailsDTO getUserInformation(Principal principal)
            throws GenericHttpErrorException, CommunicationException {
        log.debug("GET request on /information");

        return new UserDetailsDTO(userService.getUserInformationWithLogin(principal));
    }

    @PostMapping("/change_email")
    @ResponseStatus(HttpStatus.OK)
    public void changeEmail(@Valid @RequestBody ChangeEmailRequest message,
                            BindingResult bindingResult,
                            Principal principal)
            throws GenericHttpErrorException {
        log.debug("POST request on /change_email");
        userService.changeEmail(message, bindingResult, principal);
    }

    @PostMapping("/change_permissions")
    @ResponseStatus(HttpStatus.OK)
    public void changePermissions(@Valid @RequestBody ChangePermissions message,
                                  BindingResult bindingResult,
                                  Principal principal)
            throws GenericHttpErrorException {
        log.debug("POST request on /change_email");

        userService.changePermissions(message, bindingResult, principal);
    }

    @PostMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest message,
                               BindingResult bindingResult,
                               Principal principal)
            throws GenericHttpErrorException {
        log.debug("POST request on /change_password");
        userService.changePassword(message, bindingResult, principal);
    }

    @PostMapping("/accept_terms")
    @ResponseStatus(HttpStatus.OK)
    public void changePermissions(Principal principal)
            throws GenericHttpErrorException {
        log.debug("POST request on /accept_terms");

        userService.acceptTerms(principal);
    }

    @PostMapping("/delete_user")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(Principal principal)
            throws GenericHttpErrorException {
        log.debug("POST request on /delete_user");
        userService.deleteUser(principal);
    }

    @PostMapping("/cpanel/delete_client")
    @ResponseStatus(HttpStatus.OK)
    public void deleteClient(@RequestParam String clientIdToDelete, Principal principal)
            throws GenericHttpErrorException {

        log.debug("POST request on /cpanel/delete_client for client with id: " +
                clientIdToDelete);

        userService.deleteClient(clientIdToDelete, principal);
    }

    @PostMapping("/cpanel/list_user_services")
    public ResponseEntity<ListUserServicesResponse> listUserPlatforms(Principal principal) {

        log.debug("POST request on /cpanel/list_user_services");
        return ownedServicesService.listUserServices(principal);
    }

    @PostMapping("/cpanel/register_platform")
    public ResponseEntity<?> registerPlatform(@Valid @RequestBody PlatformDetails platformDetails,
                                              BindingResult bindingResult, Principal principal) {

        log.debug("POST request on /cpanel/register_platform");

        return platformService.registerPlatform(platformDetails, bindingResult, principal);
    }


    @PostMapping("/cpanel/update_platform")
    public ResponseEntity<?> updatePlatform(@Valid @RequestBody PlatformDetails platformDetails,
                                            BindingResult bindingResult, Principal principal) {

        log.debug("POST request on /cpanel/update_platform");
        return platformService.updatePlatform(platformDetails, bindingResult, principal);

    }

    @PostMapping("/cpanel/delete_platform")
    public ResponseEntity<?> deletePlatform(@RequestParam String platformIdToDelete, Principal principal) {

        log.debug("POST request on /cpanel/delete_platform for platform with id: " +
                platformIdToDelete);
        return platformService.deletePlatform(platformIdToDelete, principal);
    }


    @PostMapping("/cpanel/get_platform_config")
    public void getPlatformConfig(@Valid @RequestBody PlatformConfigurationMessage configurationMessage,
                                  BindingResult bindingResult, Principal principal,
                                  HttpServletResponse response) throws Exception {

        log.debug("POST request on /cpanel/get_platform_config: " + configurationMessage);
        platformService.getPlatformConfig(configurationMessage, bindingResult, principal, response);

    }

    @PostMapping("/cpanel/register_ssp")
    public ResponseEntity<?> registerSSP(@Valid @RequestBody SSPDetails sspDetails,
                                         BindingResult bindingResult, Principal principal) {

        log.debug("POST request on /cpanel/register_ssp");

        return sspService.registerSSP(sspDetails, bindingResult, principal);
    }

    @PostMapping("/cpanel/update_ssp")
    public ResponseEntity<?> updateSSP(@Valid @RequestBody SSPDetails sspDetails,
                                       BindingResult bindingResult, Principal principal) {

        log.debug("POST request on /cpanel/update_ssp");

        return sspService.updateSSP(sspDetails, bindingResult, principal);
    }

    @PostMapping("/cpanel/delete_ssp")
    public ResponseEntity<?> deleteSSP(@RequestParam String sspIdToDelete, Principal principal) {

        log.debug("POST request on /cpanel/delete_ssp for ssp with id: " +
                sspIdToDelete);
        return sspService.deleteSSP(sspIdToDelete, principal);
    }


    @PostMapping("/cpanel/list_all_info_models")
    public ResponseEntity<?> listAllInformationModels() {

        log.debug("POST request on /cpanel/list_all_info_models");

        // Get InformationModelList from Registry
        return informationModelService.getInformationModels();
    }

    @PostMapping("/cpanel/list_user_info_models")
    public ResponseEntity<?> listUserInformationModels(Principal principal) {

        log.debug("POST request on /cpanel/list_user_info_models");

        return informationModelService.listUserInformationModels(principal);
    }

    @PostMapping("/cpanel/register_information_model")
    public ResponseEntity<?> registerInformationModel(@RequestParam("info-model-name") String name,
                                                      @RequestParam("info-model-uri") String uri,
                                                      @RequestParam("info-model-rdf") MultipartFile rdfFile,
                                                      Principal principal) {

        log.debug("POST request on /cpanel/register_information_model");
        return informationModelService.registerInformationModel(name, uri, rdfFile, principal);
    }


    @PostMapping("/cpanel/delete_information_model")
    public ResponseEntity<?> deleteInformationModel(@RequestParam String infoModelIdToDelete,
                                                    Principal principal) {

        log.debug("POST request on /cpanel/delete_information_model for info model with id = " + infoModelIdToDelete);
        return informationModelService.deleteInformationModel(infoModelIdToDelete, principal);
    }

    @PostMapping("/cpanel/list_all_mappings")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Set<OntologyMapping> listAllMappings() throws GenericHttpErrorException {

        log.debug("POST request on /cpanel/list_all_mappings");

        // Get InformationModelList from Registry
        return informationModelService.getAllMappings(new GetAllMappings(false));
    }

    @PostMapping("/cpanel/get_mapping_definition")
    public void getMappingDefinition(@RequestParam("mappingId") String mappingId,
                                     HttpServletResponse response)
            throws GenericHttpErrorException {

        log.debug("POST request on /cpanel/get_mapping_definition");

        // Get Mapping Definition from Registry
        informationModelService.getMappingDefinition(mappingId, response);
    }

    @PostMapping("/cpanel/register_info_model_mapping")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public OntologyMapping registerMapping(@RequestParam("name") String name,
                                           @RequestParam("sourceModelId") String sourceModelId,
                                           @RequestParam("destinationModelId") String destinationModelId,
                                           @RequestParam("definition") MultipartFile definition,
                                           Principal principal)
            throws GenericHttpErrorException {

        log.debug("POST request on /cpanel/register_info_model_mapping");
        return informationModelService.registerInfoModelMapping(name, sourceModelId, destinationModelId, definition, principal);
    }

    @PostMapping("/cpanel/delete_info_model_mapping")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMapping(@RequestParam String mappingIdToDelete, Principal principal)
            throws GenericHttpErrorException {

        log.debug("POST request on /cpanel/delete_info_model_mapping");
        informationModelService.deleteInfoModelMapping(mappingIdToDelete, principal);
    }

    @PostMapping("/cpanel/list_federations")
    public ResponseEntity<?> listFederations() {

        log.debug("POST request on /cpanel/list_federations");
        return federationService.listFederations();
    }

    @PostMapping("/cpanel/create_federation")
//    public ResponseEntity<?> createFederation(@Valid @RequestBody Federation federation,
//                                              BindingResult bindingResult, Principal principal) {
//    public ResponseEntity<?> createFederation(@Valid @RequestBody FederationWithSmartContract federation,
//                                              BindingResult bindingResult, Principal principal) {
    public ResponseEntity<?> createFederation(@Valid @RequestBody FederationWithOrganization federation,
                                              BindingResult bindingResult, Principal principal) throws ServiceValidationException {

        log.debug("POST request on /cpanel/create_federation with RequestBody: "
                + ReflectionToStringBuilder.toString(federation));
        return federationService.createFederation(federation, bindingResult, principal);
    }

    @PostMapping("/cpanel/delete_federation")
    public ResponseEntity<?> deleteFederation(@RequestParam String federationIdToDelete, Principal principal) {

        log.debug("POST request on /cpanel/delete_federation for federation with id = " + federationIdToDelete);
        return federationService.deleteFederation(federationIdToDelete, false, principal);
    }

    @PostMapping("/cpanel/leave_federation")
    public ResponseEntity<?> createFederation(@RequestParam String federationId, @RequestParam String platformId,
                                              Principal principal) {
//    public ResponseEntity<?> createFederation(@RequestParam String federationId, @RequestParam String organization,
//                                              Principal principal) {

        log.debug("POST request on /cpanel/leave_federation for federationId = "
                + federationId + " organization = " + platformId);
        return federationService.leaveFederation(federationId, platformId, principal, false);
    }

    @PostMapping("/cpanel/federation_invite")
    public ResponseEntity<?> inviteToFederation(@Valid @RequestBody InvitationRequest invitationRequest, Principal principal) throws ServiceValidationException {

        log.debug("POST request on /cpanel/federation_invite :" + invitationRequest);
        return federationService.inviteToFederation(invitationRequest, principal, false);
    }

    @PostMapping("/cpanel/federation/handleInvitation")
    public ResponseEntity<?> acceptInvitation(@RequestParam String federationId, @RequestParam String platformId,
                                              @RequestParam Boolean accepted, Principal principal) {

        log.debug("POST request on /cpanel/federation/handleInvitation for federationId = "
                + federationId + " platformId = " + platformId + " accepted = " + accepted);
        return federationService.handleInvitationResponse(federationId, platformId, accepted, principal);
    }

    @PostMapping("/cpanel/federation/addPlatform")
    public ResponseEntity<?> addPlatformToFederation(@RequestParam String federationId, @RequestParam String platformId
            , Principal principal) {

        log.debug("POST request on /cpanel/federation/addPlatform for federationId = "
                + federationId + " platformId = " + platformId);
        return federationVoteRequestService.addPlatformToFederation(federationId, platformId, principal);
    }

    @PostMapping("/cpanel/list_federationsbyvertical")
    public ResponseEntity<?> listFederationsByVertical(@RequestParam String vertical) {

        log.debug("POST request on /cpanel/list_federationsbyvertical");
        return federationService.listFederationsByVertical(vertical);
    }
}