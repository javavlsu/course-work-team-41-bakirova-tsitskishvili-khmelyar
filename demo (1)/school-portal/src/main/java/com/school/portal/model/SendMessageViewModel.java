package com.school.portal.model;

public class SendMessageViewModel {
    private int recipientId;
    private String recipientName;
    private String body;

    public SendMessageViewModel() {}

    public int getRecipientId() { return recipientId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}