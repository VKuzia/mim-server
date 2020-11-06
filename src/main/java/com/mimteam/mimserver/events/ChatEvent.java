package com.mimteam.mimserver.events;

import java.time.LocalDate;

public class ChatEvent {
    private final Integer chatId;
    private final LocalDate creationTime;

    public ChatEvent(Integer chatId) {
        this.chatId = chatId;
        this.creationTime = LocalDate.now();
    }

    public Integer getChatId() {
        return chatId;
    }

    public LocalDate getCreationTime() {
        return creationTime;
    }
}
