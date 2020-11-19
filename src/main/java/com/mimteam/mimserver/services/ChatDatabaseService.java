package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.ResponseBuilder;
import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.model.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.repositories.ChatsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatDatabaseService {
    private final ChatsRepository chatsRepository;

    public ChatDatabaseService(ChatsRepository chatsRepository) {
        this.chatsRepository = chatsRepository;
    }

    public ResponseEntity<ResponseDTO> createChat(String chatName) {
        Optional<ChatEntity> chat = chatsRepository.findByName(chatName);
        if (chat.isPresent()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.CHAT_ALREADY_EXISTS)
                    .build();
        }

        ChatEntity chatEntity = new ChatEntity(chatName);
        chatsRepository.save(chatEntity);

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(chatEntity.getChatId())
                .build();
    }

    public Optional<ChatEntity> getChatById(Integer chatId) {
        return chatsRepository.findById(chatId);
    }
}
