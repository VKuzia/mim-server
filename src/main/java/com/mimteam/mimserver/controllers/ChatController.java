package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @MessageMapping("/chat.addUser")
    @SendTo("/chat/public")
    public ChatMessage handleAddUserMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.message")
    @SendTo("/chat/public")
    public ChatMessage handleChatMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }
}
