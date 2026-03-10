package com.school.portal.service;

import com.school.portal.model.Message;
import com.school.portal.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getInboxMessages(Integer userId) {
        return messageRepository.findInboxMessages(userId);
    }

    public List<Message> getSentMessages(Integer userId) {
        return messageRepository.findSentMessages(userId);
    }

    public List<Message> getArchivedMessages(Integer userId) {
        return messageRepository.findArchivedMessages(userId);
    }

    public Long getUnreadCount(Integer userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    @Transactional
    public Message sendMessage(Message message) {
        message.setSentAt(LocalDateTime.now());
        message.setStatus(0); // Новое
        return messageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(1); // Прочитано
        messageRepository.save(message);
    }

    @Transactional
    public void archiveMessage(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(2); // В архиве
        messageRepository.save(message);
    }

    @Transactional
    public void restoreMessage(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(1); // Прочитано
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }
}