package com.completetrsst.client.controls.events;

public class AuthenticationEvent {

    private String accountId;
    private boolean isAuthenticated;

    public AuthenticationEvent() {
    }

    public String getAccountId() {
        return accountId;
    }

    public AuthenticationEvent setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public AuthenticationEvent setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
        return this;
    }

}
