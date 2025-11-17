package main.java.com.school.beans;

import java.sql.Date;

public class Message {
    private int messageId;
    private Date sentAt;
    private int fromUserId;
    private int toUserId;
    private String messageText;

    public Message() {}

    public Message(int messageId, Date sentAt, int fromUserId, int toUserId, String messageText) {
        this.messageId = messageId;
        this.sentAt = sentAt;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.messageText = messageText;
    }

    public Message(Date sentAt, int fromUserId, int toUserId, String messageText) {
        this(0, sentAt, fromUserId, toUserId, messageText);
    }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }

    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }

    public int getToUserId() { return toUserId; }
    public void setToUserId(int toUserId) { this.toUserId = toUserId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    @Override
    public String toString() {
        return "Message{messageId=" + messageId + ", from=" + fromUserId + ", to=" + toUserId + "}";
    }
}