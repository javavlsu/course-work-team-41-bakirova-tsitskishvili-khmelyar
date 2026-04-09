package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.school.portal.model.enums.MessageStatus;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageId")
    private Integer messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FromUserId", nullable = false)
    private User fromUser;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ToUserId", nullable = false)
    private User toUser;

    @Column(name = "MessageText", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(name = "SentAt", nullable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Status", nullable = false)
    private MessageStatus status;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (status == null) status = MessageStatus.NEW;
    }

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

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
}