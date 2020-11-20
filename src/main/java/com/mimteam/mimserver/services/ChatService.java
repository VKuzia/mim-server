package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.model.responses.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.repositories.ChatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatsRepository chatsRepository;

    @Autowired
    public ChatService(ChatsRepository chatsRepository) {
        this.chatsRepository = chatsRepository;
    }

    public ResponseEntity<ResponseDTO> createChat(String chatName) {
        Optional<ChatEntity> chat = chatsRepository.findByName(chatName);
        if (chat.isPresent()) {
            return ResponseBuilder.buildError(ResponseType.CHAT_ALREADY_EXISTS);
        }

        ChatEntity chatEntity = new ChatEntity(chatName);
        chatsRepository.save(chatEntity);

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(chatEntity.getChatId())
                .build();
    }

    public ResponseEntity<ResponseDTO> getChatUserIdList(Integer chatId) {
        Optional<ChatEntity> chat = chatsRepository.findById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.CHAT_NOT_EXISTS);
        }

        List<Integer> userIdList = chat.get().getUserList().stream()
                .map(UserToChatEntity::getUserEntity)
                .map(UserEntity::getUserId)
                .collect(Collectors.toList());

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(userIdList)
                .build();
    }

    public Optional<ChatEntity> getChatById(Integer chatId) {
        return chatsRepository.findById(chatId);
    }
}
