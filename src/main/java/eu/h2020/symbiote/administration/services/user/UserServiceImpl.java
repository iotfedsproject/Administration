package eu.h2020.symbiote.administration.services.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.h2020.symbiote.administration.application.events.OnPasswordResetEvent;
import eu.h2020.symbiote.administration.application.events.OnRegistrationCompleteEvent;
import eu.h2020.symbiote.administration.application.listeners.RegistrationListener;
import eu.h2020.symbiote.administration.communication.rabbit.RabbitManager;
import eu.h2020.symbiote.administration.exceptions.authentication.WrongAdminPasswordException;
import eu.h2020.symbiote.administration.exceptions.authentication.WrongUserNameException;
import eu.h2020.symbiote.administration.exceptions.authentication.WrongUserPasswordException;
import eu.h2020.symbiote.administration.exceptions.generic.GenericBadRequestException;
import eu.h2020.symbiote.administration.exceptions.generic.GenericHttpErrorException;
import eu.h2020.symbiote.administration.exceptions.generic.GenericInternalServerErrorException;
import eu.h2020.symbiote.administration.exceptions.rabbit.CommunicationException;
import eu.h2020.symbiote.administration.exceptions.rabbit.EntityUnreachableException;
import eu.h2020.symbiote.administration.exceptions.token.VerificationTokenExpired;
import eu.h2020.symbiote.administration.exceptions.token.VerificationTokenNotFoundException;
import eu.h2020.symbiote.administration.exceptions.validation.ServiceValidationException;
import eu.h2020.symbiote.administration.model.*;
import eu.h2020.symbiote.administration.repository.VerificationTokenRepository;
import eu.h2020.symbiote.administration.services.baas.BaasService;
import eu.h2020.symbiote.security.commons.enums.AccountStatus;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    private static Log log = LogFactory.getLog(RegistrationListener.class);

    private RabbitManager rabbitManager;
    private VerificationTokenRepository tokenRepository;
    private ApplicationEventPublisher eventPublisher;

    private BaasService baasService;

    private String aaMOwnerUsername;
    private String aaMOwnerPassword;
    private Integer tokenExpirationTimeInHours;
    private Boolean emailVerificationEnabled;
    private boolean baasIntegration;

    @Autowired
    public UserServiceImpl(RabbitManager rabbitManager,
                           VerificationTokenRepository tokenRepository,
                           ApplicationEventPublisher eventPublisher,
                           BaasService baasService,
                           @Value("${aam.deployment.owner.username}") String aaMOwnerUsername,
                           @Value("${aam.deployment.owner.password}") String aaMOwnerPassword,
                           @Value("${verificationToken.expirationTime.hours}") Integer tokenExpirationTimeInHours,
                           @Value("${symbiote.core.administration.email.verification}") Boolean emailVerificationEnabled,
                           @Value("${symbiote.baas.integration}") boolean baasIntegration) {
        this.rabbitManager = rabbitManager;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
        this.baasService = baasService;

        Assert.notNull(tokenExpirationTimeInHours, "tokenExpirationTimeInHours can not be null!");
        this.tokenExpirationTimeInHours = tokenExpirationTimeInHours;

        Assert.notNull(aaMOwnerUsername, "aaMOwnerUsername can not be null!");
        this.aaMOwnerUsername = aaMOwnerUsername;

        Assert.notNull(aaMOwnerPassword, "aaMOwnerPassword can not be null!");
        this.aaMOwnerPassword = aaMOwnerPassword;

        Assert.notNull(emailVerificationEnabled, "emailVerificationEnabled can not be null!");
        this.emailVerificationEnabled = emailVerificationEnabled;

        Assert.notNull(baasIntegration,"baasIntegration can not be null!");
        this.baasIntegration = baasIntegration;
    }

    @Override
    public void createVerificationToken(CoreUser user, String tokenString) {
        log.debug("Got tokenString " + tokenString + " for user = " + user);
        tokenRepository.deleteByUser_ValidUsername(user.getValidUsername());
        VerificationToken token = new VerificationToken(tokenString, user, tokenExpirationTimeInHours);
        tokenRepository.save(token);
    }


    @Override
    public VerificationToken verifyToken(String token)
            throws VerificationTokenNotFoundException, VerificationTokenExpired {

        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

        if (!verificationToken.isPresent())
            throw new VerificationTokenNotFoundException(token);

        // Get dates in days
        Long currentDate = convertDateToHours(new Date());
        Long tokenExpirationDate = convertDateToHours(verificationToken.get().getExpirationDate());

        if (currentDate > tokenExpirationDate) {
            tokenRepository.delete(verificationToken.get());
            throw new VerificationTokenExpired(token);
        }

        return verificationToken.get();
    }

    @Override
    public void deleteVerificationToken(VerificationToken verificationToken) {
        tokenRepository.delete(verificationToken);
    }

    @Override
    public void validateUserRegistrationForm(CoreUser coreUser, BindingResult bindingResult) throws ServiceValidationException {
        boolean invalidUserRole = (coreUser.getRole() == UserRole.NULL);

        if (bindingResult.hasErrors() || invalidUserRole) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errorMessages = new HashMap<>();

            for (FieldError fieldError : errors) {
                String errorMessage = fieldError.getDefaultMessage();
                String errorField = fieldError.getField();
                errorMessages.put(errorField, errorMessage);
                log.debug(errorField + ": " + errorMessage);

            }

            if (invalidUserRole)
                errorMessages.put("role", "Invalid User Role");

            response.put("validationErrors", errorMessages);

            throw new ServiceValidationException("Invalid Arguments", errorMessages);
        }
    }

    @Override
    public void createUserAccount(String jsonRequest)
            throws CommunicationException, GenericHttpErrorException {
        UserCreationRequest request = new Gson().fromJson(jsonRequest, UserCreationRequest.class);
        // Construct the UserManagementRequest
        UserManagementRequest userRegistrationRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(request.getValidUsername(), request.getValidPassword()),
                new UserDetails(
                        new Credentials(request.getValidUsername(), request.getValidPassword()),
                        request.getRecoveryMail(),
                        request.getRole(),
                        emailVerificationEnabled ? AccountStatus.NEW : AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        request.isConditionsAccepted(),
                        request.isAnalyticsAndResearchConsent()
                ),
                OperationType.CREATE
        );

        ManagementStatus managementStatus = rabbitManager.sendUserManagementRequest(userRegistrationRequest);

        if (managementStatus == null) {
            throw new EntityUnreachableException("AAM");

        } else if (managementStatus == ManagementStatus.OK) {
            //Register user to Baas
            ResponseEntity<String> baasStatus = baasService.registerUserToBcBaasRequest(request);
            if (baasIntegration && baasStatus.getStatusCode() != HttpStatus.OK) {
                throw new GenericBadRequestException(baasStatus.getBody());
            }
            if (emailVerificationEnabled) {
                try {
                    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                    CoreUser coreUser = new CoreUser(request.getValidUsername(), request.getValidPassword(), true, true,
                            true, true, grantedAuthorities, request.getRecoveryMail(), request.getRole(), request.isTermsAccepted(),
                            request.isConditionsAccepted(), request.isAnalyticsAndResearchConsent(), request.getOrganization(), request.getIotfedsrole());
                    coreUser.setValidPassword(request.getValidPassword());
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                            (coreUser, Locale.US, "", request.getRecoveryMail()));
                } catch (Exception e) {
                    log.warn("Exception during sending verification email", e);
                    throw new GenericInternalServerErrorException("Could not send verification email");
                }
            }
        } else if (managementStatus == ManagementStatus.USERNAME_EXISTS) {
            throw new GenericBadRequestException("Username exists!");
        } else
            throw new GenericBadRequestException(managementStatus.toString());
    }


    @Override
    public void createUserAccount(CoreUser coreUser, WebRequest webRequest)
            throws CommunicationException, GenericHttpErrorException {

        // Construct the UserManagementRequest
        UserManagementRequest userRegistrationRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(coreUser.getValidUsername(), coreUser.getValidPassword()),
                new UserDetails(
                        new Credentials(coreUser.getValidUsername(), coreUser.getValidPassword()),
                        coreUser.getRecoveryMail(),
                        coreUser.getRole(),
                        emailVerificationEnabled ? AccountStatus.NEW : AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        coreUser.isConditionsAccepted(),
                        coreUser.isAnalyticsAndResearchConsent()
                ),
                OperationType.CREATE
        );

        ManagementStatus managementStatus = rabbitManager.sendUserManagementRequest(userRegistrationRequest);
        coreUser.clearSensitiveData();

        if (managementStatus == null) {
            throw new EntityUnreachableException("AAM");

        } else if (managementStatus == ManagementStatus.OK) {
            if (emailVerificationEnabled) {
                try {
                    String appUrl = webRequest.getContextPath();
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                            (coreUser, webRequest.getLocale(), appUrl, coreUser.getRecoveryMail()));
                } catch (Exception e) {
                    log.warn("Exception during sending verification email", e);
                    throw new GenericInternalServerErrorException("Could not send verification email");
                }
            }

