package com.mimteam.mimserver.model.chat;

public class ChatMessage {
    private Integer userId;
    private Integer chatId;
    private OperationType messageType;

    enum OperationType {
        JOIN, LEAVE,
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public OperationType getMessageType() {
        return messageType;
    }

    public void setMessageType(OperationType messageType) {
        this.messageType = messageType;
    }
}
