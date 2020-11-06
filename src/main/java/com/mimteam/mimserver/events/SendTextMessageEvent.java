package com.mimteam.mimserver.events;

import com.mimteam.mimserver.model.chat.TextMessage;

public class SendTextMessageEvent extends ChatEvent {
    private final TextMessage message;

    public SendTextMessageEvent(Integer chatId, TextMessage message) {
        super(chatId);
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }

    public String getContent() {
        return message.getContent();
    }

    public TextMessage getMessage() {
        return message;
    }
}
