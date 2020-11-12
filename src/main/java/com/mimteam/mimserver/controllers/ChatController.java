package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.events.JoinChatEvent;
import com.mimteam.mimserver.events.LeaveChatEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final EventHandler eventHandler;

    @Autowired
    ChatController(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @MessageMapping("/chats/{chatId}/message")
    public void handleChatMessage(@Payload MessageDTO dto) {
        eventHandler.post(dtoToChatEvent(dto));
    }

    private ChatEvent dtoToChatEvent(MessageDTO dto) {
        if (dto.getMessageType() == MessageDTO.MessageType.TEXT_MESSAGE) {
            TextMessage message = new TextMessage();
            message.fromDataTransferObject(dto);
            return new SendTextMessageEvent(message);
        }
        if (dto.getMessageType() == MessageDTO.MessageType.CHAT_MEMBERSHIP_MESSAGE) {
            ChatMembershipMessage message = new ChatMembershipMessage();
            message.fromDataTransferObject(dto);
            if (message.getChatMembershipMessageType() == ChatMembershipMessage.ChatMembershipMessageType.JOIN) {
                return new JoinChatEvent(message);
            }
            return new LeaveChatEvent(message);
        }
        return null;
    }
}
