package eu.h2020.symbiote.administration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserCreationRequest {
    @NotNull
    @Pattern(regexp="^[\\w-]{4,}$", message = "{validation.user.id}")
    @Size(max=30)
    @Id
    private String validUsername;

    @NotNull
    @Size(min=4, max=30)
    private String validPassword;

    @NotNull
    @Email
    private String recoveryMail;

    @NotNull
    private UserRole role;

    @NotNull
    private boolean termsAccepted;

    @NotNull
    private boolean conditionsAccepted;

    @NotNull
    private boolean analyticsAndResearchConsent;

    private String organization;

    private String iotfedsrole;

    @PersistenceConstructor
    @JsonCreator
    public UserCreationRequest(@JsonProperty("username") String username,
                               @JsonProperty("password") String password,
                               @JsonProperty("recoveryMail") String recoveryMail,
                               @JsonProperty("role") UserRole role,
                               @JsonProperty("termsAccepted") boolean termsAccepted,
                               @JsonProperty("conditionsAccepted") boolean conditionsAccepted,
                               @JsonProperty("analyticsAndResearchConsent") boolean analyticsAndResearchConsent,
                               @JsonProperty("organization") String organization,
                               @JsonProperty("iotfedsrole") String iotfedsrole) {
        this.validUsername = username;
        this.validPassword = password;
        this.recoveryMail = recoveryMail;
        this.role = role;
        this.termsAccepted = termsAccepted;
        this.conditionsAccepted = conditionsAccepted;
        this.analyticsAndResearchConsent = analyticsAndResearchConsent;
        this.organization = organization;
        this.iotfedsrole = iotfedsrole;
    }

    /* -------- Getters & Setters -------- */

    public String getValidUsername() {
        return this.validUsername;
    }
    public void setValidUsername(String validUsername) {
        this.validUsername = validUsername;
    }

    public String getValidPassword() {
        return this.validPassword;
    }
    public void setValidPassword(String validPassword) {
        this.validPassword = validPassword;
    }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getRecoveryMail() {
        return this.recoveryMail;
    }
    public void setRecoveryMail(String recoveryMail) {
        this.recoveryMail = recoveryMail;
    }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public boolean isConditionsAccepted() { return conditionsAccepted; }
    public void setConditionsAccepted(boolean conditionsAccepted) { this.conditionsAccepted = conditionsAccepted; }

    public boolean isAnalyticsAndResearchConsent() { return analyticsAndResearchConsent; }
    public void setAnalyticsAndResearchConsent(boolean analyticsAndResearchConsent) { this.analyticsAndResearchConsent = analyticsAndResearchConsent; }

    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getIotfedsrole() {
        return iotfedsrole;
    }
    public void setIotfedsrole(String iotfedsrole) {
        this.iotfedsrole = iotfedsrole;
    }
}
