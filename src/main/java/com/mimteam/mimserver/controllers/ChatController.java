package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage.ChatMembershipMessageType;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.services.ChatMembershipService;
import com.mimteam.mimserver.services.ChatMessageService;
import com.mimteam.mimserver.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class ChatController {
    private final EventHandler eventHandler;

    private final ChatService chatService;
    private final ChatMembershipService chatMembershipService;
    private final ChatMessageService chatMessageService;

    @Autowired
    ChatController(EventHandler eventHandler,
                   ChatService chatService,
                   ChatMembershipService chatMembershipService,
                   ChatMessageService chatMessageService) {
        this.eventHandler = eventHandler;
        this.chatService = chatService;
        this.chatMembershipService = chatMembershipService;
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/chats/create")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleCreateChat(String chatName) {
        return chatService.createChat(chatName);
    }

    @PostMapping("/chats/join")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleJoinChat(Authentication authentication, String invitationKey) {
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        Optional<ChatEntity> chatEntity = chatService.getChatByInvitationKey(invitationKey);

        if (chatEntity.isEmpty()) {
            return ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS);
        }

        ResponseEntity<ResponseDTO> response =
                chatMembershipService.joinChat(userEntity.getUserId(), chatEntity.get().getChatId());
        if (response.getStatusCode().is2xxSuccessful()) {
            postMembershipEvent(userEntity.getUserId(), chatEntity.get().getChatId(), ChatMembershipMessageType.JOIN);
        }

        return response;
    }

    @PostMapping("/chats/{chatId}/leave")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleLeaveChat(Authentication authentication,
                                                       @PathVariable Integer chatId) {
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();

        ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(userEntity.getUserId(), chatId);
        if (response.getStatusCode().is2xxSuccessful()) {
            postMembershipEvent(userEntity.getUserId(), chatId, ChatMembershipMessageType.LEAVE);
        }

        return response;
    }

    @GetMapping("/chats/{chatId}/userlist")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUserChatList(@PathVariable Integer chatId) {
        return chatService.getChatUserIdList(chatId);
    }

    @GetMapping("/chats/{chatId}/invitation")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getChatInvitationKey(@PathVariable Integer chatId) {
        return chatService.getChatInvitationKey(chatId);
    }

    @MessageMapping("/chats/{chatId}/message")
    public ResponseEntity<ResponseDTO> handleChatMessage(@Payload MessageDTO dto) {
        ResponseEntity<ResponseDTO> response = ResponseBuilder.buildSuccess();
        if (dto.getMessageType() == MessageDTO.MessageType.TEXT_MESSAGE) {
            response = chatMessageService.saveTextMessage(new TextMessage(dto));
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            eventHandler.post(dtoToChatEvent(dto));
        }
        return response;
    }

    @GetMapping("/chats/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getMessageHistory(@PathVariable Integer chatId) {
        return chatMessageService.getMessageList(chatId);
    }

    private void postMembershipEvent(Integer userId,
                                     Integer chatId,
                                     ChatMembershipMessageType messageType) {
        ChatMembershipMessage message = new ChatMembershipMessage(userId, chatId, messageType);
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
