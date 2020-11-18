package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.repositories.ChatsRepository;
import com.mimteam.mimserver.repositories.UsersRepository;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class ChatController {
    private final EventHandler eventHandler;

    private final ChatsRepository chatsRepository;
    private final UsersRepository usersRepository;
    private final UsersToChatsRepository usersToChatsRepository;

    @Autowired
    ChatController(EventHandler eventHandler,
                   ChatsRepository chatsRepository,
                   UsersRepository usersRepository,
                   UsersToChatsRepository usersToChatsRepository) {
        this.eventHandler = eventHandler;
        this.chatsRepository = chatsRepository;
        this.usersRepository = usersRepository;
        this.usersToChatsRepository = usersToChatsRepository;
    }

    @PostMapping("/chats/create")
    @ResponseBody
    public ResponseEntity<Integer> handleCreateChat(String chatName) {
        Optional<ChatEntity> chat = chatsRepository.findByName(chatName);
        if (chat.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        ChatEntity chatEntity = new ChatEntity(chatName);
        chatsRepository.save(chatEntity);

        return ResponseEntity.ok(chatEntity.getChatId());
    }

    @PostMapping("/chats/{chatId}/join")
    @ResponseBody
    public ResponseEntity<Void> handleJoinChat(Integer userId,
                                               @PathVariable Integer chatId) {
        Optional<UserEntity> user = usersRepository.findById(userId);
        Optional<ChatEntity> chat = chatsRepository.findById(chatId);
        if (user.isEmpty() || chat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        if (usersToChatsRepository.findById(userToChatId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserToChatEntity userToChatEntity = new UserToChatEntity(userToChatId);
        userToChatEntity.setUserEntity(user.get());
        userToChatEntity.setChatEntity(chat.get());
        usersToChatsRepository.save(userToChatEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/chats/{chatId}/leave")
    @ResponseBody
    public ResponseEntity<Void> handleLeaveChat(Integer userId,
                                                @PathVariable Integer chatId) {
        UserToChatId userToChatId = new UserToChatId(userId, chatId);
        Optional<UserToChatEntity> userToChat = usersToChatsRepository.findById(userToChatId);
        if (userToChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        usersToChatsRepository.delete(userToChat.get());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @MessageMapping("/chats/{chatId}/message")
    public void handleChatMessage(@Payload MessageDTO dto) {
        eventHandler.post(dtoToChatEvent(dto));
    }

    private ChatEvent dtoToChatEvent(MessageDTO dto) {
        switch (dto.getMessageType()) {
            case TEXT_MESSAGE:
                TextMessage textMessage = new TextMessage();
                textMessage.fromDataTransferObject(dto);
                return new SendTextMessageEvent(textMessage);
            case CHAT_MEMBERSHIP_MESSAGE:
                ChatMembershipMessage chatMembershipMessage = new ChatMembershipMessage();
                chatMembershipMessage.fromDataTransferObject(dto);
                return new ChatMembershipEvent(chatMembershipMessage);
        }
        return null;
    }
}
