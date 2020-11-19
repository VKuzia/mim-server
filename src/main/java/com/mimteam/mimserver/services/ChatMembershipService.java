package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.ResponseBuilder;
import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.model.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatMembershipService {
    private final UserDatabaseService userDatabaseService;
    private final ChatDatabaseService chatDatabaseService;

    private final UsersToChatsRepository usersToChatsRepository;

    @Autowired
    public ChatMembershipService(UserDatabaseService userDatabaseService,
                                 ChatDatabaseService chatDatabaseService,
                                 UsersToChatsRepository usersToChatsRepository) {
        this.userDatabaseService = userDatabaseService;
        this.chatDatabaseService = chatDatabaseService;
        this.usersToChatsRepository = usersToChatsRepository;
    }

    public ResponseEntity<ResponseDTO> joinChat(Integer userId, Integer chatId) {
        Optional<UserEntity> user = userDatabaseService.getUserById(userId);
        if (user.isEmpty()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_NOT_EXISTS)
                    .build();
        }

        Optional<ChatEntity> chat = chatDatabaseService.getChatById(chatId);
        if (chat.isEmpty()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.CHAT_NOT_EXISTS)
                    .build();
        }

        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        if (usersToChatsRepository.findById(userToChatId).isPresent()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_ALREADY_IN_CHAT)
                    .build();
        }

        UserToChatEntity userToChatEntity = new UserToChatEntity(userToChatId);
        userToChatEntity.setUserEntity(user.get());
        userToChatEntity.setChatEntity(chat.get());
        usersToChatsRepository.save(userToChatEntity);

        return ResponseBuilder.builder().ok();
    }

    public ResponseEntity<ResponseDTO> leaveChat(Integer userId, Integer chatId) {
        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        Optional<UserToChatEntity> userToChat = usersToChatsRepository.findById(userToChatId);
        if (userToChat.isEmpty()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_NOT_IN_CHAT)
                    .build();
        }

        usersToChatsRepository.delete(userToChat.get());

        return ResponseBuilder.builder().ok();
    }
}
