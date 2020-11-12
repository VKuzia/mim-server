package com.mimteam.mimserver.events;

import com.mimteam.mimserver.model.messages.TextMessage;

public class SendTextMessageEvent extends ChatEvent {
    private final TextMessage message;

    public SendTextMessageEvent(TextMessage message) {
        super(message.getChatId());
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
