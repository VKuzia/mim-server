package com.mimteam.mimserver.model.chat;

public class ChatMembershipMessage {
    private Integer userId;
    private ChatMembershipMessageType messageType;

    enum ChatMembershipMessageType {
        JOIN, LEAVE
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ChatMembershipMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ChatMembershipMessageType messageType) {
        this.messageType = messageType;
    }
}
