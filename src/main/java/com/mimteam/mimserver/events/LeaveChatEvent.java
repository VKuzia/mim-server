package com.mimteam.mimserver.events;

import com.mimteam.mimserver.model.messages.ChatMembershipMessage;

public class LeaveChatEvent extends ChatEvent {
    private final ChatMembershipMessage message;

    public LeaveChatEvent(ChatMembershipMessage message) {
        super(message.getChatId());
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }
}
