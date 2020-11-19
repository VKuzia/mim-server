package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.ResponseBuilder;
import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage.ChatMembershipMessageType;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.services.ChatDatabaseService;
import com.mimteam.mimserver.services.ChatMembershipService;
import com.mimteam.mimserver.services.MessageDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatController {
    private final EventHandler eventHandler;

    private final ChatDatabaseService chatDatabaseService;
    private final ChatMembershipService chatMembershipService;
    private final MessageDatabaseService messageDatabaseService;

    @Autowired
    ChatController(EventHandler eventHandler,
                   ChatDatabaseService chatDatabaseService,
                   ChatMembershipService chatMembershipService,
                   MessageDatabaseService messageDatabaseService) {
        this.eventHandler = eventHandler;
        this.chatDatabaseService = chatDatabaseService;
        this.chatMembershipService = chatMembershipService;
        this.messageDatabaseService = messageDatabaseService;
    }

    @PostMapping("/chats/create")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleCreateChat(String chatName) {
        return chatDatabaseService.createChat(chatName);
    }

    @PostMapping("/chats/{chatId}/join")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleJoinChat(Integer userId,
                                                      @PathVariable Integer chatId) {
        ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(userId, chatId);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        postMembershipEvent(userId, chatId, ChatMembershipMessageType.JOIN);
        return response;
    }

    @PostMapping("/chats/{chatId}/leave")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleLeaveChat(Integer userId,
                                                @PathVariable Integer chatId) {
        ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(userId, chatId);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        postMembershipEvent(userId, chatId, ChatMembershipMessageType.LEAVE);
        return response;
    }

    @MessageMapping("/chats/{chatId}/message")
    public ResponseEntity<ResponseDTO> handleChatMessage(@Payload MessageDTO dto) {
        ResponseEntity<ResponseDTO> response = ResponseBuilder.builder().ok();
        if (dto.getMessageType() != MessageDTO.MessageType.TEXT_MESSAGE) {
            response = messageDatabaseService.saveTextMessage(new TextMessage(dto));
        }

        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        eventHandler.post(dtoToChatEvent(dto));
        return response;
    }

    private void postMembershipEvent(Integer userId,
                                     Integer chatId,
                                     ChatMembershipMessageType messageType) {
        ChatMembershipMessage message = new ChatMembershipMessage(
                userId, chatId, messageType
        );
        eventHandler.post(new ChatMembershipEvent(message));
    }

    private ChatEvent dtoToChatEvent(MessageDTO dto) {
        switch (dto.getMessageType()) {
            case TEXT_MESSAGE:
                return new SendTextMessageEvent(new TextMessage(dto));
            case CHAT_MEMBERSHIP_MESSAGE:
                return new ChatMembershipEvent(new ChatMembershipMessage(dto));
        }
        return null;
    }
}
