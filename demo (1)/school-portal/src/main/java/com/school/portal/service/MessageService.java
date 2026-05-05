package com.school.portal.service;

import com.school.portal.model.Message;
import com.school.portal.repository.MessageRepository;
import com.school.portal.model.enums.MessageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Page<Message> getInboxMessages(Integer userId, Pageable pageable) {
        return messageRepository.findInboxMessages(userId, pageable);
    }

    public Page<Message> getSentMessages(Integer userId, Pageable pageable) {
        return messageRepository.findSentMessages(userId, pageable);
    }

    public Page<Message> getElectMessages(Integer userId, Pageable pageable) {
        return messageRepository.findElectMessages(userId, pageable);
    }

    public Long getUnreadCount(Integer userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    @Transactional
    public Message sendMessage(Message message) {
        message.setSentAt(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);
        return messageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }

    @Transactional
    public void electMessage(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.ELECT);
        messageRepository.save(message);
    }

    @Transactional
    public void restoreMessage(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }
}