//            //Register user to Baas
//TODO: Role, organization are static. Ask marios to change the endpoint to use requestUser or extend CoreUser
            ResponseEntity<?> baasStatus = baasService.registerUserToBcBaasRequest(userRegistrationRequest);
            if (baasIntegration && baasStatus.getStatusCode() != HttpStatus.OK) {
                throw new GenericBadRequestException("Baas responded as bad request");
            }

        } else if (managementStatus == ManagementStatus.USERNAME_EXISTS) {
            throw new GenericBadRequestException("Username exists!");
        } else
            throw new GenericBadRequestException(managementStatus.toString());
    }

    @Override
    public void activateUserAccount(VerificationToken verificationToken)
            throws CommunicationException, GenericBadRequestException, EntityUnreachableException {
        CoreUser coreUser = verificationToken.getUser();

        // Todo: read contents from AAM instead
        // Construct the UserManagementRequest
        UserManagementRequest userRegistrationRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(coreUser.getValidUsername(), ""),
                new UserDetails(
                        new Credentials(coreUser.getValidUsername(), ""),
                        coreUser.getRecoveryMail(),
                        coreUser.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        coreUser.isConditionsAccepted(),
                        coreUser.isAnalyticsAndResearchConsent()
                ),
                OperationType.FORCE_UPDATE
        );

        ManagementStatus managementStatus = rabbitManager.sendUserManagementRequest(userRegistrationRequest);

        if (managementStatus == null)
            throw new EntityUnreachableException("AAM");
        else if (managementStatus != ManagementStatus.OK)
            throw new GenericBadRequestException(managementStatus.toString());
    }

    @Override
    public void resendVerificationEmail(ResendVerificationEmailRequest request,
                                        BindingResult bindingResult,
                                        WebRequest webRequest)
            throws GenericHttpErrorException {

        String errorName = "resendVerificationEmailError";
        Map<String, String> errorsResponse = new HashMap<>();

        if (!emailVerificationEnabled) {
            errorsResponse.put(errorName, "Verification via email is not enabled");
            throw new GenericBadRequestException("Error getting user details", errorsResponse);
        }

        handleValidationErrors(bindingResult, errorsResponse, errorName);

        UserDetails userDetails = null;
        try {
            userDetails = getUserInformationWithLogin(request.getUsername(), request.getPassword());
        } catch (WrongUserNameException | WrongUserPasswordException | WrongAdminPasswordException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericHttpErrorException("Error getting user details", errorsResponse, HttpStatus.valueOf(e.getHttpStatus()));
        } catch (GenericHttpErrorException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericHttpErrorException("Error getting user details", errorsResponse, e.getHttpStatus());
        } catch (CommunicationException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericBadRequestException("Error getting user details", errorsResponse);
        } catch (EntityUnreachableException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericInternalServerErrorException("Error getting user details", errorsResponse);
        }

        if (userDetails == null) {
            errorsResponse.put(errorName, "Error getting user details");
            throw new GenericInternalServerErrorException("Error getting user details", errorsResponse);
        }

        if (userDetails.getStatus() == AccountStatus.ACTIVE) {
            errorsResponse.put(errorName, "Account already Active");
            throw new GenericBadRequestException("Account already Active", errorsResponse);
        }

        if (emailVerificationEnabled) {
            CoreUser coreUser = new CoreUser(
                    request.getUsername(), "", true, true,
                    true, true, new ArrayList<>(), userDetails.getRecoveryMail(),
                    userDetails.getRole(), userDetails.hasGrantedServiceConsent(), userDetails.hasGrantedServiceConsent(),
                    userDetails.hasGrantedAnalyticsAndResearchConsent(), null, null);

            try {
                String appUrl = webRequest.getContextPath();
                eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                        (coreUser, webRequest.getLocale(), appUrl, coreUser.getRecoveryMail()));
            } catch (Exception e) {
                log.warn("Exception during resending verification email", e);
                throw new GenericInternalServerErrorException("Could not resend verification email");
            }
        }
    }

    @Override
    public UserDetails getUserInformationWithLogin(String username, String password)
            throws CommunicationException, GenericHttpErrorException, EntityUnreachableException {
        return getUserInformation(username, password, false);
    }

    @Override
    public UserDetails getUserInformationWithLogin(Principal principal)
            throws CommunicationException, GenericHttpErrorException, EntityUnreachableException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        return getUserInformationWithLogin(user.getUsername(), password);
    }

    @Override
    public UserDetails getUserInformationWithForce(String username)
            throws CommunicationException, GenericHttpErrorException, EntityUnreachableException {
        return getUserInformation(username, "", true);
    }

    @Override
    public UserDetails getUserInformationWithForce(Principal principal)
            throws CommunicationException, GenericHttpErrorException, EntityUnreachableException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        return getUserInformationWithForce(user.getUsername());
    }

    @Override
    public void changeEmail(ChangeEmailRequest message, BindingResult bindingResult, Principal principal)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "changeEmailError";

        if (bindingResult.hasErrors()) {

            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError fieldError : errors) {
                String errorMessage = "Enter a valid email";
                String errorField = "error_" + fieldError.getField();
                log.debug(errorField + ": " + errorMessage);
                errorsResponse.put(errorField, errorMessage);
            }
        }

        if (errorsResponse.get("error_newEmailRetyped") == null &&
                !message.getNewEmail().equals(message.getNewEmailRetyped())) {
            String errorField = "error_newEmailRetyped";
            String errorMessage = "The provided emails do not match";
            log.debug(errorField + ": " + errorMessage);
            errorsResponse.put(errorField, errorMessage);

        }

        if (errorsResponse.size() > 0) {
            errorsResponse.put(errorName, "Invalid Arguments");
            throw new GenericBadRequestException("Invalid Arguments", errorsResponse);
        }

        // Todo: fill in the attributes
        // Construct the UserManagementRequest
        UserManagementRequest userUpdateRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(user.getUsername(), password),
                new UserDetails(
                        new Credentials(user.getUsername(), password),
                        message.getNewEmail(),
                        user.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        user.isConditionsAccepted(),
                        user.isAnalyticsAndResearchConsent()
                ),
                OperationType.UPDATE
        );


        handleUserManagementRequest(userUpdateRequest, errorName);
    }

    @Override
    public void changePermissions(ChangePermissions message, BindingResult bindingResult, Principal principal)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "changePermissionsError";

        handleValidationErrors(bindingResult, errorsResponse, errorName);

        // Todo: fill in the attributes
        // Construct the UserManagementRequest
        UserManagementRequest userUpdateRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(user.getUsername(), password),
                new UserDetails(
                        new Credentials(user.getUsername(), password),
                        user.getRecoveryMail(),
                        user.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        user.isConditionsAccepted(),
                        message.isAnalyticsAndResearchConsent()
                ),
                OperationType.UPDATE
        );

        handleUserManagementRequest(userUpdateRequest, errorName);
    }

    @Override
    public void changePassword(ChangePasswordRequest message, BindingResult bindingResult, Principal principal) throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "changePasswordError";

        if (bindingResult.hasErrors()) {

            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError fieldError : errors) {
                String errorMessage = "Enter a valid password";
                String errorField = "error_" + fieldError.getField();
                log.debug(errorField + ": " + errorMessage);
                errorsResponse.put(errorField, errorMessage);
            }
        }

        if (errorsResponse.get("error_newPasswordRetyped") == null &&
                !message.getNewPassword().equals(message.getNewPasswordRetyped())) {
            String errorField = "error_newPasswordRetyped";
            String errorMessage = "The provided passwords do not match";
            log.debug(errorField + ": " + errorMessage);
            errorsResponse.put(errorField, errorMessage);

        }

        if (!password.equals(message.getOldPassword())) {
            String errorMessage = "Your old password is not correct";
            String errorField = "error_oldPassword";
            log.debug(errorField + ": " + errorMessage);
            errorsResponse.put(errorField, errorMessage);
        }

        if (errorsResponse.size() > 0) {
            errorsResponse.put(errorName, "Invalid Arguments");
            throw new GenericBadRequestException("Invalid Arguments", errorsResponse);
        }

        // Todo: fill in the attributes
        // Construct the UserManagementRequest
        UserManagementRequest userUpdateRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(user.getUsername(), password),
                new UserDetails(
                        new Credentials(user.getUsername(), message.getNewPassword()),
                        user.getRecoveryMail(),
                        user.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        user.isConditionsAccepted(),
                        user.isAnalyticsAndResearchConsent()
                ),
                OperationType.UPDATE
        );

        handleUserManagementRequest(userUpdateRequest, errorName);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, BindingResult bindingResult, WebRequest webRequest)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();
        String errorName = "resetPasswordError";

        handleValidationErrors(bindingResult, errorsResponse, errorName);

        UserDetails userDetails = null;
        try {
            userDetails = getUserInformationWithForce(request.getUsername());
        } catch (WrongUserNameException | WrongUserPasswordException | WrongAdminPasswordException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericHttpErrorException("Error getting user details", errorsResponse, HttpStatus.valueOf(e.getHttpStatus()));
        } catch (GenericHttpErrorException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericHttpErrorException("Error getting user details", errorsResponse, e.getHttpStatus());
        } catch (CommunicationException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericBadRequestException("Error getting user details", errorsResponse);
        } catch (EntityUnreachableException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericInternalServerErrorException("Error getting user details", errorsResponse);
        }

        if (!request.getUsername().equals(userDetails.getCredentials().getUsername()) ||
                !request.getEmail().equals(userDetails.getRecoveryMail())) {
            errorsResponse.put(errorName, "No user with such credentials");
            throw new GenericBadRequestException("Error in fetching user details", errorsResponse);
        }

        // Generate new password
        String newPassword = RandomStringUtils.randomAlphanumeric(12);

        // Todo: fill in the attributes
        // Construct the UserManagementRequest
        UserManagementRequest userUpdateRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(request.getUsername(), newPassword),
                new UserDetails(
                        new Credentials(request.getUsername(), newPassword),
                        userDetails.getRecoveryMail(),
                        userDetails.getRole(),
                        userDetails.getStatus(),
                        userDetails.getAttributes(),
                        userDetails.getClients(),
                        userDetails.hasGrantedServiceConsent(),
                        userDetails.hasGrantedAnalyticsAndResearchConsent()
                ),
                OperationType.FORCE_UPDATE
        );

        handleUserManagementRequest(userUpdateRequest, errorName);

        try {
            eventPublisher.publishEvent(new OnPasswordResetEvent(
                    request.getUsername(), webRequest.getLocale(), request.getEmail(), newPassword));
        } catch (Exception e) {
            log.warn("Exception during sending password reset email", e);
            throw new GenericInternalServerErrorException("Could not send password reset email");
        }
    }

    @Override
    public void acceptTerms(Principal principal)
            throws GenericHttpErrorException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "acceptTermsError";

        // Todo: fill in the attributes
        // Construct the UserManagementRequest
        UserManagementRequest userUpdateRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(user.getUsername(), password),
                new UserDetails(
                        new Credentials(user.getUsername(), ""),
                        "",
                        user.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        true,
                        user.isAnalyticsAndResearchConsent()
                ),
                OperationType.FORCE_UPDATE
        );

        handleUserManagementRequest(userUpdateRequest, errorName);
    }

    @Override
    public void deleteUser(Principal principal) throws GenericHttpErrorException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "userDeletionError";

        // Construct the UserManagementRequest
        UserManagementRequest userDeleteRequest = new UserManagementRequest(
                new Credentials(aaMOwnerUsername, aaMOwnerPassword),
                new Credentials(user.getUsername(), password),
                new UserDetails(
                        new Credentials(user.getUsername(), password),
                        "",
                        user.getRole(),
                        AccountStatus.ACTIVE,
                        new HashMap<>(),
                        new HashMap<>(),
                        user.isConditionsAccepted(),
                        user.isAnalyticsAndResearchConsent()
                ),
                OperationType.DELETE
        );

        handleUserManagementRequest(userDeleteRequest, errorName);
        //Delete user from Baas
        ResponseEntity<?> baasStatus = baasService.deleteUserToBcBaasRequest(userDeleteRequest.getUserDetails().getCredentials().getUsername());
        if (baasIntegration && baasStatus.getStatusCode() != HttpStatus.OK) {
            throw new GenericBadRequestException("Baas responded as bad request");
        }
    }

    @Override
    public void deleteClient(String clientId, Principal principal) throws GenericHttpErrorException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CoreUser user = (CoreUser) token.getPrincipal();
        String password = (String) token.getCredentials();
        String errorName = "clientDeletionError";

        // Construct the UserManagementRequest
        RevocationRequest revocationRequest = new RevocationRequest();
        revocationRequest.setCredentials(new Credentials(user.getUsername(), password));
        revocationRequest.setCredentialType(RevocationRequest.CredentialType.USER);
        revocationRequest.setCertificateCommonName(user.getUsername() + "@" + clientId);

        handleRevocationRequest(revocationRequest, errorName);

    }

    private UserDetails getUserInformation(String username, String password, boolean force)
            throws CommunicationException, GenericHttpErrorException, EntityUnreachableException,
            WrongUserNameException, WrongUserPasswordException, WrongAdminPasswordException {

        UserDetailsResponse userDetailsResponse;

        if (!force)
            userDetailsResponse = rabbitManager.sendLoginRequest(new Credentials(username, password));
        else
            userDetailsResponse = rabbitManager.sendForceReadRequest(username);

        if (userDetailsResponse == null)
            throw new EntityUnreachableException("AAM");

        switch (userDetailsResponse.getHttpStatus()) {
            case OK:
                break;
            case BAD_REQUEST:
                log.warn("Username does not exist");
                throw new WrongUserNameException();
            case UNAUTHORIZED:
                log.warn("Wrong user password");
                throw new WrongUserPasswordException();
            case FORBIDDEN:
                if (userDetailsResponse.getUserDetails() == null) {
                    log.warn("Wrong admin password");
                    throw new WrongAdminPasswordException();
                } else {
                    break;
                }
            default:
                throw new GenericHttpErrorException("Could not get the userDetails", userDetailsResponse.getHttpStatus());
        }

        return userDetailsResponse.getUserDetails();
    }

    private UserDetailsResponse handleForceReadRequest(String username, String errorName)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();
        UserDetailsResponse response;

        try {
            response = rabbitManager.sendForceReadRequest(username);

            if (response == null) {
                errorsResponse.put(errorName, "Authorization Manager is unreachable!");
                throw new GenericHttpErrorException("Authorization Manager is unreachable!", errorsResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!response.getHttpStatus().is2xxSuccessful()) {
                errorsResponse.put(errorName, "The Authorization Manager responded with: " + response.getHttpStatus().getReasonPhrase());
                throw new GenericHttpErrorException("The Authorization Manager responded with ERROR", errorsResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (CommunicationException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericBadRequestException("Error", errorsResponse);
        }
        return response;
    }

    private void handleUserManagementRequest(UserManagementRequest userManagementRequest, String errorName)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();

        try {
            ManagementStatus managementStatus = rabbitManager.sendUserManagementRequest(userManagementRequest);

            if (managementStatus == null) {
                errorsResponse.put(errorName, "Authorization Manager is unreachable!");
                throw new GenericHttpErrorException("Authorization Manager is unreachable!", errorsResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (managementStatus != ManagementStatus.OK) {
                errorsResponse.put(errorName, "The Authorization Manager responded with ERROR");
                throw new GenericHttpErrorException("The Authorization Manager responded with ERROR", errorsResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (CommunicationException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericBadRequestException("Error", errorsResponse);
        }
    }

    private void handleRevocationRequest(RevocationRequest revocationRequest, String errorName)
            throws GenericHttpErrorException {
        Map<String, String> errorsResponse = new HashMap<>();

        try {
            RevocationResponse revocationResponse = rabbitManager.sendRevocationRequest(revocationRequest);

            if (revocationResponse == null) {
                errorsResponse.put(errorName, "Authorization Manager is unreachable!");
                throw new GenericHttpErrorException("Authorization Manager is unreachable!", errorsResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!revocationResponse.getStatus().is2xxSuccessful() || !revocationResponse.isRevoked()) {
                errorsResponse.put(errorName, "The Authorization Manager responded with ERROR");
                throw new GenericHttpErrorException("The Authorization Manager responded with ERROR", errorsResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (CommunicationException e) {
            errorsResponse.put(errorName, e.getMessage());
            throw new GenericBadRequestException("Error", errorsResponse);
        }
    }

    private void handleValidationErrors(BindingResult bindingResult, Map<String, String> errorsResponse, String errorName)
            throws GenericBadRequestException {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError fieldError : errors) {
                String errorMessage = fieldError.getDefaultMessage();
                String errorField = "error_" + fieldError.getField();
                log.debug(errorField + ": " + errorMessage);
                errorsResponse.put(errorField, errorMessage);
            }

            errorsResponse.put(errorName, "Invalid values");
            throw new GenericBadRequestException("Invalid Arguments", errorsResponse);
        }
    }

    private Long convertDateToHours(Date date) {
        return TimeUnit.MILLISECONDS.toHours(date.getTime());
    }
}
