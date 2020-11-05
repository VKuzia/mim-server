package com.mimteam.mimserver.controllers.events;

import com.mimteam.mimserver.model.chat.TextMessage;

public class SendTextMessageEvent extends Event {
    private final TextMessage message;

    public SendTextMessageEvent(TextMessage message) {
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }

    public Integer getChatId() {
        return message.getChatId();
    }

    public String getContent() {
        return message.getContent();
    }

    public TextMessage getMessage() {
        return message;
    }

}
