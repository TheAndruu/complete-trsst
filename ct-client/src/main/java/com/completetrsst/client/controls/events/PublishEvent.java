package com.completetrsst.client.controls.events;

public class PublishEvent {

    private String accountId;

    public PublishEvent() {
    }

    public String getAccountId() {
        return accountId;
    }

    public PublishEvent setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

}
