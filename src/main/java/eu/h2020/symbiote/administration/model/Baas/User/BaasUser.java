package eu.h2020.symbiote.administration.model.Baas.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaasUser {

    @JsonProperty("ID")
    private String id;
    @JsonProperty("Role")
    private String role;
    @JsonProperty("Organization")
    private String organization;
    @JsonProperty("Balance")
    private long balance;
    @JsonProperty("Mail")
    private String email;
    @JsonProperty("AssociatedPlatforms")
    private HashMap<String, List<String>> associated_platforms;

    public BaasUser() {
    }

    public BaasUser(String id, String role, String organization, long balance, HashMap<String, List<String>> associated_platforms, String email) {
        this.id = id;
        this.role = role;
        this.organization = organization;
        this.balance = balance;
        this.associated_platforms = associated_platforms;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public HashMap<String, List<String>> getAssociated_platforms() {
        return associated_platforms;
    }

    public void setAssociated_platforms(HashMap<String, List<String>> associated_platforms) {
        this.associated_platforms = associated_platforms;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
