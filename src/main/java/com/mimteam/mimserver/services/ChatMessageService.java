package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatService chatService;

    @Autowired
    public ChatMessageService(ChatMessagesRepository chatMessagesRepository, ChatService chatService) {
        this.chatMessagesRepository = chatMessagesRepository;
        this.chatService = chatService;
    }

    public ResponseEntity<ResponseDTO> saveTextMessage(TextMessage textMessage) {
        ChatMessageEntity entity = new ChatMessageEntity(textMessage);
        chatMessagesRepository.save(entity);
        return ResponseBuilder.buildSuccess();
    }

    public ResponseEntity<ResponseDTO> getMessageList(Integer chatId) {
        Optional<ChatEntity> chat = chatService.getChatById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS);
        }

        List<ChatMessageEntity> messageEntities = chatMessagesRepository.findByChatId(chatId);

        return ResponseBuilder.builder()
                .responseType(ResponseDTO.ResponseType.OK)
                .body(messageEntities)
                .build();
    }
}
