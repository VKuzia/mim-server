package com.mimteam.mimserver.controllers.events;

import com.mimteam.mimserver.model.chat.ChatMessage;

public class LeaveChatEvent extends Event {
    private final ChatMessage message;

    public LeaveChatEvent(ChatMessage message) {
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }

    public Integer getChatId() {
        return message.getChatId();
    }
}
