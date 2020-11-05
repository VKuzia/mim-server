package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.controllers.events.JoinChatEvent;
import com.mimteam.mimserver.controllers.events.LeaveChatEvent;
import com.mimteam.mimserver.controllers.events.SendTextMessageEvent;
import com.mimteam.mimserver.model.chat.ChatMessage;
import com.mimteam.mimserver.model.chat.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final EventHandler eventHandler;

    @Autowired
    ChatController(SimpMessagingTemplate simpMessagingTemplate,
                   EventHandler eventHandler) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.eventHandler = eventHandler;
    }

    @MessageMapping("/chats/{chatId}/addUser")
    public void handleAddUserMessage(@DestinationVariable("chatId") String chatId,
                                     @Payload ChatMessage chatMessage) {
        JoinChatEvent event = new JoinChatEvent(chatMessage);
        eventHandler.post(event);
    }

    @MessageMapping("/chats/{chatId}/removeUser")
    public void handleRemoveUserMessage(@DestinationVariable("chatId") String chatId,
                                        @Payload ChatMessage chatMessage) {
        LeaveChatEvent event = new LeaveChatEvent(chatMessage);
        eventHandler.post(event);
    }

    @MessageMapping("/chats/{chatId}/sendMessage")
    public void handleChatMessage(@DestinationVariable("chatId") String chatId,
                                  @Payload TextMessage textMessage) {
        SendTextMessageEvent event = new SendTextMessageEvent(textMessage);
        eventHandler.post(event);
    }

    private void sendMessageToChat(String chatId, ChatMessage message) {
        simpMessagingTemplate.convertAndSend("/chats/" + chatId, message);
    }
}
