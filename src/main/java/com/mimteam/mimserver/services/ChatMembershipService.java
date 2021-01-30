package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.dto.ChatDTO;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.model.responses.ResponseDTO.ResponseType;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatMembershipService {
    private final UserService userService;
    private final ChatService chatService;

    private final UsersToChatsRepository usersToChatsRepository;

    @Autowired
    public ChatMembershipService(UserService userService,
                                 ChatService chatService,
                                 UsersToChatsRepository usersToChatsRepository) {
        this.userService = userService;
        this.chatService = chatService;
        this.usersToChatsRepository = usersToChatsRepository;
    }

    public ResponseEntity<ResponseDTO> joinChat(Integer userId, Integer chatId) {
        Optional<UserEntity> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.USER_NOT_EXISTS);
        }

        Optional<ChatEntity> chat = chatService.getChatById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.CHAT_NOT_EXISTS);
        }

        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        if (usersToChatsRepository.findById(userToChatId).isPresent()) {
            return ResponseBuilder.buildError(ResponseType.USER_ALREADY_IN_CHAT);
        }

        UserToChatEntity userToChatEntity = new UserToChatEntity(userToChatId);
        userToChatEntity.setUserEntity(user.get());
        userToChatEntity.setChatEntity(chat.get());
        usersToChatsRepository.save(userToChatEntity);

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(new ChatDTO(chat.get()))
                .build();
    }

    public ResponseEntity<ResponseDTO> leaveChat(Integer userId, Integer chatId) {
        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        Optional<UserToChatEntity> userToChat = usersToChatsRepository.findById(userToChatId);
        if (userToChat.isEmpty()) {
            return ResponseBuilder.buildError(ResponseType.USER_NOT_IN_CHAT);
        }

        usersToChatsRepository.delete(userToChat.get());
        return ResponseBuilder.buildSuccess();
    }

    public boolean isUserInChat(Integer chatId, Integer userId) {
        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        Optional<UserToChatEntity> userToChat = usersToChatsRepository.findById(userToChatId);
        return userToChat.isPresent();
    }
}
