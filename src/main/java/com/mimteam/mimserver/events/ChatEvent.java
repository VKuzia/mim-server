package com.mimteam.mimserver.events;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChatEvent {
    private final Integer chatId;
    private final LocalDateTime creationTime;

    public ChatEvent(Integer chatId) {
        this.chatId = chatId;
        this.creationTime = LocalDateTime.now();
    }

    public Integer getChatId() {
        return chatId;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }
}
