package com.mimteam.mimserver.controllers.events;

import com.mimteam.mimserver.model.chat.ChatMessage;

public class JoinChatEvent extends  Event {
    private final ChatMessage message;

    public JoinChatEvent(ChatMessage message) {
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }

    public Integer getChatId() {
        return message.getChatId();
    }
}
