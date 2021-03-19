package com.mimteam.mimserver.events;

import java.time.LocalDateTime;

public class ChatEvent {
    private final Integer chatId;
    private final LocalDateTime dateTime;

    public ChatEvent(Integer chatId) {
        this.chatId = chatId;
        this.dateTime = LocalDateTime.now();
    }

    public Integer getChatId() {
        return chatId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
