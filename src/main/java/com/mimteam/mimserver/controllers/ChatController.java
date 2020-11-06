package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.events.JoinChatEvent;
import com.mimteam.mimserver.events.LeaveChatEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.model.chat.ChatMembershipMessage;
import com.mimteam.mimserver.model.chat.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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

    @MessageMapping("/chats/{chatId}/addUser")
    public void handleAddUserMessage(@DestinationVariable("chatId") Integer chatId,
                                     @Payload ChatMembershipMessage chatMembershipMessage) {
        JoinChatEvent event = new JoinChatEvent(chatId, chatMembershipMessage);
        eventHandler.post(event);
    }

    @MessageMapping("/chats/{chatId}/removeUser")
    public void handleRemoveUserMessage(@DestinationVariable("chatId") Integer chatId,
                                        @Payload ChatMembershipMessage chatMembershipMessage) {
        LeaveChatEvent event = new LeaveChatEvent(chatId, chatMembershipMessage);
        eventHandler.post(event);
    }

    @MessageMapping("/chats/{chatId}/sendMessage")
    public void handleChatMessage(@DestinationVariable("chatId") Integer chatId,
                                  @Payload TextMessage textMessage) {
        SendTextMessageEvent event = new SendTextMessageEvent(chatId, textMessage);
        eventHandler.post(event);
    }
}
