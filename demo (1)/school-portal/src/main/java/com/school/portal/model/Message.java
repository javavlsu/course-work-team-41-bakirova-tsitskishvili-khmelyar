package com.school.portal.model;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private int fromUserId;
    private int toUserId;
    private String messageText;
    private LocalDateTime sentAt;
    private MessageStatus status;
    private User fromUser;
    private User toUser;

    public Message() {}

    public Message(int messageId, int fromUserId, int toUserId, String messageText,
                   LocalDateTime sentAt, MessageStatus status) {
        this.messageId = messageId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.messageText = messageText;
        this.sentAt = sentAt;
        this.status = status;
    }

    // Геттеры и сеттеры
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }

    public int getToUserId() { return toUserId; }
    public void setToUserId(int toUserId) { this.toUserId = toUserId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public String getFormattedSentAt() {
        if (sentAt != null) {
            return String.format("%02d.%02d.%04d %02d:%02d",
                    sentAt.getDayOfMonth(),
                    sentAt.getMonthValue(),
                    sentAt.getYear(),
                    sentAt.getHour(),
                    sentAt.getMinute());
        }
        return "";
    }

    public String getShortMessage() {
        if (messageText == null || messageText.isEmpty()) {
            return "";
        }
        return messageText.length() > 50 ?
                messageText.substring(0, 50) + "..." : messageText;
    }
}