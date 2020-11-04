package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chats/{chatId}/addUser")
    public void handleAddUserMessage(@DestinationVariable("chatId") String chatId,
                                     @Payload ChatMessage chatMessage) {
        sendMessageToChat(chatId, chatMessage);
    }

    @MessageMapping("/chats/{chatId}/sendMessage")
    public void handleChatMessage(@DestinationVariable("chatId") String chatId,
                                  @Payload ChatMessage chatMessage) {
        sendMessageToChat(chatId, chatMessage);
    }

    private void sendMessageToChat(String chatId, ChatMessage message) {
        simpMessagingTemplate.convertAndSend("/chats/" + chatId, message);
    }
}
