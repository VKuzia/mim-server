package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {
    private final ChatMessagesRepository chatMessagesRepository;

    @Autowired
    public ChatMessageService(ChatMessagesRepository chatMessagesRepository) {
        this.chatMessagesRepository = chatMessagesRepository;
    }

    public ResponseEntity<ResponseDTO> saveTextMessage(TextMessage textMessage) {
        ChatMessageEntity entity = new ChatMessageEntity(textMessage);
        chatMessagesRepository.save(entity);
        return ResponseBuilder.buildSuccess();
    }

    public ResponseEntity<ResponseDTO> getMessageList(Integer chatId) {
        List<ChatMessageEntity> messageEntities = chatMessagesRepository.findByChatId(chatId);

        return ResponseBuilder.builder()
                .responseType(ResponseDTO.ResponseType.OK)
                .body(messageEntities)
                .build();
    }
}
