package eu.h2020.symbiote.administration.model.Baas.User;

import java.util.HashMap;
import java.util.List;

public class BaasUser {
    private String id;
    private String role;
    private String organization;
    private long balance;
    private HashMap<String, List<String>> associated_platforms;

    public BaasUser() {
    }

    public BaasUser(String id, String role, String organization, long balance, HashMap<String, List<String>> associated_platforms) {
        this.id = id;
        this.role = role;
        this.organization = organization;
        this.balance = balance;
        this.associated_platforms = associated_platforms;
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
}
