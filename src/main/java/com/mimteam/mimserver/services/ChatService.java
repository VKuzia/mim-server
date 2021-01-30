package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.dto.UserDTO;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.model.responses.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.repositories.ChatsRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final int INVITATION_KEY_LENGTH = 10;

    private final ChatsRepository chatsRepository;

    @Autowired
    public ChatService(ChatsRepository chatsRepository) {
        this.chatsRepository = chatsRepository;
    }

    public ChatEntity createChat(String chatName) {
        ChatEntity chatEntity = new ChatEntity(chatName);
        chatsRepository.save(chatEntity);
        return chatEntity;
    }

    public ResponseEntity<ResponseDTO> getChatUserList(Integer chatId) {
        Optional<ChatEntity> chat = getChatById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.CHAT_NOT_EXISTS);
        }

        List<UserDTO> userIdList = chat.get().getUserList().stream()
                .map(UserToChatEntity::getUserEntity)
                .map(UserDTO::new)
                .sorted(Comparator.comparing(UserDTO::getUserId))
                .collect(Collectors.toList());

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(userIdList)
                .build();
    }

    public ResponseEntity<ResponseDTO> getChatInvitationKey(Integer chatId) {
        Optional<ChatEntity> chat = getChatById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.CHAT_NOT_EXISTS);
        }

        String invitationKey = RandomStringUtils.randomAlphanumeric(INVITATION_KEY_LENGTH);
        chat.get().setInvitationKey(invitationKey);
        chatsRepository.save(chat.get());

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(invitationKey)
                .build();
    }

    public Optional<ChatEntity> getChatById(Integer chatId) {
        return chatsRepository.findById(chatId);
    }

    public Optional<ChatEntity> getChatByInvitationKey(String invitationKey) {
        return chatsRepository.findByInvitationKey(invitationKey);
    }
}
