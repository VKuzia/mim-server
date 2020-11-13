package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.events.ChatMembershipEvent;
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
