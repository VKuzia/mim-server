package com.mimteam.mimserver.events;

import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage.ChatMembershipMessageType;

public class ChatMembershipEvent extends ChatEvent {
    private final ChatMembershipMessage message;

    public ChatMembershipEvent(ChatMembershipMessage message) {
        super(message.getChatId());
        this.message = message;
    }

    public Integer getUserId() {
        return message.getUserId();
    }

    public ChatMembershipMessageType getChatMembershipMessageType() {
        return message.getChatMembershipMessageType();
    }
}
