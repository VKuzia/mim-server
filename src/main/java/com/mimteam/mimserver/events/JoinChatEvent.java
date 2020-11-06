package com.mimteam.mimserver.events;

import com.mimteam.mimserver.model.chat.ChatMembershipMessage;

public class JoinChatEvent extends ChatEvent {
    private final ChatMembershipMessage message;

    public JoinChatEvent(Integer chatId, ChatMembershipMessage message) {
        super(chatId);
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }
}
