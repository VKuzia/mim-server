package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.ResponseBuilder;
import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MessageDatabaseService {
    private final ChatMessagesRepository chatMessagesRepository;

    public MessageDatabaseService(ChatMessagesRepository chatMessagesRepository) {
        this.chatMessagesRepository = chatMessagesRepository;
    }

    public ResponseEntity<ResponseDTO> saveTextMessage(TextMessage textMessage) {
        ChatMessageEntity entity = new ChatMessageEntity(textMessage);
        chatMessagesRepository.save(entity);
        return ResponseBuilder.builder().ok();
    }
}
